package com.skl.jammod.network;

import com.skl.jammod.JamMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

//? if >1.20.1 {
/*import net.neoforged.neoforge.network.PacketDistributor;
*///?} else {
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
//?}

public class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";

    //? if >1.20.1 {
    /*public static final ChannelWrapper CHANNEL = new ChannelWrapper();
    public static class ChannelWrapper {
        public void sendToServer(Object msg) {
            ModNetworking.sendToServer(msg);
        }
    }
    *///?} else {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(JamMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    //?}

    // Forge message registration
    //? if <=1.20.1 {
    public static void register() {
        int id = 0;

        CHANNEL.messageBuilder(C2SJamActionPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SJamActionPacket::encode)
                .decoder(C2SJamActionPacket::decode)
                //? if >=1.19 {
                /*.consumerMainThread(C2SJamActionPacket::handle)
                *///?} else {
                .consumer(C2SJamActionPacket::handle)
                //?}
                .add();

        CHANNEL.messageBuilder(S2CSyncJamPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CSyncJamPacket::encode)
                .decoder(S2CSyncJamPacket::decode)
                //? if >=1.19 {
                /*.consumerMainThread(S2CSyncJamPacket::handle)
                *///?} else {
                .consumer(S2CSyncJamPacket::handle)
                //?}
                .add();

        CHANNEL.messageBuilder(S2CSyncJamListPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CSyncJamListPacket::encode)
                .decoder(S2CSyncJamListPacket::decode)
                //? if >=1.19 {
                /*.consumerMainThread(S2CSyncJamListPacket::handle)
                *///?} else {
                .consumer(S2CSyncJamListPacket::handle)
                //?}
                .add();

        CHANNEL.messageBuilder(S2CSetLocalVolumePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CSetLocalVolumePacket::encode)
                .decoder(S2CSetLocalVolumePacket::decode)
                //? if >=1.19 {
                /*.consumerMainThread(S2CSetLocalVolumePacket::handle)
                *///?} else {
                .consumer(S2CSetLocalVolumePacket::handle)
                //?}
                .add();

        CHANNEL.messageBuilder(S2CSetOverlayConfigPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CSetOverlayConfigPacket::encode)
                .decoder(S2CSetOverlayConfigPacket::decode)
                //? if >=1.19 {
                /*.consumerMainThread(S2CSetOverlayConfigPacket::handle)
                *///?} else {
                .consumer(S2CSetOverlayConfigPacket::handle)
                //?}
                .add();
    }
    //?}

    // NeoForge payload registration
    //? if >=1.21 {
    /*public static void registerPayloads(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(C2SJamActionPacket.TYPE, C2SJamActionPacket.STREAM_CODEC, C2SJamActionPacket::handle);
        registrar.playToClient(S2CSyncJamPacket.TYPE, S2CSyncJamPacket.STREAM_CODEC, S2CSyncJamPacket::handle);
        registrar.playToClient(S2CSyncJamListPacket.TYPE, S2CSyncJamListPacket.STREAM_CODEC, S2CSyncJamListPacket::handle);
        registrar.playToClient(S2CSetLocalVolumePacket.TYPE, S2CSetLocalVolumePacket.STREAM_CODEC, S2CSetLocalVolumePacket::handle);
        registrar.playToClient(S2CSetOverlayConfigPacket.TYPE, S2CSetOverlayConfigPacket.STREAM_CODEC, S2CSetOverlayConfigPacket::handle);
    }
    *///?} else if >1.20.1 {
    /*public static void registerPayloads(net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent event) {
        var registrar = event.registrar(JamMod.MODID);
        registrar.play(C2SJamActionPacket.ID, C2SJamActionPacket::decode, handler -> handler.server(C2SJamActionPacket::handle));
        registrar.play(S2CSyncJamPacket.ID, S2CSyncJamPacket::decode, handler -> handler.client(S2CSyncJamPacket::handle));
        registrar.play(S2CSyncJamListPacket.ID, S2CSyncJamListPacket::decode, handler -> handler.client(S2CSyncJamListPacket::handle));
        registrar.play(S2CSetLocalVolumePacket.ID, S2CSetLocalVolumePacket::decode, handler -> handler.client(S2CSetLocalVolumePacket::handle));
        registrar.play(S2CSetOverlayConfigPacket.ID, S2CSetOverlayConfigPacket::decode, handler -> handler.client(S2CSetOverlayConfigPacket::handle));
    }
    *///?}

    public static void sendToServer(Object msg) {
        //? if >=1.21 {
        /*PacketDistributor.sendToServer((net.minecraft.network.protocol.common.custom.CustomPacketPayload) msg);
         *///?} else if >1.20.1 {
        /*PacketDistributor.SERVER.noArg().send((net.minecraft.network.protocol.common.custom.CustomPacketPayload) msg);
        *///?} else {
        CHANNEL.sendToServer(msg);
         //?}
    }

    public static void sendToPlayer(ServerPlayer player, Object msg) {
        //? if >=1.21 {
        /*PacketDistributor.sendToPlayer(player, (net.minecraft.network.protocol.common.custom.CustomPacketPayload) msg);
         *///?} else if >1.20.1 {
        /*PacketDistributor.PLAYER.with(player).send((net.minecraft.network.protocol.common.custom.CustomPacketPayload) msg);
        *///?} else {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
         //?}
    }
}