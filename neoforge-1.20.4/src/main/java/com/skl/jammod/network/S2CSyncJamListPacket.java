package com.skl.jammod.network;

import com.skl.jammod.JamMod;
import com.skl.jammod.client.ClientJamManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class S2CSyncJamListPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(JamMod.MODID + ":sync_jam_list");

    public record JamSummary(String id, String name, UUID ownerId, String ownerName,
                             boolean isPrivate, int trackCount, int listeners,
                             boolean playing, String currentTrack) {}

    public final List<JamSummary> summaries;

    public S2CSyncJamListPacket(List<JamSummary> summaries) {
        this.summaries = summaries != null ? summaries : new ArrayList<>();
    }

    public S2CSyncJamListPacket(FriendlyByteBuf buf) {
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
        this.summaries = list;
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
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

    public static void handle(S2CSyncJamListPacket msg, IPayloadContext ctx) {
        ctx.workHandler().execute(() -> ClientJamManager.setDirectory(msg.summaries));
    }
}