package com.skl.jammod.network;

import com.skl.jammod.client.ClientJamManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class S2CSyncJamListPacket {
    public record JamSummary(String id, String name, UUID ownerId, String ownerName,
                             boolean isPrivate, int trackCount, int listeners,
                             boolean playing, String currentTrack) {}

    private final List<JamSummary> summaries;

    public S2CSyncJamListPacket(List<JamSummary> summaries) {
        this.summaries = summaries != null ? summaries : new ArrayList<>();
    }

    public static void encode(S2CSyncJamListPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.summaries.size());
        for (JamSummary s : msg.summaries) {
            buf.writeUtf(s.id());
            buf.writeUtf(s.name());
            buf.writeUUID(s.ownerId());
            buf.writeUtf(s.ownerName());
            buf.writeBoolean(s.isPrivate());
            buf.writeInt(s.trackCount());
            buf.writeInt(s.listeners());
            buf.writeBoolean(s.playing());
            buf.writeUtf(s.currentTrack());
        }
    }

    public static S2CSyncJamListPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<JamSummary> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(new JamSummary(
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readUUID(),
                    buf.readUtf(),
                    buf.readBoolean(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readBoolean(),
                    buf.readUtf()
            ));
        }
        return new S2CSyncJamListPacket(list);
    }

    public static void handle(S2CSyncJamListPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        ClientJamManager.setDirectory(msg.summaries)
                )
        );
        ctx.get().setPacketHandled(true);
    }
}