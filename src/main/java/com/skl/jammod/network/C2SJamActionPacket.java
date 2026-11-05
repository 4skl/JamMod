package com.skl.jammod.network;

import com.skl.jammod.JamMod;
import com.skl.jammod.session.JamSession;
import com.skl.jammod.session.ServerJamRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class C2SJamActionPacket {
    public enum Action { CREATE, JOIN, LEAVE, PLAY_URL, TOGGLE_PLAY, SKIP }

    private final Action action;
    private final String payload;

    public C2SJamActionPacket(Action action, String payload) {
        this.action = action;
        this.payload = payload;
    }

    public static void encode(C2SJamActionPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
        buf.writeUtf(msg.payload);
    }

    public static C2SJamActionPacket decode(FriendlyByteBuf buf) {
        return new C2SJamActionPacket(buf.readEnum(Action.class), buf.readUtf());
    }

    public static void handle(C2SJamActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            UUID uuid = player.getUUID();

            JamMod.LOGGER.info("[JamServer] Action {} from {} (Payload: {})", msg.action, player.getName().getString(), msg.payload);

            JamSession session;
            switch (msg.action) {
                case CREATE -> session = ServerJamRegistry.createSession(msg.payload, uuid);
                case JOIN -> {
                    ServerJamRegistry.joinSession(msg.payload, uuid);
                    session = ServerJamRegistry.getSessionByPlayer(uuid);
                }
                case LEAVE -> {
                    JamSession oldSession = ServerJamRegistry.getSessionByPlayer(uuid);
                    ServerJamRegistry.leaveSession(uuid);
                    ModNetworking.sendToPlayer(player, new S2CSyncJamPacket("", "", 0, false));
                    session = oldSession;
                }
                case PLAY_URL -> {
                    session = ServerJamRegistry.getOrCreateSession(uuid, player.getGameProfile().getName());
                    if (!msg.payload.isBlank()) {
                        session.playTrackNow(msg.payload);
                    }
                }
                case TOGGLE_PLAY -> {
                    session = ServerJamRegistry.getOrCreateSession(uuid, player.getGameProfile().getName());
                    session.togglePlay();
                }
                case SKIP -> {
                    session = ServerJamRegistry.getSessionByPlayer(uuid);
                    if (session != null) {
                        session.skip();
                    }
                }
                default -> session = ServerJamRegistry.getSessionByPlayer(uuid);
            }

            if (session != null && player.getServer() != null) {
                S2CSyncJamPacket sync = new S2CSyncJamPacket(
                        session.getId(),
                        session.getCurrentTrack(),
                        session.getStartTimestamp(),
                        session.isPlaying()
                );

                for (UUID memberId : session.getMembers()) {
                    ServerPlayer member = player.getServer().getPlayerList().getPlayer(memberId);
                    if (member != null) {
                        ModNetworking.sendToPlayer(member, sync);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}