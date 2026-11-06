package com.skl.jammod.network;

import com.skl.jammod.JamMod;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

public class ModNetworking {

    public static void register(final RegisterPayloadHandlerEvent event) {
        JamMod.LOGGER.info("[JamMod] Registering 1.20.4 network payloads...");

        final IPayloadRegistrar registrar = event.registrar(JamMod.MODID)
                .versioned("1.0.0")
                .optional();

        // C2S (Serverbound)
        registrar.play(
                C2SJamActionPacket.ID,
                C2SJamActionPacket::new,
                handler -> handler.server(C2SJamActionPacket::handle)
        );

        // S2C (Clientbound)
        registrar.play(
                S2CSyncJamPacket.ID,
                S2CSyncJamPacket::new,
                handler -> handler.client(S2CSyncJamPacket::handle)
        );

        registrar.play(
                S2CSyncJamListPacket.ID,
                S2CSyncJamListPacket::new,
                handler -> handler.client(S2CSyncJamListPacket::handle)
        );

        registrar.play(
                S2CSetLocalVolumePacket.ID,
                S2CSetLocalVolumePacket::new,
                handler -> handler.client(S2CSetLocalVolumePacket::handle)
        );

        registrar.play(
                S2CSetOverlayConfigPacket.ID,
                S2CSetOverlayConfigPacket::new,
                handler -> handler.client(S2CSetOverlayConfigPacket::handle)
        );
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        PacketDistributor.PLAYER.with(player).send(packet);
    }

    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.SERVER.noArg().send(packet);
    }
}