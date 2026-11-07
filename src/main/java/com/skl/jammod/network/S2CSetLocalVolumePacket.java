package com.skl.jammod.network;

import com.skl.jammod.JamMod;
import com.skl.jammod.client.ClientAudioEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

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

public class S2CSetLocalVolumePacket
//? if >1.20.1 {
        /*implements net.minecraft.network.protocol.common.custom.CustomPacketPayload
*///?}
{
    //? if >=1.21 {
    /*public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<S2CSetLocalVolumePacket> TYPE =
            new net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JamMod.MODID, "set_local_volume"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, S2CSetLocalVolumePacket> STREAM_CODEC =
            net.minecraft.network.codec.StreamCodec.ofMember(S2CSetLocalVolumePacket::write, S2CSetLocalVolumePacket::decode);

    @Override
    public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
        return TYPE;
    }
    *///?} else if >1.20.1 {
    /*public static final ResourceLocation ID = new ResourceLocation(JamMod.MODID, "set_local_volume");

    @Override
    public ResourceLocation id() {
        return ID;
    }
    *///?}

    //? if >1.20.1 {
    /*@Override
    public void write(FriendlyByteBuf buf) {
        encode(this, buf);
    }
    *///?}

    public final int volume;

    public S2CSetLocalVolumePacket(int volume) {
        this.volume = volume;
    }

    public static void encode(S2CSetLocalVolumePacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.volume);
    }

    public static S2CSetLocalVolumePacket decode(FriendlyByteBuf buf) {
        return new S2CSetLocalVolumePacket(buf.readVarInt());
    }

    private static void applyVolume(int volume) {
        Minecraft mc = Minecraft.getInstance();
        double target = Math.max(0.0, Math.min(1.0, volume / 100.0));
        //? if >=1.19.4 {
        mc.options.getSoundSourceOptionInstance(SoundSource.MUSIC).set(target);
        //?} else {
        /*mc.options.setSoundCategoryVolume(SoundSource.MUSIC, (float) target);
         *///?}
        mc.options.save();
        ClientAudioEngine.updateVolume();
    }

    //? if >=1.21 {
    /*public static void handle(S2CSetLocalVolumePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> applyVolume(msg.volume));
    }
    *///?} else if >1.20.1 {
    /*public static void handle(S2CSetLocalVolumePacket msg, PlayPayloadContext ctx) {
        ctx.workHandler().execute(() -> applyVolume(msg.volume));
    }
    *///?} else {
    public static void handle(S2CSetLocalVolumePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> applyVolume(msg.volume)));
        ctx.get().setPacketHandled(true);
    }
    //?}
}