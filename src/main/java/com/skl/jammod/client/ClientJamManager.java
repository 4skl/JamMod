package com.skl.jammod.client;

import com.skl.jammod.network.S2CSyncJamListPacket.JamSummary;
import com.skl.jammod.session.JamEditPolicy;
import com.skl.jammod.session.JamTrack;
import com.skl.jammod.session.SyncedJam;
import com.skl.jammod.session.SyncedJam.RepeatMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientJamManager {
    private static String jamId = "";
    private static String jamName = "";
    private static String ownerName = "";
    private static boolean isPrivate = false;
    private static JamEditPolicy editPolicy = JamEditPolicy.OPEN;
    private static RepeatMode repeatMode = RepeatMode.OFF;
    private static boolean shuffle = false;
    private static String currentTrackUrl = "";
    private static String currentTrackTitle = "";
    private static int currentTrackIndex = 0;
    private static long startTimestamp = 0;
    private static long pausedElapsedMs = 0;
    private static boolean playing = false;
    private static List<JamTrack> tracks = new ArrayList<>();
    private static List<SyncedJam.MemberInfo> members = new ArrayList<>();
    private static int listenerCount = 0;
    private static boolean canEdit = true;
    private static boolean isOwner = false;
    private static boolean isAdmin = false;

    private static List<JamSummary> directory = new ArrayList<>();

    public static void updateSession(String id, String name, String owner, boolean priv,
                                     JamEditPolicy policy, RepeatMode rep, boolean shuf,
                                     String trackUrl, String trackTitle, int index,
                                     long start, boolean isPlaying,
                                     List<JamTrack> trackList,
                                     List<SyncedJam.MemberInfo> memberList,
                                     int listeners, boolean editable, boolean ownerFlag, boolean adminFlag) {
        boolean trackChanged = !trackUrl.equals(currentTrackUrl);
        boolean titleChanged = !trackTitle.equals(currentTrackTitle);
        boolean stateChanged = isPlaying != playing;

        jamId = id;
        jamName = name;
        ownerName = owner;
        isPrivate = priv;
        editPolicy = policy != null ? policy : JamEditPolicy.OPEN;
        repeatMode = rep != null ? rep : RepeatMode.OFF;
        shuffle = shuf;
        currentTrackUrl = trackUrl;
        currentTrackTitle = trackTitle;
        currentTrackIndex = index;
        startTimestamp = start;

        if (!isPlaying && playing) {
            pausedElapsedMs = Math.max(0, System.currentTimeMillis() - start);
        } else if (isPlaying) {
            pausedElapsedMs = 0;
        }

        playing = isPlaying;
        tracks = trackList != null ? new ArrayList<>(trackList) : new ArrayList<>();
        members = memberList != null ? new ArrayList<>(memberList) : new ArrayList<>();
        listenerCount = listeners;
        canEdit = editable;
        isOwner = ownerFlag;
        isAdmin = adminFlag;

        if (isAdmin || isOwner) {
            for (int i = 0; i < tracks.size(); i++) {
                JamTrack t = tracks.get(i);
                if ((t.getTitle().equals(t.getUrl()) || t.getDurationMs() <= 0) && t.getUrl().startsWith("http")) {
                    ClientAudioEngine.fetchMetadataBackground(t.getUrl(), i);
                }
            }
        }

        if (trackUrl.isEmpty() || jamId.isEmpty()) {
            ClientAudioEngine.stop();
            MusicSuppressor.resumeVanillaMusic();
            return;
        }

        long offsetMs = Math.max(0, System.currentTimeMillis() - startTimestamp);

        if (trackChanged) {
            MusicSuppressor.stopVanillaMusic();
            ClientAudioEngine.play(trackUrl, offsetMs);
            ClientHUDOverlay.triggerPopup(trackTitle, trackUrl);
        } else if (titleChanged && !trackTitle.isEmpty()) {
            ClientHUDOverlay.triggerPopup(trackTitle, trackUrl);
        } else if (stateChanged) {
            ClientAudioEngine.setPaused(!playing);
            if (playing) MusicSuppressor.stopVanillaMusic();
            else MusicSuppressor.resumeVanillaMusic();
        } else if (playing) {
            long currentEnginePos = ClientAudioEngine.getPosition();
            if (Math.abs(currentEnginePos - offsetMs) > 1200) {
                ClientAudioEngine.seek(offsetMs);
            }
        }
    }

    public static void setDirectory(List<JamSummary> list) {
        directory = list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    public static List<JamSummary> getDirectory() {
        return Collections.unmodifiableList(directory);
    }

    public static void reset() {
        jamId = "";
        jamName = "";
        ownerName = "";
        isPrivate = false;
        editPolicy = JamEditPolicy.OPEN;
        repeatMode = RepeatMode.OFF;
        shuffle = false;
        currentTrackUrl = "";
        currentTrackTitle = "";
        currentTrackIndex = 0;
        startTimestamp = 0;
        pausedElapsedMs = 0;
        playing = false;
        tracks.clear();
        members.clear();
        directory.clear();
        canEdit = true;
        isOwner = false;
        isAdmin = false;
        ClientAudioEngine.stop();
        MusicSuppressor.resumeVanillaMusic();
    }

    public static boolean isInJam() { return !jamId.isEmpty(); }
    public static String getJamId() { return jamId; }
    public static String getJamName() { return jamName; }
    public static String getOwnerName() { return ownerName; }
    public static boolean isPrivate() { return isPrivate; }
    public static JamEditPolicy getEditPolicy() { return editPolicy; }
    public static RepeatMode getRepeatMode() { return repeatMode; }
    public static boolean isShuffle() { return shuffle; }
    public static String getCurrentTrackUrl() {
        return currentTrackUrl != null ? currentTrackUrl : "";
    }
    public static String getCurrentTrackTitle() { return currentTrackTitle; }
    public static int getCurrentTrackIndex() { return currentTrackIndex; }
    public static long getStartTimestamp() { return startTimestamp; }
    public static long getCurrentDuration() {
        int idx = ClientJamManager.getCurrentTrackIndex();
        List<JamTrack> tracks = ClientJamManager.getTracks();
        if (idx >= 0 && idx < tracks.size()) {
            return tracks.get(idx).getDurationMs();
        }
        return 0L;
    }
    public static long getPausedElapsedMs() { return pausedElapsedMs; }
    public static boolean isPlaying() { return playing; }
    public static List<JamTrack> getTracks() { return Collections.unmodifiableList(tracks); }
    public static List<SyncedJam.MemberInfo> getMembers() { return Collections.unmodifiableList(members); }
    public static int getListenerCount() { return listenerCount; }
    public static boolean canEdit() { return canEdit; }
    public static boolean isOwner() { return isOwner; }
    public static boolean isAdmin() { return isAdmin; }
    public static boolean canManage() { return isOwner || isAdmin; }
}