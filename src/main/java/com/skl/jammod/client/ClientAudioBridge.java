package com.skl.jammod.client;

import com.skl.jammod.JamMod;
import com.skl.jammod.gui.JamScreen;
import net.minecraft.client.Minecraft;

//? if >1.20.1 {
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
*///?} else {
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
//?}

//? if >=1.21 {
/*import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
*///?} else if >1.20.1 {
/*import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.neoforge.event.TickEvent;
*///?} else {
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
 //?}

@EventBusSubscriber(modid = JamMod.MODID, value = Dist.CLIENT)
public class ClientAudioBridge {

    //? if >=1.21 {
    /*@SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        while (KeyInputHandler.OPEN_JAM_MENU.consumeClick()) {
            if (mc.screen == null) {
                mc.setScreen(new JamScreen());
            }
        }
    }
    *///?} else {
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
    //?}

    //? if <=1.18.2 {
    /*@SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        ClientJamManager.reset();
    }
    *///?} else {
    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientJamManager.reset();
    }
    //?}

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