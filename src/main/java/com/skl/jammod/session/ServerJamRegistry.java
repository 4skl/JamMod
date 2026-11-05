package com.skl.jammod.session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.skl.jammod.JamMod;
import com.skl.jammod.network.ModNetworking;
import com.skl.jammod.network.S2CSyncJamListPacket;
import com.skl.jammod.network.S2CSyncJamPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerJamRegistry {
    private static final Path SAVE_FILE = FMLPaths.CONFIGDIR.get().resolve("jammod_jams.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<String, SyncedJam> JAMS = new ConcurrentHashMap<>();
    private static final Map<UUID, String> PLAYER_TO_JAM = new ConcurrentHashMap<>();

    public static synchronized void init() {
        JAMS.clear();
        if (Files.exists(SAVE_FILE)) {
            try (Reader reader = Files.newBufferedReader(SAVE_FILE)) {
                Type type = new TypeToken<List<SyncedJam>>() {}.getType();
                List<SyncedJam> list = GSON.fromJson(reader, type);
                if (list != null) {
                    for (SyncedJam jam : list) {
                        jam.ensureRuntimeState();
                        JAMS.put(jam.getId(), jam);
                    }
                }
            } catch (Exception e) {
                JamMod.LOGGER.error("[JamMod] Failed to load saved jams: ", e);
            }
        }
    }

    public static synchronized void save() {
        try (Writer writer = Files.newBufferedWriter(SAVE_FILE)) {
            GSON.toJson(new ArrayList<>(JAMS.values()), writer);
        } catch (Exception e) {
            JamMod.LOGGER.error("[JamMod] Failed to save jams: ", e);
        }
    }

    public static synchronized SyncedJam createJam(String name, UUID owner, String ownerName, boolean isPrivate) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        SyncedJam jam = new SyncedJam(id, name, owner, ownerName, isPrivate);
        JAMS.put(id, jam);
        save();
        return jam;
    }

    public static synchronized boolean deleteJam(String idOrName, UUID requester) {
        SyncedJam jam = findJam(idOrName, requester);
        if (jam != null && jam.isOwner(requester)) {
            for (UUID listener : jam.getListeners()) {
                PLAYER_TO_JAM.remove(listener);
            }
            JAMS.remove(jam.getId());
            save();
            return true;
        }
        return false;
    }

    public static synchronized boolean joinJam(String idOrName, UUID player, String playerName) {
        SyncedJam jam = findJam(idOrName, player);
        if (jam != null && jam.isAllowed(player)) {
            leaveCurrentJam(player);
            PLAYER_TO_JAM.put(player, jam.getId());
            jam.addListener(player, playerName);
            return true;
        }
        return false;
    }

    public static synchronized SyncedJam leaveCurrentJam(UUID player) {
        String jamId = PLAYER_TO_JAM.remove(player);
        if (jamId != null) {
            SyncedJam jam = JAMS.get(jamId);
            if (jam != null) {
                jam.removeListener(player);
                return jam;
            }
        }
        return null;
    }

    public static SyncedJam getJamByPlayer(UUID player) {
        String id = PLAYER_TO_JAM.get(player);
        return id != null ? JAMS.get(id) : null;
    }

    public static SyncedJam findJam(String query, UUID player) {
        if (query == null || query.isBlank()) return null;
        String clean = query.trim();

        // 1. Direct ID match
        for (SyncedJam j : JAMS.values()) {
            j.ensureRuntimeState();
            if (j.getId().equalsIgnoreCase(clean)) {
                if (j.isAllowed(player)) return j;
            }
        }
        // 2. Name match
        for (SyncedJam j : JAMS.values()) {
            j.ensureRuntimeState();
            if (j.getName().equalsIgnoreCase(clean)) {
                if (j.isAllowed(player)) return j;
            }
        }
        return null;
    }

    public static List<SyncedJam> getVisibleJams(UUID player) {
        List<SyncedJam> list = new ArrayList<>();
        for (SyncedJam j : JAMS.values()) {
            j.ensureRuntimeState();
            if (j.isAllowed(player)) {
                list.add(j);
            }
        }
        return list;
    }

    public static void broadcastJamState(MinecraftServer server, SyncedJam jam) {
        if (jam == null || server == null) return;
        jam.ensureRuntimeState();
        JamTrack cur = jam.getCurrentTrack();
        String curUrl = cur != null ? cur.getUrl() : "";
        String curTitle = cur != null ? cur.getTitle() : "";

        for (UUID memberId : jam.getListeners()) {
            ServerPlayer member = server.getPlayerList().getPlayer(memberId);
            if (member != null) {
                boolean canEdit = jam.canEdit(memberId);
                boolean isOwner = jam.isOwner(memberId);
                boolean isAdmin = jam.isAdmin(memberId);
                S2CSyncJamPacket packet = new S2CSyncJamPacket(
                        jam.getId(), jam.getName(), jam.getOwnerName(), jam.isPrivate(),
                        jam.getEditPolicy(), jam.getRepeatMode(), jam.isShuffle(),
                        curUrl, curTitle, jam.getCurrentTrackIndex(),
                        jam.getStartTimestamp(), jam.isPlaying(), jam.getTracks(),
                        jam.getMemberList(),
                        jam.getListeners().size(), canEdit, isOwner, isAdmin
                );
                ModNetworking.sendToPlayer(member, packet);
            }
        }
    }

    public static void broadcastDirectory(MinecraftServer server) {
        if (server == null) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncDirectoryToPlayer(player);
        }
    }

    public static void syncDirectoryToPlayer(ServerPlayer player) {
        List<SyncedJam> visible = getVisibleJams(player.getUUID());
        List<S2CSyncJamListPacket.JamSummary> summaries = new ArrayList<>();
        for (SyncedJam j : visible) {
            j.ensureRuntimeState();
            JamTrack cur = j.getCurrentTrack();
            summaries.add(new S2CSyncJamListPacket.JamSummary(
                    j.getId(), j.getName(), j.getOwnerId(), j.getOwnerName(),
                    j.isPrivate(), j.getTracks().size(), j.getListeners().size(),
                    j.isPlaying(), cur != null ? cur.getTitle() : ""
            ));
        }
        ModNetworking.sendToPlayer(player, new S2CSyncJamListPacket(summaries));
    }
}