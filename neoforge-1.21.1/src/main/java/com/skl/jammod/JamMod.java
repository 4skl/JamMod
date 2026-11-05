package com.skl.jammod;

import com.mojang.logging.LogUtils;
import com.skl.jammod.network.ModNetworking;
import com.skl.jammod.session.ServerJamRegistry;
import com.skl.jammod.session.SyncedJam;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(JamMod.MODID)
public class JamMod {
    public static final String MODID = "jammod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public JamMod(IEventBus modEventBus) {
        modEventBus.addListener(ModNetworking::register);
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        ServerJamRegistry.init();
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerJamRegistry.syncDirectoryToPlayer(player);
            SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
            if (jam != null) {
                ServerJamRegistry.broadcastJamState(player.getServer(), jam);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerJamRegistry.leaveCurrentJam(event.getEntity().getUUID());
    }
}