package com.skl.jammod.network;

import com.skl.jammod.client.ClientAudioEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSetLocalVolumePacket {
    private final int volume;

    public S2CSetLocalVolumePacket(int volume) {
        this.volume = volume;
    }

    public static void encode(S2CSetLocalVolumePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.volume);
    }

    public static S2CSetLocalVolumePacket decode(FriendlyByteBuf buf) {
        return new S2CSetLocalVolumePacket(buf.readInt());
    }

    public static void handle(S2CSetLocalVolumePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    Minecraft mc = Minecraft.getInstance();
                    double target = Math.max(0.0, Math.min(1.0, msg.volume / 100.0));
                    mc.options.getSoundSourceOptionInstance(SoundSource.MUSIC).set(target);
                    mc.options.save();
                    ClientAudioEngine.updateVolume();
                })
        );
        ctx.get().setPacketHandled(true);
    }
}