package com.skl.jammod.session;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerJamRegistry {
    private static final Map<String, JamSession> SESSIONS = new ConcurrentHashMap<>();
    private static final Map<UUID, String> PLAYER_TO_SESSION = new ConcurrentHashMap<>();

    public static JamSession getOrCreateSession(UUID player, String fallbackName) {
        String id = PLAYER_TO_SESSION.get(player);
        if (id != null && SESSIONS.containsKey(id)) {
            return SESSIONS.get(id);
        }
        return createSession(fallbackName + "'s Jam", player);
    }

    public static JamSession createSession(String id, UUID host) {
        if (id == null || id.isBlank()) id = "Default Jam";

        // Remove host from old session first
        leaveSession(host);

        JamSession session = new JamSession(id, host);
        SESSIONS.put(id, session);
        PLAYER_TO_SESSION.put(host, id);
        return session;
    }

    public static void joinSession(String id, UUID player) {
        JamSession session = SESSIONS.get(id);
        if (session != null) {
            leaveSession(player);
            PLAYER_TO_SESSION.put(player, id);
            session.addMember(player);
        }
    }

    public static void leaveSession(UUID player) {
        String sessionId = PLAYER_TO_SESSION.remove(player);
        if (sessionId != null) {
            JamSession session = SESSIONS.get(sessionId);
            if (session != null) {
                session.removeMember(player);
                // Evict empty sessions
                if (session.getMembers().isEmpty()) {
                    SESSIONS.remove(sessionId);
                }
            }
        }
    }

    public static JamSession getSessionByPlayer(UUID player) {
        String id = PLAYER_TO_SESSION.get(player);
        return id != null ? SESSIONS.get(id) : null;
    }
}