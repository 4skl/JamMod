package com.skl.jammod.network;

import com.skl.jammod.JamMod;
import com.skl.jammod.client.ClientJamManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class S2CSyncJamListPacket implements CustomPacketPayload {
    public static final Type<S2CSyncJamListPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(JamMod.MODID, "sync_jam_list"));

    public static final StreamCodec<FriendlyByteBuf, S2CSyncJamListPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> msg.encode(buf),
            S2CSyncJamListPacket::decode
    );

    public record JamSummary(String id, String name, UUID ownerId, String ownerName,
                             boolean isPrivate, int trackCount, int listeners,
                             boolean playing, String currentTrack) {}

    private final List<JamSummary> summaries;

    public S2CSyncJamListPacket(List<JamSummary> summaries) {
        this.summaries = summaries != null ? summaries : new ArrayList<>();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.summaries.size());
        for (JamSummary s : this.summaries) {
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

    public static void handle(S2CSyncJamListPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientJamManager.setDirectory(msg.summaries));
    }
}