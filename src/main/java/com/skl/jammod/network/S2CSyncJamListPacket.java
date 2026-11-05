package com.skl.jammod.network;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.skl.jammod.client.ClientJamManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class S2CSyncJamListPacket {
    public record JamSummary(String id, String name, UUID ownerId, String ownerName,
                             boolean isPrivate, int trackCount, int listeners,
                             boolean playing, String currentTrack) {}

    private static final Gson GSON = new Gson();
    private final String json;

    public S2CSyncJamListPacket(List<JamSummary> summaries) {
        this.json = GSON.toJson(summaries);
    }

    public S2CSyncJamListPacket(String json) {
        this.json = json;
    }

    public static void encode(S2CSyncJamListPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.json, 262144);
    }

    public static S2CSyncJamListPacket decode(FriendlyByteBuf buf) {
        return new S2CSyncJamListPacket(buf.readUtf(262144));
    }

    public static void handle(S2CSyncJamListPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    List<JamSummary> list = GSON.fromJson(msg.json, new TypeToken<ArrayList<JamSummary>>() {}.getType());
                    ClientJamManager.setDirectory(list != null ? list : new ArrayList<>());
                })
        );
        ctx.get().setPacketHandled(true);
    }
}