package com.skl.jammod.session;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AntiSpamManager {
    private static final Map<UUID, Long> LAST_INVITE_TIME = new ConcurrentHashMap<>();
    private static final Map<String, Long> TARGET_INVITE_HISTORY = new ConcurrentHashMap<>();
    private static final Set<UUID> BLOCKED_INVITES_PLAYERS = ConcurrentHashMap.newKeySet();

    private static final long GLOBAL_COOLDOWN_MS = 4000;      // 4s between sending invites
    private static final long TARGET_COOLDOWN_MS = 45000;     // 45s before inviting same player again

    public static boolean toggleInvitesBlocked(UUID player) {
        if (BLOCKED_INVITES_PLAYERS.contains(player)) {
            BLOCKED_INVITES_PLAYERS.remove(player);
            return false; // Invites now allowed
        } else {
            BLOCKED_INVITES_PLAYERS.add(player);
            return true; // Invites now blocked
        }
    }

    public static boolean isIgnoringInvites(UUID player) {
        return BLOCKED_INVITES_PLAYERS.contains(player);
    }

    public static String canSendInvite(UUID sender, UUID target) {
        if (sender.equals(target)) {
            return "You cannot invite yourself.";
        }
        if (BLOCKED_INVITES_PLAYERS.contains(target)) {
            return "This player has disabled Jam invitations.";
        }
        long now = System.currentTimeMillis();

        Long lastSent = LAST_INVITE_TIME.get(sender);
        if (lastSent != null && (now - lastSent < GLOBAL_COOLDOWN_MS)) {
            long remaining = ((GLOBAL_COOLDOWN_MS - (now - lastSent)) / 1000) + 1;
            return "Please wait " + remaining + "s before sending another invite.";
        }

        String pairKey = sender.toString() + "->" + target.toString();
        Long lastTarget = TARGET_INVITE_HISTORY.get(pairKey);
        if (lastTarget != null && (now - lastTarget < TARGET_COOLDOWN_MS)) {
            long remaining = ((TARGET_COOLDOWN_MS - (now - lastTarget)) / 1000) + 1;
            return "You recently invited this player. Try again in " + remaining + "s.";
        }

        return null; // OK
    }

    public static void recordInvite(UUID sender, UUID target) {
        long now = System.currentTimeMillis();
        LAST_INVITE_TIME.put(sender, now);
        TARGET_INVITE_HISTORY.put(sender.toString() + "->" + target.toString(), now);
    }
}