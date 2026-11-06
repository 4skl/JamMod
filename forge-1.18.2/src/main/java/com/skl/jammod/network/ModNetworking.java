package com.skl.jammod.network;

import com.skl.jammod.JamMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {
    private static final String PROTOCOL_VERSION = "4";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(JamMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;
    private static int id() { return packetId++; }

    public static void register() {
        CHANNEL.messageBuilder(C2SJamActionPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SJamActionPacket::encode)
                .decoder(C2SJamActionPacket::decode)
                .consumer(C2SJamActionPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CSyncJamPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CSyncJamPacket::encode)
                .decoder(S2CSyncJamPacket::decode)
                .consumer(S2CSyncJamPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CSyncJamListPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CSyncJamListPacket::encode)
                .decoder(S2CSyncJamListPacket::decode)
                .consumer(S2CSyncJamListPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CSetLocalVolumePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CSetLocalVolumePacket::encode)
                .decoder(S2CSetLocalVolumePacket::decode)
                .consumer(S2CSetLocalVolumePacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CSetOverlayConfigPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CSetOverlayConfigPacket::encode)
                .decoder(S2CSetOverlayConfigPacket::decode)
                .consumer(S2CSetOverlayConfigPacket::handle)
                .add();
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}