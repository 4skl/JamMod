package com.skl.jammod.session;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JamSession {
    private final String id;
    private final UUID hostUuid;
    private final List<String> tracks = Collections.synchronizedList(new ArrayList<>());
    private final Set<UUID> members = ConcurrentHashMap.newKeySet();
    private int currentTrackIndex = 0;
    private long startTimestamp = 0;
    private long pausedPositionMs = 0;
    private boolean playing = false;

    public JamSession(String id, UUID hostUuid) {
        this.id = id;
        this.hostUuid = hostUuid;
        this.members.add(hostUuid);
    }

    public synchronized void playTrackNow(String url) {
        this.tracks.clear();
        this.tracks.add(url);
        this.currentTrackIndex = 0;
        this.playing = true;
        this.pausedPositionMs = 0;
        this.startTimestamp = System.currentTimeMillis();
    }

    public synchronized void togglePlay() {
        if (tracks.isEmpty()) return;

        if (this.playing) {
            this.pausedPositionMs = Math.max(0, System.currentTimeMillis() - this.startTimestamp);
            this.playing = false;
        } else {
            this.startTimestamp = System.currentTimeMillis() - this.pausedPositionMs;
            this.playing = true;
        }
    }

    public synchronized void pause() {
        if (this.playing) {
            this.pausedPositionMs = Math.max(0, System.currentTimeMillis() - this.startTimestamp);
            this.playing = false;
        }
    }

    public synchronized void skip() {
        if (!tracks.isEmpty()) {
            if (tracks.size() == 1) {
                this.playing = false;
                this.tracks.clear();
                this.currentTrackIndex = 0;
                this.pausedPositionMs = 0;
                this.startTimestamp = 0;
            } else {
                this.currentTrackIndex = (this.currentTrackIndex + 1) % this.tracks.size();
                this.pausedPositionMs = 0;
                this.startTimestamp = System.currentTimeMillis();
            }
        }
    }

    public synchronized String getCurrentTrack() {
        if (tracks.isEmpty() || currentTrackIndex >= tracks.size()) return "";
        return tracks.get(currentTrackIndex);
    }

    public String getId() { return id; }
    public synchronized boolean isPlaying() { return playing; }
    public synchronized long getStartTimestamp() { return startTimestamp; }
    public List<String> getTracks() { return tracks; }
    public Set<UUID> getMembers() { return members; }
    public void addMember(UUID uuid) { members.add(uuid); }
    public void removeMember(UUID uuid) { members.remove(uuid); }
}