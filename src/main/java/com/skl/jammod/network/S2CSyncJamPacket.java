package com.skl.jammod.network;

import com.skl.jammod.client.ClientJamManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2CSyncJamPacket {
    private final String jamId;
    private final String jamName;
    private final String ownerName;
    private final boolean isPrivate;
    private final String currentTrack;
    private final int trackIndex;
    private final long startTimestamp;
    private final boolean isPlaying;
    private final List<String> tracks;
    private final int listenerCount;

    public S2CSyncJamPacket(String jamId, String jamName, String ownerName, boolean isPrivate,
                            String currentTrack, int trackIndex, long startTimestamp, boolean isPlaying,
                            List<String> tracks, int listenerCount) {
        this.jamId = jamId;
        this.jamName = jamName;
        this.ownerName = ownerName;
        this.isPrivate = isPrivate;
        this.currentTrack = currentTrack;
        this.trackIndex = trackIndex;
        this.startTimestamp = startTimestamp;
        this.isPlaying = isPlaying;
        this.tracks = tracks != null ? tracks : new ArrayList<>();
        this.listenerCount = listenerCount;
    }

    public static void encode(S2CSyncJamPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.jamId);
        buf.writeUtf(msg.jamName);
        buf.writeUtf(msg.ownerName);
        buf.writeBoolean(msg.isPrivate);
        buf.writeUtf(msg.currentTrack);
        buf.writeInt(msg.trackIndex);
        buf.writeLong(msg.startTimestamp);
        buf.writeBoolean(msg.isPlaying);
        buf.writeCollection(msg.tracks, FriendlyByteBuf::writeUtf);
        buf.writeInt(msg.listenerCount);
    }

    public static S2CSyncJamPacket decode(FriendlyByteBuf buf) {
        return new S2CSyncJamPacket(
                buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readBoolean(),
                buf.readUtf(), buf.readInt(), buf.readLong(), buf.readBoolean(),
                buf.readList(FriendlyByteBuf::readUtf), buf.readInt()
        );
    }

    public static void handle(S2CSyncJamPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        ClientJamManager.updateSession(
                                msg.jamId, msg.jamName, msg.ownerName, msg.isPrivate,
                                msg.currentTrack, msg.trackIndex, msg.startTimestamp,
                                msg.isPlaying, msg.tracks, msg.listenerCount
                        )
                )
        );
        ctx.get().setPacketHandled(true);
    }
}