package com.skl.jammod.client;

import com.skl.jammod.JamMod;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;

//? if >1.20.1 {
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
*///?} else {
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
//?}

//? if >=1.21 {
/*import net.neoforged.fml.common.EventBusSubscriber;
 *///?} else if >1.20.1 {
/*import net.neoforged.fml.common.Mod.EventBusSubscriber;
*///?} else {
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
 //?}

@EventBusSubscriber(modid = JamMod.MODID, value = Dist.CLIENT)
public class MusicSuppressor {
    private static boolean suppressVanillaMusic = false;

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        if (suppressVanillaMusic && event.getSound() != null && event.getSound().getSource() == SoundSource.MUSIC) {
            event.setSound(null);
        }
    }

    public static void stopVanillaMusic() {
        suppressVanillaMusic = true;
        Minecraft.getInstance().getSoundManager().stop(null, SoundSource.MUSIC);
    }

    public static void resumeVanillaMusic() {
        suppressVanillaMusic = false;
    }
}