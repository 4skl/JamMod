package com.skl.jammod.network;

import com.skl.jammod.session.AntiSpamManager;
import com.skl.jammod.session.JamEditPolicy;
import com.skl.jammod.session.ServerJamRegistry;
import com.skl.jammod.session.SyncedJam;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Supplier;

public class C2SJamActionPacket {
    public enum Action {
        CREATE_JAM, JOIN_JAM, LEAVE_JAM, DELETE_JAM,
        TOGGLE_PLAY, SKIP, PREVIOUS, JUMP_TRACK,
        ADD_TRACK, REMOVE_TRACK, MOVE_TRACK,
        SET_POLICY, TOGGLE_PRIVACY, INVITE_PLAYER, CYCLE_MEMBER_ROLE, ADD_MEMBER_BY_NAME, REMOVE_MEMBER,
        UPDATE_METADATA, SEEK, CYCLE_REPEAT, TOGGLE_SHUFFLE,
        REQUEST_SYNC
    }

    private final Action action;
    private final String targetId;
    private final String payload;
    private final boolean flag;
    private final int intArg1;
    private final int intArg2;

    public C2SJamActionPacket(Action action) { this(action, "", "", false, 0, 0); }
    public C2SJamActionPacket(Action action, String payload) { this(action, "", payload, false, 0, 0); }
    public C2SJamActionPacket(Action action, String targetId, String payload) { this(action, targetId, payload, false, 0, 0); }
    public C2SJamActionPacket(Action action, String targetId, String payload, boolean flag, int intArg1) { this(action, targetId, payload, flag, intArg1, 0); }
    public C2SJamActionPacket(Action action, String targetId, String payload, boolean flag, int intArg1, int intArg2) {
        this.action = action;
        this.targetId = targetId;
        this.payload = payload;
        this.flag = flag;
        this.intArg1 = intArg1;
        this.intArg2 = intArg2;
    }

    public static void encode(C2SJamActionPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
        buf.writeUtf(msg.targetId);
        buf.writeUtf(msg.payload);
        buf.writeBoolean(msg.flag);
        buf.writeInt(msg.intArg1);
        buf.writeInt(msg.intArg2);
    }

    public static C2SJamActionPacket decode(FriendlyByteBuf buf) {
        return new C2SJamActionPacket(
                buf.readEnum(Action.class),
                buf.readUtf(),
                buf.readUtf(),
                buf.readBoolean(),
                buf.readInt(),
                buf.readInt()
        );
    }

    public static void handle(C2SJamActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || player.getServer() == null) return;
            UUID uuid = player.getUUID();
            String playerName = player.getGameProfile().getName();

            // Resolution fallback so targetId or payload can be passed interchangeably
            String idOrName = !msg.targetId.isBlank() ? msg.targetId : msg.payload;

            switch (msg.action) {
                case CREATE_JAM -> {
                    SyncedJam jam = ServerJamRegistry.createJam(msg.payload, uuid, playerName, msg.flag);
                    ServerJamRegistry.joinJam(jam.getId(), uuid, playerName);
                    ServerJamRegistry.broadcastJamState(player.getServer(), jam);
                    ServerJamRegistry.broadcastDirectory(player.getServer());
                    return;
                }
                case JOIN_JAM -> {
                    if (ServerJamRegistry.joinJam(idOrName, uuid, playerName)) {
                        SyncedJam jam = ServerJamRegistry.getJamByPlayer(uuid);
                        ServerJamRegistry.broadcastJamState(player.getServer(), jam);
                        ServerJamRegistry.broadcastDirectory(player.getServer());
                    } else {
                        player.sendSystemMessage(Component.literal("§c[JamMod] Cannot join Jam: '" + idOrName + "' (Private or Not Found)."));
                    }
                    return;
                }
                case LEAVE_JAM -> {
                    SyncedJam old = ServerJamRegistry.leaveCurrentJam(uuid);
                    ModNetworking.sendToPlayer(player, new S2CSyncJamPacket(
                            "", "", "", false, JamEditPolicy.OPEN, SyncedJam.RepeatMode.OFF, false,
                            "", "", 0, 0, false, new ArrayList<>(), new ArrayList<>(), 0, false, false, false));
                    if (old != null) ServerJamRegistry.broadcastJamState(player.getServer(), old);
                    ServerJamRegistry.broadcastDirectory(player.getServer());
                    return;
                }
                case DELETE_JAM -> {
                    ServerJamRegistry.deleteJam(idOrName, uuid);
                    ServerJamRegistry.broadcastDirectory(player.getServer());
                    return;
                }
                case INVITE_PLAYER -> {
                    SyncedJam jam = ServerJamRegistry.getJamByPlayer(uuid);
                    if (jam == null) return;
                    ServerPlayer target = player.getServer().getPlayerList().getPlayerByName(msg.payload);
                    if (target == null) {
                        player.sendSystemMessage(Component.literal("§c[JamMod] Player '" + msg.payload + "' is not online."));
                        return;
                    }
                    String err = AntiSpamManager.canSendInvite(uuid, target.getUUID());
                    if (err != null) {
                        player.sendSystemMessage(Component.literal("§c[JamMod] " + err));
                        return;
                    }

                    AntiSpamManager.recordInvite(uuid, target.getUUID());
                    jam.invitePlayer(target.getUUID(), target.getName().getString());
                    ServerJamRegistry.save();

                    player.sendSystemMessage(Component.literal("§6[JamMod] §aInvited §f" + target.getName().getString() + " §ato §f" + jam.getName()));
                    target.sendSystemMessage(Component.literal("§6[JamMod] §f" + playerName + " §ainvited you to Jam: §e" + jam.getName() + " ")
                            .append(Component.literal("§a§l[Click to Join]").withStyle(s -> s
                                    .withColor(net.minecraft.ChatFormatting.GREEN)
                                    .withUnderlined(true)
                                    .withClickEvent(new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND, "/jam join " + jam.getName()))
                                    .withHoverEvent(new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.literal("Click to join " + jam.getName())))))
                    );
                    return;
                }
                case REQUEST_SYNC -> {
                    ServerJamRegistry.syncDirectoryToPlayer(player);
                    SyncedJam current = ServerJamRegistry.getJamByPlayer(uuid);
                    if (current != null) {
                        ServerJamRegistry.broadcastJamState(player.getServer(), current);
                    }
                    return;
                }
                default -> {}
            }

            SyncedJam active = ServerJamRegistry.getJamByPlayer(uuid);
            if (active == null) return;

            if (msg.action == Action.UPDATE_METADATA) {
                long durationMs = (long) msg.intArg2 * 1000L;
                active.updateTrackMetadata(msg.intArg1, msg.payload, durationMs);
                ServerJamRegistry.save();
                ServerJamRegistry.broadcastJamState(player.getServer(), active);
                return;
            }

            // Management actions (Host & Jam Admins)
            if (active.canManage(uuid)) {
                if (msg.action == Action.SET_POLICY) {
                    active.setEditPolicy(active.getEditPolicy().next());
                    ServerJamRegistry.save();
                    ServerJamRegistry.broadcastJamState(player.getServer(), active);
                    return;
                } else if (msg.action == Action.TOGGLE_PRIVACY && active.isOwner(uuid)) {
                    active.setPrivate(!active.isPrivate());
                    ServerJamRegistry.save();
                    ServerJamRegistry.broadcastJamState(player.getServer(), active);
                    ServerJamRegistry.broadcastDirectory(player.getServer());
                    return;
                } else if (msg.action == Action.CYCLE_MEMBER_ROLE && !msg.targetId.isBlank()) {
                    try {
                        UUID targetMember = UUID.fromString(msg.targetId);
                        active.cycleRole(targetMember);
                        ServerJamRegistry.save();
                        ServerJamRegistry.broadcastJamState(player.getServer(), active);
                    } catch (Exception ignored) {}
                    return;
                } else if (msg.action == Action.ADD_MEMBER_BY_NAME && !msg.payload.isBlank()) {
                    ServerPlayer target = player.getServer().getPlayerList().getPlayerByName(msg.payload.trim());
                    if (target != null) {
                        active.setRole(target.getUUID(), target.getName().getString(), SyncedJam.Role.EDITOR);
                    } else {
                        // Offline player registration
                        UUID offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + msg.payload.trim()).getBytes());
                        active.setRole(offlineUuid, msg.payload.trim(), SyncedJam.Role.EDITOR);
                    }
                    ServerJamRegistry.save();
                    ServerJamRegistry.broadcastJamState(player.getServer(), active);
                    player.sendSystemMessage(Component.literal("§6[JamMod] §aAdded §f" + msg.payload.trim() + " §aas Editor."));
                    return;
                } else if (msg.action == Action.REMOVE_MEMBER && !msg.targetId.isBlank()) {
                    try {
                        active.removeMember(UUID.fromString(msg.targetId));
                        ServerJamRegistry.save();
                        ServerJamRegistry.broadcastJamState(player.getServer(), active);
                    } catch (Exception ignored) {}
                    return;
                }
            }

            // Playback controls (Collaborators / Open permissions)
            if (!active.canEdit(uuid)) {
                player.sendSystemMessage(Component.literal("§c[JamMod] You do not have permission to modify this Jam."));
                return;
            }

            switch (msg.action) {
                case TOGGLE_PLAY -> active.togglePlay();
                case SKIP -> active.skip();
                case PREVIOUS -> active.previous();
                case JUMP_TRACK -> active.playTrackAt(msg.intArg1);
                case ADD_TRACK -> {
                    active.addTrack(msg.payload, msg.targetId, msg.flag);
                    ServerJamRegistry.save();
                }
                case REMOVE_TRACK -> {
                    active.removeTrack(msg.intArg1);
                    ServerJamRegistry.save();
                }
                case MOVE_TRACK -> {
                    active.moveTrack(msg.intArg1, msg.intArg2);
                    ServerJamRegistry.save();
                }
                case SEEK -> {
                    try {
                        long offsetMs = Long.parseLong(msg.payload);
                        active.seek(offsetMs);
                    } catch (NumberFormatException ignored) {}
                }
                case CYCLE_REPEAT -> {
                    SyncedJam.RepeatMode next = SyncedJam.RepeatMode.values()[(active.getRepeatMode().ordinal() + 1) % SyncedJam.RepeatMode.values().length];
                    active.setRepeatMode(next);
                    ServerJamRegistry.save();
                }
                case TOGGLE_SHUFFLE -> {
                    active.setShuffle(!active.isShuffle());
                    ServerJamRegistry.save();
                }
                default -> {}
            }

            ServerJamRegistry.broadcastJamState(player.getServer(), active);
            ServerJamRegistry.broadcastDirectory(player.getServer());
        });
        ctx.get().setPacketHandled(true);
    }
}