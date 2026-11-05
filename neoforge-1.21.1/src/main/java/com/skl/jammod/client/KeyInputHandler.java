package com.skl.jammod.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.skl.jammod.JamMod;
import com.skl.jammod.gui.JamScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyInputHandler {
    public static final KeyMapping OPEN_JAM_SCREEN = new KeyMapping(
            "key.jammod.open_gui",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            "key.categories.jammod"
    );

    @EventBusSubscriber(modid = JamMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(OPEN_JAM_SCREEN);
        }
    }

    @EventBusSubscriber(modid = JamMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
    public static class GameBusEvents {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                while (OPEN_JAM_SCREEN.consumeClick()) {
                    mc.setScreen(new JamScreen());
                }
            }
        }
    }
}