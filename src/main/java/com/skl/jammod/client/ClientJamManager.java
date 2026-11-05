package com.skl.jammod.client;

import com.skl.jammod.JamMod;
import com.skl.jammod.network.S2CSyncJamListPacket.JamSummary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientJamManager {
    private static String jamId = "";
    private static String jamName = "";
    private static String ownerName = "";
    private static boolean isPrivate = false;
    private static String currentTrack = "";
    private static int currentTrackIndex = 0;
    private static boolean playing = false;
    private static List<String> tracks = new ArrayList<>();
    private static int listenerCount = 0;

    private static List<JamSummary> directory = new ArrayList<>();

    public static void updateSession(String id, String name, String owner, boolean priv,
                                     String track, int index, long startTimestamp, boolean isPlaying,
                                     List<String> trackList, int listeners) {
        boolean trackChanged = !track.equals(currentTrack);
        boolean stateChanged = isPlaying != playing;

        jamId = id;
        jamName = name;
        ownerName = owner;
        isPrivate = priv;
        currentTrack = track;
        currentTrackIndex = index;
        playing = isPlaying;
        tracks = trackList != null ? new ArrayList<>(trackList) : new ArrayList<>();
        listenerCount = listeners;

        if (track.isEmpty() || jamId.isEmpty()) {
            ClientAudioEngine.stop();
            MusicSuppressor.resumeVanillaMusic();
            return;
        }

        if (trackChanged) {
            if (playing) {
                long offsetMs = Math.max(0, System.currentTimeMillis() - startTimestamp);
                MusicSuppressor.stopVanillaMusic();
                ClientAudioEngine.play(track, offsetMs);
            } else {
                ClientAudioEngine.stop();
                MusicSuppressor.resumeVanillaMusic();
            }
        } else if (stateChanged) {
            ClientAudioEngine.setPaused(!playing);
            if (playing) {
                MusicSuppressor.stopVanillaMusic();
            } else {
                MusicSuppressor.resumeVanillaMusic();
            }
        }
    }

    public static void setDirectory(List<JamSummary> list) {
        directory = list != null ? list : new ArrayList<>();
    }

    public static void reset() {
        jamId = "";
        jamName = "";
        ownerName = "";
        currentTrack = "";
        playing = false;
        tracks.clear();
        directory.clear();
        ClientAudioEngine.stop();
        MusicSuppressor.resumeVanillaMusic();
    }

    public static boolean isInJam() { return !jamId.isEmpty(); }
    public static String getJamId() { return jamId; }
    public static String getJamName() { return jamName; }
    public static String getOwnerName() { return ownerName; }
    public static boolean isPrivate() { return isPrivate; }
    public static String getCurrentTrack() { return currentTrack; }
    public static int getCurrentTrackIndex() { return currentTrackIndex; }
    public static boolean isPlaying() { return playing; }
    public static List<String> getTracks() { return Collections.unmodifiableList(tracks); }
    public static int getListenerCount() { return listenerCount; }
    public static List<JamSummary> getDirectory() { return Collections.unmodifiableList(directory); }
}