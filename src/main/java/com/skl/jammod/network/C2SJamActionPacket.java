package com.skl.jammod.network;

import com.skl.jammod.JamMod;
import com.skl.jammod.session.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

//? if <=1.18.2 {
/*import net.minecraft.network.chat.TextComponent;
 *///?}

//? if >=1.21 {
/*import net.neoforged.neoforge.network.handling.IPayloadContext;
 *///?} else if >1.20.1 {
/*import net.neoforged.neoforge.network.handling.PlayPayloadContext;
*///?} else {
import net.minecraftforge.network.NetworkEvent;
 //?}

public class C2SJamActionPacket
//? if >1.20.1 {
        /*implements net.minecraft.network.protocol.common.custom.CustomPacketPayload
*///?}
{
    //? if >=1.21 {
    /*public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<C2SJamActionPacket> TYPE =
            new net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JamMod.MODID, "jam_action"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, C2SJamActionPacket> STREAM_CODEC =
            net.minecraft.network.codec.StreamCodec.ofMember(C2SJamActionPacket::write, C2SJamActionPacket::decode);

    @Override
    public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
        return TYPE;
    }
    *///?} else if >1.20.1 {
    /*public static final ResourceLocation ID = new ResourceLocation(JamMod.MODID, "jam_action");

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

    public enum Action {
        REQUEST_SYNC, PREVIOUS, TOGGLE_PLAY, SKIP, CYCLE_REPEAT, TOGGLE_SHUFFLE,
        LEAVE_JAM, ADD_TRACK, TOGGLE_PRIVACY, SET_POLICY, DELETE_JAM,
        ADD_MEMBER_BY_NAME, CREATE_JAM, SEEK, JUMP_TRACK, MOVE_TRACK,
        REMOVE_TRACK, CYCLE_MEMBER_ROLE, REMOVE_MEMBER, INVITE_PLAYER,
        JOIN_JAM, UPDATE_METADATA
    }

    public final Action action;
    public final String targetId;
    public final String payload;
    public final boolean flag;
    public final int index;
    public final int extraInt;

    public C2SJamActionPacket(Action action) {
        this(action, "", "", false, 0, 0);
    }

    public C2SJamActionPacket(Action action, String targetId) {
        this(action, targetId, "", false, 0, 0);
    }

    public C2SJamActionPacket(Action action, String targetId, String payload) {
        this(action, targetId, payload, false, 0, 0);
    }

    public C2SJamActionPacket(Action action, String targetId, String payload, boolean flag, int index) {
        this(action, targetId, payload, flag, index, 0);
    }

    public C2SJamActionPacket(Action action, String targetId, String payload, boolean flag, int index, int extraInt) {
        this.action = action;
        this.targetId = targetId;
        this.payload = payload;
        this.flag = flag;
        this.index = index;
        this.extraInt = extraInt;
    }

    public static void encode(C2SJamActionPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
        buf.writeUtf(msg.targetId);
        buf.writeUtf(msg.payload);
        buf.writeBoolean(msg.flag);
        buf.writeVarInt(msg.index);
        buf.writeVarInt(msg.extraInt);
    }

    public static C2SJamActionPacket decode(FriendlyByteBuf buf) {
        return new C2SJamActionPacket(
                buf.readEnum(Action.class),
                buf.readUtf(),
                buf.readUtf(),
                buf.readBoolean(),
                buf.readVarInt(),
                buf.readVarInt()
        );
    }

    private static MutableComponent text(String msg) {
        //? if >=1.19 {
        return Component.literal(msg);
        //?} else {
        /*return new TextComponent(msg);
         *///?}
    }

    private static void sendMsg(ServerPlayer player, Component comp) {
        //? if >=1.19 {
        player.sendSystemMessage(comp);
        //?} else {
        /*player.sendMessage(comp, net.minecraft.Util.NIL_UUID);
         *///?}
    }

    private static void processAction(C2SJamActionPacket msg, ServerPlayer player) {
        UUID pUuid = player.getUUID();
        String playerName = player.getName().getString();

        switch (msg.action) {
            case REQUEST_SYNC -> {
                SyncedJam jam = ServerJamRegistry.getJamByPlayer(pUuid);
                if (jam != null) {
                    ServerJamRegistry.broadcastJamState(player.server, jam);
                }
                ServerJamRegistry.broadcastDirectory(player.server);
            }
            case PREVIOUS -> {
                SyncedJam jam = ServerJamRegistry.getJamByPlayer(pUuid);
                if (jam != null && jam.canEdit(pUuid)) {
                    jam.previous();
                    ServerJamRegistry.broadcastJamState(player.server, jam);
                }
            }
            case TOGGLE_PLAY -> {
                SyncedJam jam = ServerJamRegistry.getJamByPlayer(pUuid);
                if (jam != null && jam.canEdit(pUuid)) {
                    jam.togglePlay();
                    ServerJamRegistry.broadcastJamState(player.server, jam);
                }
            }
            case SKIP -> {
                SyncedJam jam = ServerJamRegistry.getJamByPlayer(pUuid);
                if (jam != null && jam.canEdit(pUuid)) {
                    jam.skip();
                    ServerJamRegistry.broadcastJamState(player.server, jam);
                }
            }
            case CYCLE_REPEAT -> {
                SyncedJam jam = ServerJamRegistry.getJamByPlayer(pUuid);
                if (jam != null && jam.canEdit(pUuid)) {
                    SyncedJam.RepeatMode next = SyncedJam.RepeatMode.values()[(jam.getRepeatMode().ordinal() + 1) % SyncedJam.RepeatMode.values().length];
                    jam.setRepeatMode(next);
                    ServerJamRegistry.save();
                    ServerJamRegistry.broadcastJamState(player.server, jam);
                }
            }
            case TOGGLE_SHUFFLE -> {
                SyncedJam jam = ServerJamRegistry.getJamByPlayer(pUuid);
                if (jam != null && jam.canEdit(pUuid)) {
                    jam.setShuffle(!jam.isShuffle());
                    ServerJamRegistry.save();
                    ServerJamRegistry.broadcastJamState(player.server, jam);
                }
            }
            case LEAVE_JAM -> {
                SyncedJam old = ServerJamRegistry.leaveCurrentJam(pUuid);
                ModNetworking.sendToPlayer(player, new S2CSyncJamPacket(
                        "", "", "", false, JamEditPolicy.OPEN, SyncedJam.RepeatMode.OFF, false,
                        "", "", 0L, 0L, false, new ArrayList<>(), new ArrayList<>(), 0, false, false, false));
                if (old != null) ServerJamRegistry.broadcastJamState(player.server, old);
                ServerJamRegistry.broadcastDirectory(player.server);
            }
            case ADD_TRACK -> {
                SyncedJam jam = ServerJamRegistry.getJamByPlayer(pUuid);
                if (jam != null && jam.canEdit(pUuid)) {
                    jam.addTrack(msg.payload, msg.targetId, msg.flag);
                    ServerJamRegistry.save();
                    ServerJamRegistry.broadcastJamState(player.server, jam);
                    ServerJamRegistry.broadcastDirectory(player.server);
                }
            }
            case TOGGLE_PRIVACY -> {
                SyncedJam jam = ServerJamRegistry.getJamByPlayer(pUuid);
                if (jam != null && jam.isOwner(pUuid)) {
                    jam.setPrivate(!jam.isPrivate());
                    ServerJamRegistry.save();
                    ServerJamRegistry.broadcastJamState(player.server, jam);
                    ServerJamRegistry.broadcastDirectory(player.server);
                }
            }
            case SET_POLICY -> {
                SyncedJam jam = ServerJamRegistry.getJamByPlayer(pUuid);
                if (jam != null && jam.canManage(pUuid)) {
                    jam.setEditPolicy(jam.getEditPolicy().next());
                    ServerJamRegistry.save();
                    ServerJamRegistry.broadcastJamState(player.server, jam);
                }
            }
            case DELETE_JAM -> {
                if (ServerJamRegistry.deleteJam(msg.targetId, pUuid)) {
                    ServerJamRegistry.broadcastDirectory(player.server);
                }
            }
            case ADD_MEMBER_BY_NAME -> {
                SyncedJam jam = ServerJamRegistry.getJamByPlayer(pUuid);
                if (jam != null && jam.canManage(pUuid)) {
                    ServerPlayer target = player.server.getPlayerList().getPlayerByName(msg.payload);
                    if (target != null) {
                        jam.setRole(target.getUUID(), target.getName().getString(), SyncedJam.Role.EDITOR);
                        ServerJamRegistry.save();
                        ServerJamRegistry.broadcastJamState(player.server, jam);
                        sendMsg(player, text("§6[JamMod] §aAdded §f" + target.getName().getString() + " §aas Editor."));
                    } else {
                        sendMsg(player, text("§c[JamMod] Player '" + msg.payload + "' is not online."));
                    }
                }
            }
            case CREATE_JAM -> {
                SyncedJam jam = ServerJamRegistry.createJam(msg.payload, pUuid, playerName, msg.flag);
                ServerJamRegistry.joinJam(jam.getId(), pUuid, playerName);
                ServerJamRegistry.broadcastJamState(player.server, jam);
                ServerJamRegistry.broadcastDirectory(player.server);
            }
            case SEEK -> {
                SyncedJam jam = ServerJamRegistry.getJamByPlayer(pUuid);
                if (jam != null && jam.canEdit(pUuid)) {
                    try {
                        long ms = Long.parseLong(msg.targetId);
                        jam.seek(ms);
                        ServerJamRegistry.broadcastJamState(player.server, jam);
                    } catch (NumberFormatException ignored) {}
                }
            }
            case JUMP_TRACK -> {
                SyncedJam jam = ServerJamRegistry.getJamByPlayer(pUuid);
                if (jam != null && jam.canEdit(pUuid)) {
                    jam.playTrackAt(msg.index);
                    ServerJamRegistry.broadcastJamState(player.server, jam);
                }
            }
            case MOVE_TRACK -> {
                SyncedJam jam = ServerJamRegistry.getJamByPlayer(pUuid);
                if (jam != null && jam.canEdit(pUuid)) {
                    jam.moveTrack(msg.index, msg.extraInt);
                    ServerJamRegistry.save();
                    ServerJamRegistry.broadcastJamState(player.server, jam);
                }
            }
            case REMOVE_TRACK -> {
                SyncedJam jam = ServerJamRegistry.getJamByPlayer(pUuid);
                if (jam != null && jam.canEdit(pUuid)) {
                    jam.removeTrack(msg.index);
                    ServerJamRegistry.save();
                    ServerJamRegistry.broadcastJamState(player.server, jam);
                    ServerJamRegistry.broadcastDirectory(player.server);
                }
            }
            case CYCLE_MEMBER_ROLE -> {
                SyncedJam jam = ServerJamRegistry.getJamByPlayer(pUuid);
                if (jam != null && jam.canManage(pUuid)) {
                    try {
                        UUID memberUuid = UUID.fromString(msg.targetId);
                        SyncedJam.Role currentRole = jam.getRole(memberUuid);
                        if (currentRole != SyncedJam.Role.OWNER) {
                            SyncedJam.Role nextRole = (currentRole == SyncedJam.Role.LISTENER) ? SyncedJam.Role.EDITOR :
                                    (currentRole == SyncedJam.Role.EDITOR && jam.isOwner(pUuid)) ? SyncedJam.Role.ADMIN : SyncedJam.Role.LISTENER;
                            ServerPlayer target = player.getServer().getPlayerList().getPlayer(memberUuid);
                            String memberName = target != null ? target.getName().getString() : (!msg.payload.isEmpty() ? msg.payload : "Unknown");
                            jam.setRole(memberUuid, memberName, nextRole);
                            ServerJamRegistry.save();
                            ServerJamRegistry.broadcastJamState(player.server, jam);
                        }
                    } catch (IllegalArgumentException ignored) {}
                }
            }
            case REMOVE_MEMBER -> {
                SyncedJam jam = ServerJamRegistry.getJamByPlayer(pUuid);
                if (jam != null && jam.canManage(pUuid)) {
                    try {
                        UUID memberUuid = UUID.fromString(msg.targetId);
                        if (jam.canManageMember(pUuid, memberUuid)) {
                            jam.removeMember(memberUuid);
                            ServerJamRegistry.save();
                            ServerJamRegistry.broadcastJamState(player.server, jam);
                        } else {
                            sendMsg(player, text("§c[JamMod] You do not have authority to remove this member."));
                        }
                    } catch (IllegalArgumentException ignored) {}
                }
            }
            case INVITE_PLAYER -> {
                SyncedJam jam = ServerJamRegistry.getJamByPlayer(pUuid);
                if (jam != null) {
                    ServerPlayer target = player.server.getPlayerList().getPlayerByName(msg.payload);
                    if (target != null) {
                        String err = AntiSpamManager.canSendInvite(pUuid, target.getUUID());
                        if (err != null) {
                            sendMsg(player, text("§c[JamMod] " + err));
                            return;
                        }
                        AntiSpamManager.recordInvite(pUuid, target.getUUID());
                        jam.invitePlayer(target.getUUID(), target.getName().getString());
                        ServerJamRegistry.save();

                        sendMsg(player, text("§6[JamMod] §aInvited §f" + target.getName().getString() + " §ato §f" + jam.getName()));
                        sendMsg(target, text("§6[JamMod] §f" + playerName + " §ainvited you to Jam: §e" + jam.getName() + " ")
                                .append(text("§a§l[Click to Join]").withStyle(s -> s
                                        .withColor(net.minecraft.ChatFormatting.GREEN)
                                        .withUnderlined(true)
                                        .withClickEvent(new net.minecraft.network.chat.ClickEvent(
                                                net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND, "/jam join " + jam.getName()))
                                        .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                                                net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, text("Click to join " + jam.getName()))))));
                    } else {
                        sendMsg(player, text("§c[JamMod] Player '" + msg.payload + "' is not online."));
                    }
                }
            }
            case JOIN_JAM -> {
                String idOrName = !msg.targetId.isEmpty() ? msg.targetId : msg.payload;
                if (!ServerJamRegistry.joinJam(idOrName, pUuid, playerName)) {
                    sendMsg(player, text("§c[JamMod] Cannot join Jam: '" + idOrName + "' (Private or Not Found)."));
                } else {
                    SyncedJam jam = ServerJamRegistry.getJamByPlayer(pUuid);
                    if (jam != null) {
                        ServerJamRegistry.broadcastJamState(player.server, jam);
                        ServerJamRegistry.broadcastDirectory(player.server);
                    }
                }
            }
            case UPDATE_METADATA -> {
                SyncedJam jam = ServerJamRegistry.getJamByPlayer(pUuid);
                if (jam != null) {
                    jam.updateTrack(msg.index, msg.payload, (long) msg.extraInt * 1000L);
                    ServerJamRegistry.save();
                    ServerJamRegistry.broadcastJamState(player.server, jam);
                }
            }
        }
    }

    //? if >=1.21 {
    /*public static void handle(C2SJamActionPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                processAction(msg, player);
            }
        });
    }
    *///?} else if >1.20.1 {
    /*public static void handle(C2SJamActionPacket msg, PlayPayloadContext ctx) {
        ctx.workHandler().execute(() -> {
            ctx.player().ifPresent(p -> {
                if (p instanceof ServerPlayer player) {
                    processAction(msg, player);
                }
            });
        });
    }
    *///?} else {
    public static void handle(C2SJamActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        ServerPlayer player = context.getSender();
        if (player == null) return;
        context.enqueueWork(() -> processAction(msg, player));
        context.setPacketHandled(true);
    }
    //?}
}