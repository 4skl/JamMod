package com.skl.jammod.client;

import com.skl.jammod.JamMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = JamMod.MODID, value = Dist.CLIENT)
public class ClientAudioBridge {

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientJamManager.reset();
    }
}