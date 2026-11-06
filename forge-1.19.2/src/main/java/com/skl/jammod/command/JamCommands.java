package com.skl.jammod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.skl.jammod.JamMod;
import com.skl.jammod.client.OverlayCorner;
import com.skl.jammod.network.ModNetworking;
import com.skl.jammod.network.S2CSetLocalVolumePacket;
import com.skl.jammod.network.S2CSetOverlayConfigPacket;
import com.skl.jammod.network.S2CSyncJamPacket;
import com.skl.jammod.session.*;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = JamMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class JamCommands {

    public static MutableComponent clickable(String display, String command, String hoverText) {
        return Component.literal(display).withStyle(s -> s
                .withColor(ChatFormatting.AQUA)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(hoverText)))
        );
    }

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_JAMS = (ctx, builder) -> {
        if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
            return SharedSuggestionProvider.suggest(
                    ServerJamRegistry.getVisibleJams(player.getUUID()).stream().map(SyncedJam::getName), builder);
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_MY_JAMS = (ctx, builder) -> {
        if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
            return SharedSuggestionProvider.suggest(
                    ServerJamRegistry.getVisibleJams(player.getUUID()).stream()
                            .filter(j -> j.isOwner(player.getUUID()))
                            .map(SyncedJam::getName), builder);
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ROLES = (ctx, builder) ->
            SharedSuggestionProvider.suggest(new String[]{"admin", "editor", "listener"}, builder);

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("jam")
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(ctx -> createJam(ctx.getSource(), StringArgumentType.getString(ctx, "name"), false))
                                .then(Commands.argument("isPrivate", BoolArgumentType.bool())
                                        .executes(ctx -> createJam(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "name"),
                                                BoolArgumentType.getBool(ctx, "isPrivate"))))))
                .then(Commands.literal("join")
                        .then(Commands.argument("jam", StringArgumentType.greedyString())
                                .suggests(SUGGEST_JAMS)
                                .executes(ctx -> joinJam(ctx.getSource(), StringArgumentType.getString(ctx, "jam")))))
                .then(Commands.literal("leave").executes(ctx -> leaveJam(ctx.getSource())))
                .then(Commands.literal("delete")
                        .then(Commands.argument("jam", StringArgumentType.greedyString())
                                .suggests(SUGGEST_MY_JAMS)
                                .executes(ctx -> deleteJam(ctx.getSource(), StringArgumentType.getString(ctx, "jam")))))
                .then(Commands.literal("list").executes(ctx -> listJams(ctx.getSource())))

                // Playback Controls
                .then(Commands.literal("play")
                        .executes(ctx -> togglePlay(ctx.getSource()))
                        .then(Commands.argument("url", StringArgumentType.greedyString())
                                .executes(ctx -> addTrack(ctx.getSource(), StringArgumentType.getString(ctx, "url"), true))))
                .then(Commands.literal("add")
                        .then(Commands.argument("url", StringArgumentType.greedyString())
                                .executes(ctx -> addTrack(ctx.getSource(), StringArgumentType.getString(ctx, "url"), false))))
                .then(Commands.literal("pause").executes(ctx -> pause(ctx.getSource())))
                .then(Commands.literal("resume").executes(ctx -> resume(ctx.getSource())))
                .then(Commands.literal("toggle").executes(ctx -> togglePlay(ctx.getSource())))
                .then(Commands.literal("skip").executes(ctx -> skip(ctx.getSource())))
                .then(Commands.literal("prev").executes(ctx -> previous(ctx.getSource())))
                .then(Commands.literal("jump")
                        .then(Commands.argument("index", IntegerArgumentType.integer(1))
                                .executes(ctx -> jumpTrack(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "index")))))
                .then(Commands.literal("move")
                        .then(Commands.argument("from", IntegerArgumentType.integer(1))
                                .then(Commands.argument("to", IntegerArgumentType.integer(1))
                                        .executes(ctx -> moveTrack(ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "from"),
                                                IntegerArgumentType.getInteger(ctx, "to"))))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("index", IntegerArgumentType.integer(1))
                                .executes(ctx -> removeTrack(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "index")))))
                .then(Commands.literal("seek")
                        .then(Commands.argument("seconds", IntegerArgumentType.integer(0))
                                .executes(ctx -> seek(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "seconds")))))
                .then(Commands.literal("repeat").executes(ctx -> cycleRepeat(ctx.getSource())))
                .then(Commands.literal("shuffle").executes(ctx -> toggleShuffle(ctx.getSource())))

                // Volume Controls
                .then(Commands.literal("volume")
                        .then(Commands.argument("level", IntegerArgumentType.integer(0, 100))
                                .executes(ctx -> setVolume(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "level")))))
                .then(Commands.literal("vol")
                        .then(Commands.argument("level", IntegerArgumentType.integer(0, 100))
                                .executes(ctx -> setVolume(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "level")))))

                // Overlay Controls
                .then(Commands.literal("overlay")
                        .then(Commands.literal("toggle")
                                .executes(ctx -> toggleOverlay(ctx.getSource())))
                        .then(Commands.literal("corner")
                                .then(Commands.argument("position", StringArgumentType.word())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(new String[]{"top_right", "top_left", "bottom_right", "bottom_left"}, b))
                                        .executes(ctx -> setOverlayCorner(ctx.getSource(), StringArgumentType.getString(ctx, "position"))))))

                // Administration & Policies
                .then(Commands.literal("policy").executes(ctx -> cyclePolicy(ctx.getSource())))
                .then(Commands.literal("privacy").executes(ctx -> togglePrivacy(ctx.getSource())))
                .then(Commands.literal("role")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("role", StringArgumentType.word())
                                        .suggests(SUGGEST_ROLES)
                                        .executes(ctx -> setRole(ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                StringArgumentType.getString(ctx, "role"))))))
                .then(Commands.literal("invite")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> invite(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("toggleinvites").executes(ctx -> toggleInvites(ctx.getSource())))

                .then(Commands.literal("status").executes(ctx -> showStatus(ctx.getSource())))
                .then(Commands.literal("help").executes(ctx -> {
                    ctx.getSource().sendSuccess(Component.literal("§6=== JamMod Commands ===\n")
                            .append(clickable("/jam create <name>", "/jam create ", "Create a new Jam room")).append("\n")
                            .append(clickable("/jam join <name>", "/jam join ", "Join an existing Jam")).append("\n")
                            .append(clickable("/jam leave", "/jam leave", "Leave current Jam")).append("\n")
                            .append(clickable("/jam list", "/jam list", "List available Jams")).append("\n")
                            .append(clickable("/jam play [url]", "/jam play ", "Play track immediately")).append("\n")
                            .append(clickable("/jam add <url>", "/jam add ", "Add track to queue")).append("\n")
                            .append(clickable("/jam pause | resume | toggle", "/jam toggle", "Toggle playback state")).append("\n")
                            .append(clickable("/jam skip | prev", "/jam skip", "Skip or previous track")).append("\n")
                            .append(clickable("/jam overlay toggle | corner <corner>", "/jam overlay toggle", "Configure HUD")).append("\n")
                            .append(clickable("/jam policy", "/jam policy", "Cycle edit policy")).append("\n")
                            .append(clickable("/jam role <player> <admin|editor|listener>", "/jam role ", "Set member role")).append("\n")
                            .append(clickable("/jam volume <0-100>", "/jam volume ", "Set music volume")), false);
                    return 1;
                }))
        );
    }

    private static int createJam(CommandSourceStack source, String name, boolean isPrivate) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        String pName = player.getName().getString();
        SyncedJam jam = ServerJamRegistry.createJam(name, player.getUUID(), pName, isPrivate);
        ServerJamRegistry.joinJam(jam.getId(), player.getUUID(), pName);
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        ServerJamRegistry.broadcastDirectory(source.getServer());
        source.sendSuccess(Component.literal("§6[JamMod] §aCreated and joined " + (isPrivate ? "§c[Private] " : "§a[Public] ") + "§f" + jam.getName()), false);
        return 1;
    }

    private static int joinJam(CommandSourceStack source, String query) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        try {
            if (ServerJamRegistry.joinJam(query, player.getUUID(), player.getName().getString())) {
                SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
                ServerJamRegistry.broadcastJamState(source.getServer(), jam);
                ServerJamRegistry.broadcastDirectory(source.getServer());
                source.sendSuccess(Component.literal("§6[JamMod] §aJoined §f" + jam.getName() + " §7(" + jam.getListeners().size() + " listeners)"), false);
                return 1;
            } else {
                source.sendFailure(Component.literal("§cCould not join Jam: '" + query + "'. It may be private or nonexistent."));
                return 0;
            }
        } catch (Exception e) {
            JamMod.LOGGER.error("[JamMod] Error joining jam: ", e);
            source.sendFailure(Component.literal("§cError joining jam: " + e.getMessage()));
            return 0;
        }
    }

    private static int leaveJam(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam old = ServerJamRegistry.leaveCurrentJam(player.getUUID());
        ModNetworking.CHANNEL.sendTo(new S2CSyncJamPacket(
                        "", "", "", false, JamEditPolicy.OPEN, SyncedJam.RepeatMode.OFF, false,
                        "", "", 0, 0, false, new ArrayList<>(), new ArrayList<>(), 0, false, false, false),
                player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
        if (old != null) ServerJamRegistry.broadcastJamState(source.getServer(), old);
        ServerJamRegistry.broadcastDirectory(source.getServer());
        source.sendSuccess(Component.literal("§6[JamMod] §eLeft jam session."), false);
        return 1;
    }

    private static int deleteJam(CommandSourceStack source, String query) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        if (ServerJamRegistry.deleteJam(query, player.getUUID())) {
            ServerJamRegistry.broadcastDirectory(source.getServer());
            source.sendSuccess(Component.literal("§6[JamMod] §aJam deleted."), false);
            return 1;
        } else {
            source.sendFailure(Component.literal("§cCould not delete Jam. Ensure you are the host."));
            return 0;
        }
    }

    private static int listJams(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        List<SyncedJam> list = ServerJamRegistry.getVisibleJams(player.getUUID());
        if (list.isEmpty()) {
            source.sendSuccess(Component.literal("§7No accessible jams found."), false);
            return 1;
        }
        source.sendSuccess(Component.literal("§6=== Accessible Jams (" + list.size() + ") ==="), false);
        for (SyncedJam j : list) {
            String tag = j.isPrivate() ? "§c[Private]" : "§a[Public]";
            MutableComponent line = Component.literal(tag + " §f" + j.getName() + " §7(by " + j.getOwnerName() + ") §8- " + j.getTracks().size() + " songs ")
                    .append(clickable("§a[Join]", "/jam join " + j.getName(), "Click to join " + j.getName()));
            source.sendSuccess(line, false);
        }
        return 1;
    }

    private static int addTrack(CommandSourceStack source, String url, boolean playNow) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) {
            source.sendFailure(Component.literal("§cYou are not in a Jam. Join or create one first with /jam create"));
            return 0;
        }
        if (!jam.canEdit(player.getUUID())) {
            source.sendFailure(Component.literal("§cYou do not have permission to add tracks to this Jam."));
            return 0;
        }
        jam.addTrack(url, "", playNow);
        ServerJamRegistry.save();
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        ServerJamRegistry.broadcastDirectory(source.getServer());
        source.sendSuccess(Component.literal("§6[JamMod] §aTrack added to §f" + jam.getName()), false);
        return 1;
    }

    private static int togglePlay(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) return 0;
        if (!jam.canEdit(player.getUUID())) {
            source.sendFailure(Component.literal("§cYou do not have permission to control playback."));
            return 0;
        }
        jam.togglePlay();
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        return 1;
    }

    private static int pause(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) return 0;
        if (!jam.canEdit(player.getUUID())) {
            source.sendFailure(Component.literal("§cYou do not have permission to pause playback."));
            return 0;
        }
        jam.pause();
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        source.sendSuccess(Component.literal("§6[JamMod] §ePaused."), false);
        return 1;
    }

    private static int resume(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) return 0;
        if (!jam.canEdit(player.getUUID())) {
            source.sendFailure(Component.literal("§cYou do not have permission to resume playback."));
            return 0;
        }
        jam.resume();
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        source.sendSuccess(Component.literal("§6[JamMod] §aResumed."), false);
        return 1;
    }

    private static int skip(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) return 0;
        if (!jam.canEdit(player.getUUID())) {
            source.sendFailure(Component.literal("§cYou do not have permission to skip tracks."));
            return 0;
        }
        jam.skip();
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        source.sendSuccess(Component.literal("§6[JamMod] §eSkipped to next track."), false);
        return 1;
    }

    private static int previous(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) return 0;
        if (!jam.canEdit(player.getUUID())) {
            source.sendFailure(Component.literal("§cYou do not have permission to change tracks."));
            return 0;
        }
        jam.previous();
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        source.sendSuccess(Component.literal("§6[JamMod] §ePrevious track."), false);
        return 1;
    }

    private static int jumpTrack(CommandSourceStack source, int oneBasedIndex) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) return 0;
        if (!jam.canEdit(player.getUUID())) {
            source.sendFailure(Component.literal("§cYou do not have permission to select tracks."));
            return 0;
        }
        int idx = oneBasedIndex - 1;
        if (idx < 0 || idx >= jam.getTracks().size()) {
            source.sendFailure(Component.literal("§cInvalid track index."));
            return 0;
        }
        jam.playTrackAt(idx);
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        source.sendSuccess(Component.literal("§6[JamMod] §aJumped to track #" + oneBasedIndex), false);
        return 1;
    }

    private static int moveTrack(CommandSourceStack source, int from, int to) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) return 0;
        if (!jam.canEdit(player.getUUID())) {
            source.sendFailure(Component.literal("§cYou do not have permission to reorder tracks."));
            return 0;
        }
        int fromIdx = from - 1;
        int toIdx = to - 1;
        if (fromIdx < 0 || fromIdx >= jam.getTracks().size() || toIdx < 0 || toIdx >= jam.getTracks().size()) {
            source.sendFailure(Component.literal("§cInvalid track index range."));
            return 0;
        }
        jam.moveTrack(fromIdx, toIdx);
        ServerJamRegistry.save();
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        source.sendSuccess(Component.literal("§6[JamMod] §aMoved track #" + from + " to #" + to), false);
        return 1;
    }

    private static int removeTrack(CommandSourceStack source, int oneBasedIndex) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) return 0;
        if (!jam.canEdit(player.getUUID())) {
            source.sendFailure(Component.literal("§cYou do not have permission to delete tracks."));
            return 0;
        }
        int idx = oneBasedIndex - 1;
        if (idx < 0 || idx >= jam.getTracks().size()) {
            source.sendFailure(Component.literal("§cInvalid track index."));
            return 0;
        }
        jam.removeTrack(idx);
        ServerJamRegistry.save();
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        ServerJamRegistry.broadcastDirectory(source.getServer());
        source.sendSuccess(Component.literal("§6[JamMod] §aRemoved track #" + oneBasedIndex), false);
        return 1;
    }

    private static int seek(CommandSourceStack source, int seconds) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) return 0;
        if (!jam.canEdit(player.getUUID())) {
            source.sendFailure(Component.literal("§cYou do not have permission to seek audio in this Jam."));
            return 0;
        }
        jam.seek((long) seconds * 1000L);
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        source.sendSuccess(Component.literal("§6[JamMod] §aSeeked to " + (seconds / 60) + "m " + (seconds % 60) + "s"), false);
        return 1;
    }

    private static int cycleRepeat(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) return 0;
        if (!jam.canEdit(player.getUUID())) {
            source.sendFailure(Component.literal("§cYou do not have permission to change repeat mode."));
            return 0;
        }
        SyncedJam.RepeatMode next = SyncedJam.RepeatMode.values()[(jam.getRepeatMode().ordinal() + 1) % SyncedJam.RepeatMode.values().length];
        jam.setRepeatMode(next);
        ServerJamRegistry.save();
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        source.sendSuccess(Component.literal("§6[JamMod] §aRepeat mode: §e" + next.name()), false);
        return 1;
    }

    private static int toggleShuffle(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) return 0;
        if (!jam.canEdit(player.getUUID())) {
            source.sendFailure(Component.literal("§cYou do not have permission to toggle shuffle."));
            return 0;
        }
        jam.setShuffle(!jam.isShuffle());
        ServerJamRegistry.save();
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        source.sendSuccess(Component.literal("§6[JamMod] §aShuffle mode: " + (jam.isShuffle() ? "§aON" : "§cOFF")), false);
        return 1;
    }

    private static int setVolume(CommandSourceStack source, int volume) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        ModNetworking.CHANNEL.sendTo(new S2CSetLocalVolumePacket(volume), player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
        source.sendSuccess(Component.literal("§6[JamMod] §aMusic volume set to " + volume + "%"), false);
        return 1;
    }

    private static int toggleOverlay(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        ModNetworking.CHANNEL.sendTo(S2CSetOverlayConfigPacket.toggle(), player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
        source.sendSuccess(Component.literal("§6[JamMod] §aToggled HUD overlay."), false);
        return 1;
    }

    private static int setOverlayCorner(CommandSourceStack source, String cornerName) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        OverlayCorner corner = OverlayCorner.fromString(cornerName);
        ModNetworking.CHANNEL.sendTo(S2CSetOverlayConfigPacket.corner(corner), player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
        source.sendSuccess(Component.literal("§6[JamMod] §aHUD overlay position set to §e" + corner.getDisplayName()), false);
        return 1;
    }

    private static int cyclePolicy(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) return 0;
        if (!jam.canManage(player.getUUID())) {
            source.sendFailure(Component.literal("§cOnly the Jam owner and admins can change edit policies."));
            return 0;
        }
        jam.setEditPolicy(jam.getEditPolicy().next());
        ServerJamRegistry.save();
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        source.sendSuccess(Component.literal("§6[JamMod] §aEdit policy changed to: §e" + jam.getEditPolicy().getDisplayName()), false);
        return 1;
    }

    private static int setRole(CommandSourceStack source, ServerPlayer target, String roleName) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) return 0;
        if (!jam.canManage(player.getUUID())) {
            source.sendFailure(Component.literal("§cOnly the Jam host and admins can assign roles."));
            return 0;
        }
        SyncedJam.Role role;
        try {
            role = SyncedJam.Role.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("§cInvalid role. Valid: admin, editor, listener"));
            return 0;
        }
        if (role == SyncedJam.Role.ADMIN && !jam.isOwner(player.getUUID())) {
            source.sendFailure(Component.literal("§cOnly the Jam host can promote players to Admin."));
            return 0;
        }
        if (!jam.canManageMember(player.getUUID(), target.getUUID())) {
            source.sendFailure(Component.literal("§cYou cannot modify the role of this member."));
            return 0;
        }

        jam.setRole(target.getUUID(), target.getName().getString(), role);
        ServerJamRegistry.save();
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        source.sendSuccess(Component.literal("§6[JamMod] §aSet §f" + target.getName().getString() + "§a's role to " + role.getColor() + role.getDisplayName()), false);
        target.sendSystemMessage(Component.literal("§6[JamMod] §aYour role in §f" + jam.getName() + " §awas updated to " + role.getColor() + role.getDisplayName()));
        return 1;
    }

    private static int togglePrivacy(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) return 0;
        if (!jam.isOwner(player.getUUID())) {
            source.sendFailure(Component.literal("§cOnly the Jam host can change room privacy."));
            return 0;
        }
        jam.setPrivate(!jam.isPrivate());
        ServerJamRegistry.save();
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        ServerJamRegistry.broadcastDirectory(source.getServer());
        source.sendSuccess(Component.literal("§6[JamMod] §aRoom privacy set to: " + (jam.isPrivate() ? "§cPrivate" : "§aPublic")), false);
        return 1;
    }

    private static int invite(CommandSourceStack source, ServerPlayer invited) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) {
            source.sendFailure(Component.literal("§cYou are not in a Jam session."));
            return 0;
        }
        String err = AntiSpamManager.canSendInvite(player.getUUID(), invited.getUUID());
        if (err != null) {
            source.sendFailure(Component.literal("§c" + err));
            return 0;
        }

        AntiSpamManager.recordInvite(player.getUUID(), invited.getUUID());
        jam.invitePlayer(invited.getUUID(), invited.getName().getString());
        ServerJamRegistry.save();

        source.sendSuccess(Component.literal("§6[JamMod] §aInvited §f" + invited.getName().getString() + " §ato §f" + jam.getName()), false);
        invited.sendSystemMessage(Component.literal("§6[JamMod] §f" + player.getName().getString() + " §ainvited you to Jam: §e" + jam.getName() + " ")
                .append(clickable("§a§l[Click to Join]", "/jam join " + jam.getName(), "Join " + jam.getName())));
        return 1;
    }

    private static int toggleInvites(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        boolean blocked = AntiSpamManager.toggleInvitesBlocked(player.getUUID());
        source.sendSuccess(Component.literal("§6[JamMod] §eJam invitations are now " + (blocked ? "§cBLOCKED" : "§aALLOWED")), false);
        return 1;
    }

    private static int showStatus(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) {
            source.sendSuccess(Component.literal("§7Not in any active Jam. Use §e/jam list §7to find one."), false);
            return 1;
        }
        source.sendSuccess(Component.literal("§6=== Jam: §f" + jam.getName() + " " + (jam.isPrivate() ? "§c[Private]" : "§a[Public]") + " §6==="), false);
        source.sendSuccess(Component.literal("§7Host: §f" + jam.getOwnerName() + " | §a" + jam.getListeners().size() + " listeners"), false);
        source.sendSuccess(Component.literal("§7Your Role: " + jam.getRole(player.getUUID()).getColor() + jam.getRole(player.getUUID()).getDisplayName() +
                " | Policy: §b" + jam.getEditPolicy().getDisplayName()), false);

        JamTrack cur = jam.getCurrentTrack();
        if (cur != null) {
            source.sendSuccess(Component.literal("§7Now Playing: §e" + cur.getTitle()), false);
        } else {
            source.sendSuccess(Component.literal("§7Now Playing: §8(None)"), false);
        }
        return 1;
    }
}