package com.skl.jammod.session;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SyncedJam {
    public enum RepeatMode { OFF, ALL, ONE }

    public enum Role {
        OWNER("Host", "§c"),
        ADMIN("Admin", "§6"),
        EDITOR("Editor", "§e"),
        LISTENER("Listener", "§7");

        private final String displayName;
        private final String color;

        Role(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }

        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
    }

    public record MemberInfo(UUID uuid, String name, Role role) {}

    private final String id;
    private String name;
    private final UUID ownerId;
    private final String ownerName;
    private boolean isPrivate;
    private JamEditPolicy editPolicy = JamEditPolicy.OPEN;
    private List<JamTrack> tracks = new ArrayList<>();
    private Map<UUID, Role> memberRoles = new ConcurrentHashMap<>();
    private Map<UUID, String> memberNames = new ConcurrentHashMap<>();
    private Set<UUID> invitedPlayers = new HashSet<>();

    private RepeatMode repeatMode = RepeatMode.OFF;
    private boolean shuffle = false;

    // Runtime state (Gson-safe)
    private transient Set<UUID> listeners;
    private transient int currentTrackIndex = 0;
    private transient long startTimestamp = 0;
    private transient long pausedElapsedMs = 0;
    private transient boolean playing = false;
    private transient long lastActionTime = 0;

    public SyncedJam(String id, String name, UUID ownerId, String ownerName, boolean isPrivate) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.isPrivate = isPrivate;
        ensureRuntimeState();
        memberRoles.put(ownerId, Role.OWNER);
        memberNames.put(ownerId, ownerName);
    }

    public synchronized void ensureRuntimeState() {
        if (listeners == null) listeners = ConcurrentHashMap.newKeySet();
        if (tracks == null) tracks = new ArrayList<>();
        if (memberRoles == null) memberRoles = new ConcurrentHashMap<>();
        if (memberNames == null) memberNames = new ConcurrentHashMap<>();
        if (invitedPlayers == null) invitedPlayers = new HashSet<>();
        if (editPolicy == null) editPolicy = JamEditPolicy.OPEN;
        if (repeatMode == null) repeatMode = RepeatMode.OFF;

        if (!memberRoles.containsKey(ownerId)) {
            memberRoles.put(ownerId, Role.OWNER);
            memberNames.put(ownerId, ownerName);
        }
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UUID getOwnerId() { return ownerId; }
    public String getOwnerName() { return ownerName; }
    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }
    public JamEditPolicy getEditPolicy() { ensureRuntimeState(); return editPolicy; }
    public void setEditPolicy(JamEditPolicy policy) { this.editPolicy = policy; }
    public RepeatMode getRepeatMode() { ensureRuntimeState(); return repeatMode; }
    public void setRepeatMode(RepeatMode mode) { this.repeatMode = mode; }
    public boolean isShuffle() { return shuffle; }
    public void setShuffle(boolean shuffle) { this.shuffle = shuffle; }

    public synchronized List<JamTrack> getTracks() { ensureRuntimeState(); return Collections.unmodifiableList(new ArrayList<>(tracks)); }
    public synchronized Set<UUID> getListeners() { ensureRuntimeState(); return Collections.unmodifiableSet(new HashSet<>(listeners)); }
    public synchronized int getCurrentTrackIndex() { return currentTrackIndex; }
    public synchronized long getStartTimestamp() { return startTimestamp; }
    public synchronized boolean isPlaying() { return playing; }

    public synchronized JamTrack getCurrentTrack() {
        ensureRuntimeState();
        if (tracks.isEmpty() || currentTrackIndex < 0 || currentTrackIndex >= tracks.size()) return null;
        return tracks.get(currentTrackIndex);
    }

    public synchronized List<MemberInfo> getMemberList() {
        ensureRuntimeState();
        List<MemberInfo> list = new ArrayList<>();
        for (Map.Entry<UUID, Role> entry : memberRoles.entrySet()) {
            String uname = memberNames.getOrDefault(entry.getKey(), "Unknown");
            list.add(new MemberInfo(entry.getKey(), uname, entry.getValue()));
        }
        return list;
    }

    public synchronized Role getRole(UUID player) {
        ensureRuntimeState();
        if (ownerId.equals(player)) return Role.OWNER;
        return memberRoles.getOrDefault(player, Role.LISTENER);
    }

    public synchronized void setRole(UUID player, String playerName, Role role) {
        ensureRuntimeState();
        if (ownerId.equals(player)) return;
        memberRoles.put(player, role);
        if (playerName != null && !playerName.isBlank()) {
            memberNames.put(player, playerName);
        }
        if (role != Role.LISTENER) {
            invitedPlayers.add(player);
        }
    }

    public synchronized void cycleRole(UUID player) {
        ensureRuntimeState();
        if (ownerId.equals(player)) return;
        Role current = getRole(player);
        Role next = switch (current) {
            case LISTENER -> Role.EDITOR;
            case EDITOR -> Role.ADMIN;
            case ADMIN -> Role.LISTENER;
            default -> Role.LISTENER;
        };
        setRole(player, memberNames.get(player), next);
    }

    public synchronized void removeMember(UUID player) {
        ensureRuntimeState();
        if (ownerId.equals(player)) return;
        memberRoles.remove(player);
        memberNames.remove(player);
        invitedPlayers.remove(player);
        listeners.remove(player);
    }

    public synchronized boolean isOwner(UUID player) { return ownerId.equals(player); }
    public synchronized boolean isAdmin(UUID player) { return isOwner(player) || getRole(player) == Role.ADMIN; }
    public synchronized boolean canManage(UUID player) { return isAdmin(player); }

    public synchronized boolean canEdit(UUID player) {
        ensureRuntimeState();
        if (isAdmin(player)) return true;
        if (editPolicy == JamEditPolicy.OPEN) return true;
        return editPolicy == JamEditPolicy.COLLABORATORS && getRole(player) == Role.EDITOR;
    }

    public synchronized boolean isAllowed(UUID player) {
        ensureRuntimeState();
        if (!isPrivate) return true;
        return ownerId.equals(player) || getRole(player) == Role.ADMIN || getRole(player) == Role.EDITOR || invitedPlayers.contains(player);
    }

    public synchronized void addListener(UUID player, String playerName) {
        ensureRuntimeState();
        listeners.add(player);
        if (!memberRoles.containsKey(player)) {
            memberRoles.put(player, Role.LISTENER);
            if (playerName != null && !playerName.isBlank()) memberNames.put(player, playerName);
        }
        if (!playing && !tracks.isEmpty() && listeners.size() == 1 && pausedElapsedMs == 0) {
            playTrackAt(currentTrackIndex);
        }
    }

    public synchronized void removeListener(UUID player) {
        ensureRuntimeState();
        listeners.remove(player);
        if (listeners.isEmpty() && playing) pause();
    }

    public synchronized void addTrack(String url, String title, boolean playNow) {
        ensureRuntimeState();
        tracks.add(new JamTrack(url, title, 0));
        if (playNow || (tracks.size() == 1 && !playing)) {
            playTrackAt(tracks.size() - 1);
        }
    }

    public synchronized void updateTrackMetadata(int index, String title, long durationMs) {
        ensureRuntimeState();
        if (index >= 0 && index < tracks.size()) {
            JamTrack t = tracks.get(index);
            t.setTitle(title);
            t.setDurationMs(durationMs);
        }
    }

    public synchronized void removeTrack(int index) {
        ensureRuntimeState();
        if (index >= 0 && index < tracks.size()) {
            tracks.remove(index);
            if (index == currentTrackIndex) {
                if (tracks.isEmpty()) stop();
                else playTrackAt(Math.min(index, tracks.size() - 1));
            } else if (index < currentTrackIndex) {
                currentTrackIndex--;
            }
        }
    }

    public synchronized void moveTrack(int from, int to) {
        ensureRuntimeState();
        if (from < 0 || from >= tracks.size() || to < 0 || to >= tracks.size() || from == to) return;
        JamTrack track = tracks.remove(from);
        tracks.add(to, track);
        if (currentTrackIndex == from) currentTrackIndex = to;
        else if (from < currentTrackIndex && to >= currentTrackIndex) currentTrackIndex--;
        else if (from > currentTrackIndex && to <= currentTrackIndex) currentTrackIndex++;
    }

    public synchronized void seek(long offsetMs) {
        ensureRuntimeState();
        if (tracks.isEmpty() || !playing) return;
        this.startTimestamp = System.currentTimeMillis() - offsetMs;
    }

    public synchronized void playTrackAt(int index) {
        ensureRuntimeState();
        if (tracks.isEmpty()) { stop(); return; }
        this.currentTrackIndex = Math.max(0, Math.min(index, tracks.size() - 1));
        this.startTimestamp = System.currentTimeMillis();
        this.pausedElapsedMs = 0;
        this.playing = true;
    }

    public synchronized void togglePlay() {
        if (tracks.isEmpty()) return;
        if (this.playing) pause(); else resume();
    }

    public synchronized void pause() {
        if (this.playing) {
            this.pausedElapsedMs = Math.max(0, System.currentTimeMillis() - this.startTimestamp);
            this.playing = false;
        }
    }

    public synchronized void resume() {
        ensureRuntimeState();
        if (!this.playing && !tracks.isEmpty()) {
            this.startTimestamp = System.currentTimeMillis() - this.pausedElapsedMs;
            this.playing = true;
        }
    }

    public synchronized void skip() {
        ensureRuntimeState();
        long now = System.currentTimeMillis();
        if (now - lastActionTime < 500) return;
        lastActionTime = now;

        if (tracks.isEmpty()) { stop(); return; }
        if (repeatMode == RepeatMode.ONE) { playTrackAt(currentTrackIndex); return; }
        if (shuffle && tracks.size() > 1) {
            int next;
            do { next = new Random().nextInt(tracks.size()); } while (next == currentTrackIndex);
            playTrackAt(next);
            return;
        }
        if (currentTrackIndex + 1 < tracks.size()) {
            playTrackAt(currentTrackIndex + 1);
        } else if (repeatMode == RepeatMode.ALL) {
            playTrackAt(0);
        } else {
            stop();
        }
    }

    public synchronized void previous() {
        ensureRuntimeState();
        if (currentTrackIndex - 1 >= 0) playTrackAt(currentTrackIndex - 1);
        else if (!tracks.isEmpty()) playTrackAt(0);
    }

    public synchronized void stop() {
        this.playing = false;
        this.startTimestamp = 0;
        this.pausedElapsedMs = 0;
    }

    public synchronized void invitePlayer(UUID player, String playerName) {
        ensureRuntimeState();
        invitedPlayers.add(player);
        if (!memberRoles.containsKey(player)) {
            memberRoles.put(player, Role.LISTENER);
            if (playerName != null && !playerName.isBlank()) memberNames.put(player, playerName);
        }
    }
}