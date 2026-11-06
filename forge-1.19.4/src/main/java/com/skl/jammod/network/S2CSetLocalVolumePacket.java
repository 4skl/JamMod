package com.skl.jammod.network;

import com.skl.jammod.client.ClientAudioEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSetLocalVolumePacket {
    public final int volume;

    public S2CSetLocalVolumePacket(int volume) {
        this.volume = volume;
    }

    public S2CSetLocalVolumePacket(FriendlyByteBuf buf) {
        this.volume = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.volume);
    }

    public static S2CSetLocalVolumePacket decode(FriendlyByteBuf buf) {
        return new S2CSetLocalVolumePacket(buf);
    }

    public static void handle(S2CSetLocalVolumePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            double target = Math.max(0.0, Math.min(1.0, msg.volume / 100.0));
            mc.options.getSoundSourceOptionInstance(SoundSource.MUSIC).set(target);
            mc.options.save();
            ClientAudioEngine.updateVolume();
        });
        ctx.setPacketHandled(true);
    }
}