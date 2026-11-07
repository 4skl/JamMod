package com.skl.jammod.network;

import com.skl.jammod.JamMod;
import com.skl.jammod.client.ClientJamManager;
import com.skl.jammod.session.JamEditPolicy;
import com.skl.jammod.session.JamTrack;
import com.skl.jammod.session.SyncedJam;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

//? if >=1.21 {
/*import net.neoforged.neoforge.network.handling.IPayloadContext;
 *///?} else if >1.20.1 {
/*import net.neoforged.neoforge.network.handling.PlayPayloadContext;
*///?} else {
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
//?}

public class S2CSyncJamPacket
//? if >1.20.1 {
        /*implements net.minecraft.network.protocol.common.custom.CustomPacketPayload
*///?}
{
    //? if >=1.21 {
    /*public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<S2CSyncJamPacket> TYPE =
            new net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JamMod.MODID, "sync_jam"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, S2CSyncJamPacket> STREAM_CODEC =
            net.minecraft.network.codec.StreamCodec.ofMember(S2CSyncJamPacket::write, S2CSyncJamPacket::decode);

    @Override
    public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
        return TYPE;
    }
    *///?} else if >1.20.1 {
    /*public static final ResourceLocation ID = new ResourceLocation(JamMod.MODID, "sync_jam");

    @Override
    public ResourceLocation id() {
        return ID;
    }
    *///?}

    //? if >1.20.1 {
    /*@Override
    public void write(FriendlyByteBuf buf) {
        encode(this, buf);
    }
    *///?}

    public final String jamId;
    public final String jamName;
    public final String ownerName;
    public final boolean isPrivate;
    public final JamEditPolicy editPolicy;
    public final SyncedJam.RepeatMode repeatMode;
    public final boolean isShuffle;
    public final String currentTrackTitle;
    public final String currentTrackUrl;
    public final long durationMs;
    public final long startTimestamp;
    public final boolean isPlaying;
    public final List<JamTrack> tracks;
    public final List<SyncedJam.MemberInfo> members;
    public final int currentTrackIndex;
    public final boolean isOwner;
    public final boolean canEdit;
    public final boolean canManage;

    public S2CSyncJamPacket(String jamId, String jamName, String ownerName, boolean isPrivate,
                            JamEditPolicy editPolicy, SyncedJam.RepeatMode repeatMode, boolean isShuffle,
                            String currentTrackTitle, String currentTrackUrl, long durationMs,
                            long startTimestamp, boolean isPlaying,
                            List<JamTrack> tracks, List<SyncedJam.MemberInfo> members,
                            int currentTrackIndex, boolean isOwner, boolean canEdit, boolean canManage) {
        this.jamId = jamId;
        this.jamName = jamName;
        this.ownerName = ownerName;
        this.isPrivate = isPrivate;
        this.editPolicy = editPolicy;
        this.repeatMode = repeatMode;
        this.isShuffle = isShuffle;
        this.currentTrackTitle = currentTrackTitle;
        this.currentTrackUrl = currentTrackUrl;
        this.durationMs = durationMs;
        this.startTimestamp = startTimestamp;
        this.isPlaying = isPlaying;
        this.tracks = tracks;
        this.members = members;
        this.currentTrackIndex = currentTrackIndex;
        this.isOwner = isOwner;
        this.canEdit = canEdit;
        this.canManage = canManage;
    }

    public static void encode(S2CSyncJamPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.jamId);
        buf.writeUtf(msg.jamName);
        buf.writeUtf(msg.ownerName);
        buf.writeBoolean(msg.isPrivate);
        buf.writeEnum(msg.editPolicy);
        buf.writeEnum(msg.repeatMode);
        buf.writeBoolean(msg.isShuffle);
        buf.writeUtf(msg.currentTrackTitle);
        buf.writeUtf(msg.currentTrackUrl);
        buf.writeLong(msg.durationMs);
        buf.writeLong(msg.startTimestamp);
        buf.writeBoolean(msg.isPlaying);

        buf.writeVarInt(msg.tracks.size());
        for (JamTrack t : msg.tracks) {
            buf.writeUtf(t.getTitle());
            buf.writeUtf(t.getUrl());
            buf.writeLong(t.getDurationMs());
        }

        buf.writeVarInt(msg.members.size());
        for (SyncedJam.MemberInfo m : msg.members) {
            buf.writeUUID(m.uuid());
            buf.writeUtf(m.name());
            buf.writeEnum(m.role());
        }

        buf.writeVarInt(msg.currentTrackIndex);
        buf.writeBoolean(msg.isOwner);
        buf.writeBoolean(msg.canEdit);
        buf.writeBoolean(msg.canManage);
    }

    public static S2CSyncJamPacket decode(FriendlyByteBuf buf) {
        String jamId = buf.readUtf();
        String jamName = buf.readUtf();
        String ownerName = buf.readUtf();
        boolean isPrivate = buf.readBoolean();
        JamEditPolicy editPolicy = buf.readEnum(JamEditPolicy.class);
        SyncedJam.RepeatMode repeatMode = buf.readEnum(SyncedJam.RepeatMode.class);
        boolean isShuffle = buf.readBoolean();
        String currentTrackTitle = buf.readUtf();
        String currentTrackUrl = buf.readUtf();
        long durationMs = buf.readLong();
        long startTimestamp = buf.readLong();
        boolean isPlaying = buf.readBoolean();

        int trackCount = buf.readVarInt();
        List<JamTrack> tracks = new ArrayList<>(trackCount);
        for (int i = 0; i < trackCount; i++) {
            tracks.add(new JamTrack(buf.readUtf(), buf.readUtf(), buf.readLong()));
        }

        int memberCount = buf.readVarInt();
        List<SyncedJam.MemberInfo> members = new ArrayList<>(memberCount);
        for (int i = 0; i < memberCount; i++) {
            members.add(new SyncedJam.MemberInfo(buf.readUUID(), buf.readUtf(), buf.readEnum(SyncedJam.Role.class)));
        }

        int currentTrackIndex = buf.readVarInt();
        boolean isOwner = buf.readBoolean();
        boolean canEdit = buf.readBoolean();
        boolean canManage = buf.readBoolean();

        return new S2CSyncJamPacket(jamId, jamName, ownerName, isPrivate, editPolicy, repeatMode,
                isShuffle, currentTrackTitle, currentTrackUrl, durationMs, startTimestamp,
                isPlaying, tracks, members, currentTrackIndex, isOwner, canEdit, canManage);
    }

    private static void applySync(S2CSyncJamPacket msg) {
        ClientJamManager.updateSession(
                msg.jamId,
                msg.jamName,
                msg.ownerName,
                msg.isPrivate,
                msg.editPolicy,
                msg.repeatMode,
                msg.isShuffle,
                msg.currentTrackUrl,
                msg.currentTrackTitle,
                msg.currentTrackIndex,
                msg.startTimestamp,
                msg.isPlaying,
                msg.tracks,
                msg.members,
                msg.members.size(),
                msg.canEdit,
                msg.isOwner,
                msg.canManage
        );
    }

    //? if >=1.21 {
    /*public static void handle(S2CSyncJamPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> applySync(msg));
    }
    *///?} else if >1.20.1 {
    /*public static void handle(S2CSyncJamPacket msg, PlayPayloadContext ctx) {
        ctx.workHandler().execute(() -> applySync(msg));
    }
    *///?} else {
    public static void handle(S2CSyncJamPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> applySync(msg)));
        ctx.get().setPacketHandled(true);
    }
    //?}
}