package com.skl.jammod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.skl.jammod.JamMod;
import com.skl.jammod.network.ModNetworking;
import com.skl.jammod.network.S2CSyncJamPacket;
import com.skl.jammod.session.ServerJamRegistry;
import com.skl.jammod.session.SyncedJam;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = JamMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class JamCommands {

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_JAMS = (ctx, builder) -> {
        if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
            return SharedSuggestionProvider.suggest(
                    ServerJamRegistry.getVisibleJams(player.getUUID()).stream().map(SyncedJam::getName), builder);
        }
        return builder.buildFuture();
    };

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("jam")
                // Core room commands
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(ctx -> createJam(ctx.getSource(), StringArgumentType.getString(ctx, "name"), false))
                                .then(Commands.argument("isPrivate", BoolArgumentType.bool())
                                        .executes(ctx -> createJam(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "name"),
                                                BoolArgumentType.getBool(ctx, "isPrivate"))))))
                .then(Commands.literal("join")
                        .then(Commands.argument("jam", StringArgumentType.string())
                                .suggests(SUGGEST_JAMS)
                                .executes(ctx -> joinJam(ctx.getSource(), StringArgumentType.getString(ctx, "jam")))))
                .then(Commands.literal("leave").executes(ctx -> leaveJam(ctx.getSource())))
                .then(Commands.literal("delete")
                        .then(Commands.argument("jam", StringArgumentType.string())
                                .suggests(SUGGEST_JAMS)
                                .executes(ctx -> deleteJam(ctx.getSource(), StringArgumentType.getString(ctx, "jam")))))
                .then(Commands.literal("list").executes(ctx -> listJams(ctx.getSource())))

                // Playback in active jam
                .then(Commands.literal("play")
                        .executes(ctx -> togglePlay(ctx.getSource()))
                        .then(Commands.argument("url", StringArgumentType.greedyString())
                                .executes(ctx -> addTrack(ctx.getSource(), StringArgumentType.getString(ctx, "url"), true))))
                .then(Commands.literal("add")
                        .then(Commands.argument("url", StringArgumentType.greedyString())
                                .executes(ctx -> addTrack(ctx.getSource(), StringArgumentType.getString(ctx, "url"), false))))
                .then(Commands.literal("pause").executes(ctx -> pause(ctx.getSource())))
                .then(Commands.literal("resume").executes(ctx -> resume(ctx.getSource())))
                .then(Commands.literal("skip").executes(ctx -> skip(ctx.getSource())))
                .then(Commands.literal("prev").executes(ctx -> previous(ctx.getSource())))
                .then(Commands.literal("jump")
                        .then(Commands.argument("index", IntegerArgumentType.integer(1))
                                .executes(ctx -> jumpTrack(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "index")))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("index", IntegerArgumentType.integer(1))
                                .executes(ctx -> removeTrack(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "index")))))

                // Invite & privacy
                .then(Commands.literal("invite")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> invite(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("status").executes(ctx -> showStatus(ctx.getSource())))

                // Help
                .then(Commands.literal("help").executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal("""
                            §6=== Synced Jam Commands ===
                            §e/jam create <name> [true|false] §7- Create Public or Private Jam
                            §e/jam join <name|id> §7- Join jam & sync audio
                            §e/jam leave §7- Leave current jam
                            §e/jam list §7- Browse all available jams
                            §e/jam play [url] §7- Play URL immediately or resume
                            §e/jam add <url> §7- Append track to the playlist
                            §e/jam skip | prev §7- Next or previous track
                            §e/jam jump <index> §7- Jump to song #
                            §e/jam remove <index> §7- Remove song # from playlist
                            §e/jam invite <player> §7- Allow player into private jam
                            §e/jam status §7- View active jam information
                            """), false);
                    return 1;
                }))
        );
    }

    private static int createJam(CommandSourceStack source, String name, boolean isPrivate) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.createJam(name, player.getUUID(), player.getGameProfile().getName(), isPrivate);
        ServerJamRegistry.joinJam(jam.getId(), player.getUUID());
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        ServerJamRegistry.broadcastDirectory(source.getServer());
        source.sendSuccess(() -> Component.literal("§6[JamMod] §aCreated and joined " + (isPrivate ? "§c[Private] " : "§a[Public] ") + "§f" + jam.getName()), false);
        return 1;
    }

    private static int joinJam(CommandSourceStack source, String query) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        if (ServerJamRegistry.joinJam(query, player.getUUID())) {
            SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
            ServerJamRegistry.broadcastJamState(source.getServer(), jam);
            ServerJamRegistry.broadcastDirectory(source.getServer());
            source.sendSuccess(() -> Component.literal("§6[JamMod] §aJoined §f" + jam.getName() + " §7(" + jam.getListeners().size() + " listeners)"), false);
            return 1;
        } else {
            source.sendFailure(Component.literal("§cCould not find or join Jam: " + query));
            return 0;
        }
    }

    private static int leaveJam(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam old = ServerJamRegistry.leaveCurrentJam(player.getUUID());
        ModNetworking.sendToPlayer(player, new S2CSyncJamPacket("", "", "", false, "", 0, 0, false, new ArrayList<>(), 0));
        if (old != null) ServerJamRegistry.broadcastJamState(source.getServer(), old);
        ServerJamRegistry.broadcastDirectory(source.getServer());
        source.sendSuccess(() -> Component.literal("§6[JamMod] §eLeft jam session."), false);
        return 1;
    }

    private static int deleteJam(CommandSourceStack source, String query) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        if (ServerJamRegistry.deleteJam(query, player.getUUID())) {
            ServerJamRegistry.broadcastDirectory(source.getServer());
            source.sendSuccess(() -> Component.literal("§6[JamMod] §aJam playlist deleted."), false);
            return 1;
        } else {
            source.sendFailure(Component.literal("§cCould not delete Jam. Ensure you are the creator."));
            return 0;
        }
    }

    private static int listJams(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        List<SyncedJam> list = ServerJamRegistry.getVisibleJams(player.getUUID());
        if (list.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7No accessible jams available."), false);
            return 1;
        }
        source.sendSuccess(() -> Component.literal("§6=== Accessible Jams (" + list.size() + ") ==="), false);
        for (SyncedJam j : list) {
            String tag = j.isPrivate() ? "§c[Private]" : "§a[Public]";
            source.sendSuccess(() -> Component.literal(tag + " §f" + j.getName() + " §7by §e" + j.getOwnerName() +
                    " §7(" + j.getTracks().size() + " tracks, §a" + j.getListeners().size() + " listening§7)"), false);
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
        jam.addTrack(url, playNow);
        ServerJamRegistry.save();
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        ServerJamRegistry.broadcastDirectory(source.getServer());
        source.sendSuccess(() -> Component.literal("§6[JamMod] §aAdded track to §f" + jam.getName()), false);
        return 1;
    }

    private static int togglePlay(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) return 0;
        jam.togglePlay();
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        return 1;
    }

    private static int pause(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) return 0;
        jam.pause();
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        source.sendSuccess(() -> Component.literal("§6[JamMod] §ePaused."), false);
        return 1;
    }

    private static int resume(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) return 0;
        jam.resume();
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        source.sendSuccess(() -> Component.literal("§6[JamMod] §aResumed."), false);
        return 1;
    }

    private static int skip(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) return 0;
        jam.skip();
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        source.sendSuccess(() -> Component.literal("§6[JamMod] §eSkipped to next track."), false);
        return 1;
    }

    private static int previous(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) return 0;
        jam.previous();
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        source.sendSuccess(() -> Component.literal("§6[JamMod] §ePrevious track."), false);
        return 1;
    }

    private static int jumpTrack(CommandSourceStack source, int oneBasedIndex) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) return 0;
        jam.playTrackAt(oneBasedIndex - 1);
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        source.sendSuccess(() -> Component.literal("§6[JamMod] §aJumped to track #" + oneBasedIndex), false);
        return 1;
    }

    private static int removeTrack(CommandSourceStack source, int oneBasedIndex) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) return 0;
        jam.removeTrack(oneBasedIndex - 1);
        ServerJamRegistry.save();
        ServerJamRegistry.broadcastJamState(source.getServer(), jam);
        ServerJamRegistry.broadcastDirectory(source.getServer());
        source.sendSuccess(() -> Component.literal("§6[JamMod] §aRemoved track #" + oneBasedIndex), false);
        return 1;
    }

    private static int invite(CommandSourceStack source, ServerPlayer invited) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null || !jam.getOwnerId().equals(player.getUUID())) {
            source.sendFailure(Component.literal("§cYou must own the active jam to invite others."));
            return 0;
        }
        jam.invitePlayer(invited.getUUID());
        ServerJamRegistry.save();
        ServerJamRegistry.broadcastDirectory(source.getServer());
        source.sendSuccess(() -> Component.literal("§6[JamMod] §aInvited " + invited.getName().getString() + " to " + jam.getName()), false);
        invited.sendSystemMessage(Component.literal("§6[JamMod] §aYou have been invited to private jam: §f" + jam.getName() + " §7(/jam join " + jam.getName() + ")"));
        return 1;
    }

    private static int showStatus(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        SyncedJam jam = ServerJamRegistry.getJamByPlayer(player.getUUID());
        if (jam == null) {
            source.sendSuccess(() -> Component.literal("§7Not in any active Jam. Use §e/jam list §7to find one."), false);
            return 1;
        }
        source.sendSuccess(() -> Component.literal("§6=== Jam: §f" + jam.getName() + " " + (jam.isPrivate() ? "§c[Private]" : "§a[Public]") + " §6==="), false);
        source.sendSuccess(() -> Component.literal("§7Host: §f" + jam.getOwnerName() + " | §a" + jam.getListeners().size() + " listeners"), false);
        source.sendSuccess(() -> Component.literal("§7State: " + (jam.isPlaying() ? "§aPlaying" : "§cPaused/Idle")), false);
        source.sendSuccess(() -> Component.literal("§7Current Song: §e" + (jam.getCurrentTrack().isEmpty() ? "None" : jam.getCurrentTrack())), false);
        source.sendSuccess(() -> Component.literal("§7Tracks in Playlist: §f" + jam.getTracks().size()), false);
        return 1;
    }
}