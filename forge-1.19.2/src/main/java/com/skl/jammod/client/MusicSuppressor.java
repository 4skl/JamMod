package com.skl.jammod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MusicSuppressor {

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        if (ClientJamManager.isPlaying() && event.getSound() != null && event.getSound().getSource() == SoundSource.MUSIC) {
            event.setSound(null);
        }
    }

    public static void stopVanillaMusic() {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.getMusicManager() != null) {
                mc.getMusicManager().stopPlaying();
            }
        });
    }

    public static void resumeVanillaMusic() {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            MusicManager musicManager = mc.getMusicManager();
            if (musicManager != null) {
                Music music = mc.getSituationalMusic();
                if (music != null) {
                    musicManager.stopPlaying();
                    musicManager.startPlaying(music);
                }
            }
        });
    }
}