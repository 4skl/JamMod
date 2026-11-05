package com.skl.jammod.network;

import com.skl.jammod.client.ClientJamManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncJamPacket {
    private final String sessionId;
    private final String currentTrack;
    private final long startTimestamp;
    private final boolean isPlaying;

    public S2CSyncJamPacket(String sessionId, String currentTrack, long startTimestamp, boolean isPlaying) {
        this.sessionId = sessionId;
        this.currentTrack = currentTrack;
        this.startTimestamp = startTimestamp;
        this.isPlaying = isPlaying;
    }

    public static void encode(S2CSyncJamPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.sessionId);
        buf.writeUtf(msg.currentTrack);
        buf.writeLong(msg.startTimestamp);
        buf.writeBoolean(msg.isPlaying);
    }

    public static S2CSyncJamPacket decode(FriendlyByteBuf buf) {
        return new S2CSyncJamPacket(buf.readUtf(), buf.readUtf(), buf.readLong(), buf.readBoolean());
    }

    public static void handle(S2CSyncJamPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        ClientJamManager.updateSession(msg.sessionId, msg.currentTrack, msg.startTimestamp, msg.isPlaying)
                )
        );
        ctx.get().setPacketHandled(true);
    }
}