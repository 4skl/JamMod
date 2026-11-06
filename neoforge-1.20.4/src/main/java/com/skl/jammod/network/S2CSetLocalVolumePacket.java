package com.skl.jammod.network;

import com.skl.jammod.JamMod;
import com.skl.jammod.client.ClientAudioEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CSetLocalVolumePacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(JamMod.MODID + ":set_local_volume");

    private final int volume;

    public S2CSetLocalVolumePacket(int volume) {
        this.volume = volume;
    }

    public S2CSetLocalVolumePacket(FriendlyByteBuf buf) {
        this.volume = buf.readInt();
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.volume);
    }

    public static void handle(S2CSetLocalVolumePacket msg, IPayloadContext ctx) {
        ctx.workHandler().execute(() -> {
            Minecraft mc = Minecraft.getInstance();
            double target = Math.max(0.0, Math.min(1.0, msg.volume / 100.0));
            mc.options.getSoundSourceOptionInstance(SoundSource.MUSIC).set(target);
            mc.options.save();
            ClientAudioEngine.updateVolume();
        });
    }
}