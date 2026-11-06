package com.skl.jammod.network;

import com.skl.jammod.JamMod;
import com.skl.jammod.client.ClientJamManager;
import com.skl.jammod.session.JamEditPolicy;
import com.skl.jammod.session.JamTrack;
import com.skl.jammod.session.SyncedJam;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class S2CSyncJamPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(JamMod.MODID + ":sync_jam");

    private final String jamId, jamName, ownerName, currentTrackUrl, currentTrackTitle;
    private final boolean isPrivate, isPlaying, canEdit, isOwner, isAdmin, shuffle;
    private final JamEditPolicy editPolicy;
    private final SyncedJam.RepeatMode repeatMode;
    private final int trackIndex, listenerCount;
    private final long startTimestamp;
    private final List<JamTrack> tracks;
    private final List<SyncedJam.MemberInfo> members;

    public S2CSyncJamPacket(String jamId, String jamName, String ownerName, boolean isPrivate,
                            JamEditPolicy editPolicy, SyncedJam.RepeatMode repeatMode, boolean shuffle,
                            String currentTrackUrl, String currentTrackTitle, int trackIndex,
                            long startTimestamp, boolean isPlaying, List<JamTrack> tracks,
                            List<SyncedJam.MemberInfo> members,
                            int listenerCount, boolean canEdit, boolean isOwner, boolean isAdmin) {
        this.jamId = jamId; this.jamName = jamName; this.ownerName = ownerName; this.isPrivate = isPrivate;
        this.editPolicy = editPolicy != null ? editPolicy : JamEditPolicy.OPEN;
        this.repeatMode = repeatMode != null ? repeatMode : SyncedJam.RepeatMode.OFF;
        this.shuffle = shuffle; this.currentTrackUrl = currentTrackUrl; this.currentTrackTitle = currentTrackTitle;
        this.trackIndex = trackIndex; this.startTimestamp = startTimestamp; this.isPlaying = isPlaying;
        this.tracks = tracks != null ? tracks : new ArrayList<>();
        this.members = members != null ? members : new ArrayList<>();
        this.listenerCount = listenerCount; this.canEdit = canEdit; this.isOwner = isOwner; this.isAdmin = isAdmin;
    }

    public S2CSyncJamPacket(FriendlyByteBuf buf) {
        this.jamId = buf.readUtf();
        this.jamName = buf.readUtf();
        this.ownerName = buf.readUtf();
        this.isPrivate = buf.readBoolean();
        this.editPolicy = buf.readEnum(JamEditPolicy.class);
        this.repeatMode = buf.readEnum(SyncedJam.RepeatMode.class);
        this.shuffle = buf.readBoolean();
        this.currentTrackUrl = buf.readUtf();
        this.currentTrackTitle = buf.readUtf();
        this.trackIndex = buf.readInt();
        this.startTimestamp = buf.readLong();
        this.isPlaying = buf.readBoolean();

        int size = buf.readInt();
        this.tracks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.tracks.add(new JamTrack(buf.readUtf(), buf.readUtf(), buf.readLong()));
        }

        int memSize = buf.readInt();
        this.members = new ArrayList<>(memSize);
        for (int i = 0; i < memSize; i++) {
            this.members.add(new SyncedJam.MemberInfo(buf.readUUID(), buf.readUtf(), buf.readEnum(SyncedJam.Role.class)));
        }

        this.listenerCount = buf.readInt();
        this.canEdit = buf.readBoolean();
        this.isOwner = buf.readBoolean();
        this.isAdmin = buf.readBoolean();
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.jamId);
        buf.writeUtf(this.jamName);
        buf.writeUtf(this.ownerName);
        buf.writeBoolean(this.isPrivate);
        buf.writeEnum(this.editPolicy);
        buf.writeEnum(this.repeatMode);
        buf.writeBoolean(this.shuffle);
        buf.writeUtf(this.currentTrackUrl);
        buf.writeUtf(this.currentTrackTitle);
        buf.writeInt(this.trackIndex);
        buf.writeLong(this.startTimestamp);
        buf.writeBoolean(this.isPlaying);

        buf.writeInt(this.tracks.size());
        for (JamTrack t : this.tracks) {
            buf.writeUtf(t.getUrl());
            buf.writeUtf(t.getTitle());
            buf.writeLong(t.getDurationMs());
        }

        buf.writeInt(this.members.size());
        for (SyncedJam.MemberInfo m : this.members) {
            buf.writeUUID(m.uuid());
            buf.writeUtf(m.name());
            buf.writeEnum(m.role());
        }

        buf.writeInt(this.listenerCount);
        buf.writeBoolean(this.canEdit);
        buf.writeBoolean(this.isOwner);
        buf.writeBoolean(this.isAdmin);
    }

    public static void handle(S2CSyncJamPacket msg, IPayloadContext ctx) {
        ctx.workHandler().execute(() ->
                ClientJamManager.updateSession(msg.jamId, msg.jamName, msg.ownerName, msg.isPrivate, msg.editPolicy,
                        msg.repeatMode, msg.shuffle, msg.currentTrackUrl, msg.currentTrackTitle, msg.trackIndex,
                        msg.startTimestamp, msg.isPlaying, msg.tracks, msg.members, msg.listenerCount, msg.canEdit, msg.isOwner, msg.isAdmin)
        );
    }
}