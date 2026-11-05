package com.skl.jammod.network;

import com.skl.jammod.JamMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {
    private static final String PROTOCOL_VERSION = "2";
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
                .consumerMainThread(C2SJamActionPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CSyncJamPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CSyncJamPacket::encode)
                .decoder(S2CSyncJamPacket::decode)
                .consumerMainThread(S2CSyncJamPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CSyncJamListPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CSyncJamListPacket::encode)
                .decoder(S2CSyncJamListPacket::decode)
                .consumerMainThread(S2CSyncJamListPacket::handle)
                .add();
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}