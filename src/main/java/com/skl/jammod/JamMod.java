package com.skl.jammod;

import com.skl.jammod.network.ModNetworking;
import com.skl.jammod.session.ServerJamRegistry;
import com.skl.jammod.session.SyncedJam;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//? if >1.20.1 {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
*///?} else {
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
//?}

@Mod(JamMod.MODID)
public class JamMod {
    public static final String MODID = "jammod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    //? if >1.20.1 {
    /*public JamMod(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ModNetworking::registerPayloads);
        NeoForge.EVENT_BUS.register(this);
    }
    *///?} else {
    public JamMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }
    //?}

    private void commonSetup(final FMLCommonSetupEvent event) {
        //? if <=1.20.1 {
        event.enqueueWork(ModNetworking::register);
         //?}
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        ServerJamRegistry.init();
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerJamRegistry.broadcastDirectory(player.getServer());
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SyncedJam old = ServerJamRegistry.leaveCurrentJam(player.getUUID());
            if (old != null) {
                ServerJamRegistry.broadcastJamState(player.getServer(), old);
            }
            ServerJamRegistry.broadcastDirectory(player.getServer());
        }
    }
}