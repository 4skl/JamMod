package com.skl.jammod.network;

import com.skl.jammod.client.ClientJamManager;
import com.skl.jammod.session.JamEditPolicy;
import com.skl.jammod.session.JamTrack;
import com.skl.jammod.session.SyncedJam;
import com.skl.jammod.session.SyncedJam.RepeatMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class S2CSyncJamPacket {
    private final String jamId, jamName, ownerName, currentTrackUrl, currentTrackTitle;
    private final boolean isPrivate, isPlaying, canEdit, isOwner, isAdmin, shuffle;
    private final JamEditPolicy editPolicy;
    private final RepeatMode repeatMode;
    private final int trackIndex, listenerCount;
    private final long startTimestamp;
    private final List<JamTrack> tracks;
    private final List<SyncedJam.MemberInfo> members;

    public S2CSyncJamPacket(String jamId, String jamName, String ownerName, boolean isPrivate,
                            JamEditPolicy editPolicy, RepeatMode repeatMode, boolean shuffle,
                            String currentTrackUrl, String currentTrackTitle, int trackIndex,
                            long startTimestamp, boolean isPlaying, List<JamTrack> tracks,
                            List<SyncedJam.MemberInfo> members,
                            int listenerCount, boolean canEdit, boolean isOwner, boolean isAdmin) {
        this.jamId = jamId; this.jamName = jamName; this.ownerName = ownerName; this.isPrivate = isPrivate;
        this.editPolicy = editPolicy != null ? editPolicy : JamEditPolicy.OPEN;
        this.repeatMode = repeatMode != null ? repeatMode : RepeatMode.OFF;
        this.shuffle = shuffle; this.currentTrackUrl = currentTrackUrl; this.currentTrackTitle = currentTrackTitle;
        this.trackIndex = trackIndex; this.startTimestamp = startTimestamp; this.isPlaying = isPlaying;
        this.tracks = tracks != null ? tracks : new ArrayList<>();
        this.members = members != null ? members : new ArrayList<>();
        this.listenerCount = listenerCount; this.canEdit = canEdit; this.isOwner = isOwner; this.isAdmin = isAdmin;
    }

    public static void encode(S2CSyncJamPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.jamId); buf.writeUtf(msg.jamName); buf.writeUtf(msg.ownerName);
        buf.writeBoolean(msg.isPrivate); buf.writeEnum(msg.editPolicy); buf.writeEnum(msg.repeatMode);
        buf.writeBoolean(msg.shuffle); buf.writeUtf(msg.currentTrackUrl); buf.writeUtf(msg.currentTrackTitle);
        buf.writeInt(msg.trackIndex); buf.writeLong(msg.startTimestamp); buf.writeBoolean(msg.isPlaying);

        buf.writeInt(msg.tracks.size());
        for (JamTrack t : msg.tracks) {
            buf.writeUtf(t.getUrl()); buf.writeUtf(t.getTitle()); buf.writeLong(t.getDurationMs());
        }

        buf.writeInt(msg.members.size());
        for (SyncedJam.MemberInfo m : msg.members) {
            buf.writeUUID(m.uuid());
            buf.writeUtf(m.name());
            buf.writeEnum(m.role());
        }

        buf.writeInt(msg.listenerCount); buf.writeBoolean(msg.canEdit); buf.writeBoolean(msg.isOwner); buf.writeBoolean(msg.isAdmin);
    }

    public static S2CSyncJamPacket decode(FriendlyByteBuf buf) {
        String id = buf.readUtf(), name = buf.readUtf(), owner = buf.readUtf();
        boolean priv = buf.readBoolean(); JamEditPolicy pol = buf.readEnum(JamEditPolicy.class);
        RepeatMode rep = buf.readEnum(RepeatMode.class); boolean shuf = buf.readBoolean();
        String tUrl = buf.readUtf(), tTitle = buf.readUtf();
        int idx = buf.readInt(); long start = buf.readLong(); boolean play = buf.readBoolean();

        int size = buf.readInt(); List<JamTrack> trks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) trks.add(new JamTrack(buf.readUtf(), buf.readUtf(), buf.readLong()));

        int memSize = buf.readInt(); List<SyncedJam.MemberInfo> mems = new ArrayList<>(memSize);
        for (int i = 0; i < memSize; i++) mems.add(new SyncedJam.MemberInfo(buf.readUUID(), buf.readUtf(), buf.readEnum(SyncedJam.Role.class)));

        return new S2CSyncJamPacket(id, name, owner, priv, pol, rep, shuf, tUrl, tTitle, idx, start, play, trks, mems,
                buf.readInt(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(S2CSyncJamPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientJamManager.updateSession(msg.jamId, msg.jamName, msg.ownerName, msg.isPrivate, msg.editPolicy,
                        msg.repeatMode, msg.shuffle, msg.currentTrackUrl, msg.currentTrackTitle, msg.trackIndex,
                        msg.startTimestamp, msg.isPlaying, msg.tracks, msg.members, msg.listenerCount, msg.canEdit, msg.isOwner, msg.isAdmin)
        ));
        ctx.get().setPacketHandled(true);
    }
}