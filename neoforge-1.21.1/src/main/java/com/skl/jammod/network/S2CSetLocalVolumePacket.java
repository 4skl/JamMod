package com.skl.jammod.network;

import com.skl.jammod.JamMod;
import com.skl.jammod.client.ClientAudioEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CSetLocalVolumePacket implements CustomPacketPayload {
    public static final Type<S2CSetLocalVolumePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(JamMod.MODID, "set_local_volume"));

    public static final StreamCodec<FriendlyByteBuf, S2CSetLocalVolumePacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> msg.encode(buf),
            S2CSetLocalVolumePacket::decode
    );

    private final int volume;

    public S2CSetLocalVolumePacket(int volume) {
        this.volume = volume;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.volume);
    }

    public static S2CSetLocalVolumePacket decode(FriendlyByteBuf buf) {
        return new S2CSetLocalVolumePacket(buf.readInt());
    }

    public static void handle(S2CSetLocalVolumePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            double target = Math.max(0.0, Math.min(1.0, msg.volume / 100.0));
            mc.options.getSoundSourceOptionInstance(SoundSource.MUSIC).set(target);
            mc.options.save();
            ClientAudioEngine.updateVolume();
        });
    }
}