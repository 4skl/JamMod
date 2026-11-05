package com.skl.jammod.session;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SyncedJam {
    private final String id;
    private String name;
    private final UUID ownerId;
    private final String ownerName;
    private boolean isPrivate;
    private final List<String> tracks = new ArrayList<>();
    private final Set<UUID> invitedPlayers = new HashSet<>();

    // Runtime state (not serialized to disk)
    private transient final Set<UUID> listeners = ConcurrentHashMap.newKeySet();
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
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UUID getOwnerId() { return ownerId; }
    public String getOwnerName() { return ownerName; }
    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }

    public synchronized List<String> getTracks() { return Collections.unmodifiableList(new ArrayList<>(tracks)); }
    public synchronized Set<UUID> getListeners() { return Collections.unmodifiableSet(new HashSet<>(listeners)); }
    public synchronized Set<UUID> getInvitedPlayers() { return Collections.unmodifiableSet(new HashSet<>(invitedPlayers)); }
    public synchronized int getCurrentTrackIndex() { return currentTrackIndex; }
    public synchronized long getStartTimestamp() { return startTimestamp; }
    public synchronized boolean isPlaying() { return playing; }

    public synchronized String getCurrentTrack() {
        if (tracks.isEmpty() || currentTrackIndex < 0 || currentTrackIndex >= tracks.size()) {
            return "";
        }
        return tracks.get(currentTrackIndex);
    }

    public synchronized void addListener(UUID player) {
        listeners.add(player);
        // Auto-start if songs exist and it was idling
        if (!playing && !tracks.isEmpty() && listeners.size() == 1 && pausedElapsedMs == 0) {
            playTrackAt(currentTrackIndex);
        }
    }

    public synchronized void removeListener(UUID player) {
        listeners.remove(player);
        // Pause if no listeners remain to save bandwidth
        if (listeners.isEmpty() && playing) {
            pause();
        }
    }

    public synchronized void addTrack(String url, boolean playNow) {
        tracks.add(url);
        if (playNow || (tracks.size() == 1 && !playing)) {
            playTrackAt(tracks.size() - 1);
        }
    }

    public synchronized void removeTrack(int index) {
        if (index >= 0 && index < tracks.size()) {
            tracks.remove(index);
            if (index == currentTrackIndex) {
                if (tracks.isEmpty()) {
                    stop();
                } else {
                    currentTrackIndex = Math.min(index, tracks.size() - 1);
                    playTrackAt(currentTrackIndex);
                }
            } else if (index < currentTrackIndex) {
                currentTrackIndex--;
            }
        }
    }

    public synchronized void playTrackAt(int index) {
        if (tracks.isEmpty()) {
            stop();
            return;
        }
        this.currentTrackIndex = Math.max(0, Math.min(index, tracks.size() - 1));
        this.startTimestamp = System.currentTimeMillis();
        this.pausedElapsedMs = 0;
        this.playing = true;
    }

    public synchronized void togglePlay() {
        if (tracks.isEmpty()) return;
        if (this.playing) {
            pause();
        } else {
            resume();
        }
    }

    public synchronized void pause() {
        if (this.playing) {
            this.pausedElapsedMs = Math.max(0, System.currentTimeMillis() - this.startTimestamp);
            this.playing = false;
        }
    }

    public synchronized void resume() {
        if (!this.playing && !tracks.isEmpty()) {
            this.startTimestamp = System.currentTimeMillis() - this.pausedElapsedMs;
            this.playing = true;
        }
    }

    public synchronized void skip() {
        long now = System.currentTimeMillis();
        if (now - lastActionTime < 1000) return; // Debounce multi-client skips
        lastActionTime = now;

        if (currentTrackIndex + 1 < tracks.size()) {
            playTrackAt(currentTrackIndex + 1);
        } else if (!tracks.isEmpty()) {
            // Loop back to start
            playTrackAt(0);
        } else {
            stop();
        }
    }

    public synchronized void previous() {
        if (currentTrackIndex - 1 >= 0) {
            playTrackAt(currentTrackIndex - 1);
        } else if (!tracks.isEmpty()) {
            playTrackAt(0);
        }
    }

    public synchronized void stop() {
        this.playing = false;
        this.startTimestamp = 0;
        this.pausedElapsedMs = 0;
    }

    public synchronized void invitePlayer(UUID player) { invitedPlayers.add(player); }
    public synchronized boolean isAllowed(UUID player) {
        return !isPrivate || ownerId.equals(player) || invitedPlayers.contains(player);
    }
}