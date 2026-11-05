package com.skl.jammod.network;

import com.skl.jammod.session.ServerJamRegistry;
import com.skl.jammod.session.SyncedJam;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Supplier;

public class C2SJamActionPacket {
    public enum Action {
        CREATE_JAM, JOIN_JAM, LEAVE_JAM, DELETE_JAM,
        TOGGLE_PLAY, SKIP, PREVIOUS, JUMP_TRACK,
        ADD_TRACK, REMOVE_TRACK, TOGGLE_PRIVACY, REQUEST_SYNC
    }

    private final Action action;
    private final String targetId;
    private final String payload;
    private final boolean flag;
    private final int index;

    public C2SJamActionPacket(Action action) {
        this(action, "", "", false, 0);
    }

    public C2SJamActionPacket(Action action, String payload) {
        this(action, "", payload, false, 0);
    }

    public C2SJamActionPacket(Action action, String targetId, String payload, boolean flag, int index) {
        this.action = action;
        this.targetId = targetId;
        this.payload = payload;
        this.flag = flag;
        this.index = index;
    }

    public static void encode(C2SJamActionPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
        buf.writeUtf(msg.targetId);
        buf.writeUtf(msg.payload);
        buf.writeBoolean(msg.flag);
        buf.writeInt(msg.index);
    }

    public static C2SJamActionPacket decode(FriendlyByteBuf buf) {
        return new C2SJamActionPacket(
                buf.readEnum(Action.class),
                buf.readUtf(),
                buf.readUtf(),
                buf.readBoolean(),
                buf.readInt()
        );
    }

    public static void handle(C2SJamActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            UUID uuid = player.getUUID();

            switch (msg.action) {
                case CREATE_JAM -> {
                    SyncedJam jam = ServerJamRegistry.createJam(msg.payload, uuid, player.getGameProfile().getName(), msg.flag);
                    ServerJamRegistry.joinJam(jam.getId(), uuid);
                    ServerJamRegistry.broadcastJamState(player.getServer(), jam);
                    ServerJamRegistry.broadcastDirectory(player.getServer());
                    return;
                }
                case JOIN_JAM -> {
                    if (ServerJamRegistry.joinJam(msg.targetId, uuid)) {
                        SyncedJam jam = ServerJamRegistry.getJamByPlayer(uuid);
                        ServerJamRegistry.broadcastJamState(player.getServer(), jam);
                        ServerJamRegistry.broadcastDirectory(player.getServer());
                    }
                    return;
                }
                case LEAVE_JAM -> {
                    SyncedJam old = ServerJamRegistry.leaveCurrentJam(uuid);
                    ModNetworking.sendToPlayer(player, new S2CSyncJamPacket("", "", "", false, "", 0, 0, false, new ArrayList<>(), 0));
                    if (old != null) ServerJamRegistry.broadcastJamState(player.getServer(), old);
                    ServerJamRegistry.broadcastDirectory(player.getServer());
                    return;
                }
                case DELETE_JAM -> {
                    ServerJamRegistry.deleteJam(msg.targetId, uuid);
                    ServerJamRegistry.broadcastDirectory(player.getServer());
                    return;
                }
                case TOGGLE_PRIVACY -> {
                    SyncedJam jam = ServerJamRegistry.findJam(msg.targetId, uuid);
                    if (jam != null && jam.getOwnerId().equals(uuid)) {
                        jam.setPrivate(!jam.isPrivate());
                        ServerJamRegistry.save();
                        ServerJamRegistry.broadcastDirectory(player.getServer());
                        ServerJamRegistry.broadcastJamState(player.getServer(), jam);
                    }
                    return;
                }
                case REQUEST_SYNC -> {
                    ServerJamRegistry.broadcastDirectory(player.getServer());
                    SyncedJam current = ServerJamRegistry.getJamByPlayer(uuid);
                    if (current != null) {
                        ServerJamRegistry.broadcastJamState(player.getServer(), current);
                    }
                    return;
                }
                default -> {}
            }

            // Playback controls on active room
            SyncedJam active = ServerJamRegistry.getJamByPlayer(uuid);
            if (active != null) {
                switch (msg.action) {
                    case TOGGLE_PLAY -> active.togglePlay();
                    case SKIP -> active.skip();
                    case PREVIOUS -> active.previous();
                    case JUMP_TRACK -> active.playTrackAt(msg.index);
                    case ADD_TRACK -> {
                        active.addTrack(msg.payload, msg.flag);
                        ServerJamRegistry.save();
                    }
                    case REMOVE_TRACK -> {
                        active.removeTrack(msg.index);
                        ServerJamRegistry.save();
                    }
                    default -> {}
                }
                ServerJamRegistry.broadcastJamState(player.getServer(), active);
                ServerJamRegistry.broadcastDirectory(player.getServer());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}