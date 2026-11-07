package com.skl.jammod.network;

import com.skl.jammod.JamMod;
import com.skl.jammod.client.ClientJamManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

//? if >=1.21 {
/*import net.neoforged.neoforge.network.handling.IPayloadContext;
        *///?} else if >1.20.1 {
/*import net.neoforged.neoforge.network.handling.PlayPayloadContext;
 *///?} else {
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
//?}

public class S2CSyncJamListPacket
//? if >1.20.1 {
        /*implements net.minecraft.network.protocol.common.custom.CustomPacketPayload
*///?}
{
    //? if >=1.21 {
    /*public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<S2CSyncJamListPacket> TYPE =
            new net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JamMod.MODID, "sync_jam_list"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, S2CSyncJamListPacket> STREAM_CODEC =
            net.minecraft.network.codec.StreamCodec.ofMember(S2CSyncJamListPacket::write, S2CSyncJamListPacket::decode);

    @Override
    public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
        return TYPE;
    }
    *///?} else if >1.20.1 {
    /*public static final ResourceLocation ID = new ResourceLocation(JamMod.MODID, "sync_jam_list");

    @Override
    public ResourceLocation id() {
        return ID;
    }
    *///?}

    //? if >1.20.1 {
    /*public void write(FriendlyByteBuf buf) {
        encode(this, buf);
    }
    *///?}

    public record JamSummary(String id, String name, UUID ownerId, String ownerName, boolean isPrivate, int trackCount, int listeners, boolean isPlaying, String currentTrackTitle) {}

    public final List<JamSummary> directory;

    public S2CSyncJamListPacket(List<JamSummary> directory) {
        this.directory = directory;
    }

    public static void encode(S2CSyncJamListPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.directory.size());
        for (JamSummary j : msg.directory) {
            buf.writeUtf(j.id());
            buf.writeUtf(j.name());
            buf.writeUUID(j.ownerId());
            buf.writeUtf(j.ownerName());
            buf.writeBoolean(j.isPrivate());
            buf.writeVarInt(j.trackCount());
            buf.writeVarInt(j.listeners());
            buf.writeBoolean(j.isPlaying());
            buf.writeUtf(j.currentTrackTitle());
        }
    }

    public static S2CSyncJamListPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<JamSummary> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(new JamSummary(
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readUUID(),
                    buf.readUtf(),
                    buf.readBoolean(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readBoolean(),
                    buf.readUtf()
            ));
        }
        return new S2CSyncJamListPacket(list);
    }

    //? if >=1.21 {
    /*public static void handle(S2CSyncJamListPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientJamManager.setDirectory(msg.directory));
    }
    *///?} else if >1.20.1 {
    /*public static void handle(S2CSyncJamListPacket msg, PlayPayloadContext ctx) {
        ctx.workHandler().execute(() -> ClientJamManager.setDirectory(msg.directory));
    }
    *///?} else {
    public static void handle(S2CSyncJamListPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientJamManager.setDirectory(msg.directory)));
        ctx.get().setPacketHandled(true);
    }
    //?}
}