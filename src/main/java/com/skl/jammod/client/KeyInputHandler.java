package com.skl.jammod.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.skl.jammod.JamMod;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

//? if >1.20.1 {
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
*///?} else {
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyConflictContext;
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

//? if <=1.18.2 {
/*import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
*///?} else if <=1.20.1 {
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
 //?}

@EventBusSubscriber(modid = JamMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class KeyInputHandler {
    public static final KeyMapping OPEN_JAM_MENU = new KeyMapping(
            "key.jammod.open_menu",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            "category.jammod"
    );

    //? if <=1.18.2 {
    /*@SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ClientRegistry.registerKeyBinding(OPEN_JAM_MENU));
    }
    *///?} else {
    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_JAM_MENU);
    }
    //?}
}