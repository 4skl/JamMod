package com.skl.jammod.client;

import com.skl.jammod.JamMod;
import com.skl.jammod.gui.JamScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = JamMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientAudioBridge {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        while (KeyInputHandler.OPEN_JAM_MENU.consumeClick()) {
            if (mc.screen == null) {
                mc.setScreen(new JamScreen());
            }
        }
    }

    public static void playTrack(String trackUrl, long startTimestamp) {
        long offsetMs = Math.max(0, System.currentTimeMillis() - startTimestamp);
        MusicSuppressor.stopVanillaMusic();
        ClientAudioEngine.play(trackUrl, offsetMs);
    }

    public static void stopTrack() {
        ClientAudioEngine.stop();
        MusicSuppressor.resumeVanillaMusic();
    }
}