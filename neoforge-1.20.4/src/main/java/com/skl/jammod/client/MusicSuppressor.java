package com.skl.jammod.client;

import com.skl.jammod.JamMod;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

@Mod.EventBusSubscriber(modid = JamMod.MODID, value = Dist.CLIENT)
public class MusicSuppressor {
    private static boolean suppress = false;

    public static void stopVanillaMusic() {
        suppress = true;
        Minecraft.getInstance().getSoundManager().stop(null, SoundSource.MUSIC);
    }

    public static void resumeVanillaMusic() {
        suppress = false;
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        if (suppress && event.getSound() != null && event.getSound().getSource() == SoundSource.MUSIC) {
            event.setSound(null);
        }
    }
}