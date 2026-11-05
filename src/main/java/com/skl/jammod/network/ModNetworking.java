package com.skl.jammod.network;

import com.skl.jammod.JamMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(JamMod.MODID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void register() {
        CHANNEL.messageBuilder(C2SJamActionPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SJamActionPacket::encode)
                .decoder(C2SJamActionPacket::decode)
                .consumerMainThread(C2SJamActionPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CSyncJamPacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CSyncJamPacket::encode)
                .decoder(S2CSyncJamPacket::decode)
                .consumerMainThread(S2CSyncJamPacket::handle)
                .add();
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToAll(Object packet) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }
}