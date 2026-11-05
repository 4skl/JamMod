package com.skl.jammod.network;

import com.skl.jammod.JamMod;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworking {

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        // C2S
        registrar.playToServer(
                C2SJamActionPacket.TYPE,
                C2SJamActionPacket.STREAM_CODEC,
                C2SJamActionPacket::handle
        );

        // S2C
        registrar.playToClient(
                S2CSyncJamPacket.TYPE,
                S2CSyncJamPacket.STREAM_CODEC,
                S2CSyncJamPacket::handle
        );

        registrar.playToClient(
                S2CSyncJamListPacket.TYPE,
                S2CSyncJamListPacket.STREAM_CODEC,
                S2CSyncJamListPacket::handle
        );

        registrar.playToClient(
                S2CSetLocalVolumePacket.TYPE,
                S2CSetLocalVolumePacket.STREAM_CODEC,
                S2CSetLocalVolumePacket::handle
        );

        registrar.playToClient(
                S2CSetOverlayConfigPacket.TYPE,
                S2CSetOverlayConfigPacket.STREAM_CODEC,
                S2CSetOverlayConfigPacket::handle
        );
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }
}