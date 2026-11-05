package com.skl.jammod.client;

import com.skl.jammod.JamMod;

public class ClientJamManager {
    private static String currentSession = "";
    private static String currentTrack = "";
    private static boolean playing = false;

    public static void updateSession(String session, String track, long startTimestamp, boolean isPlaying) {
        boolean trackChanged = !track.equals(currentTrack);
        boolean stateChanged = isPlaying != playing;

        currentSession = session;
        currentTrack = track;
        playing = isPlaying;

        JamMod.LOGGER.info("[JamClient] State: session={}, playing={}, track={}", session, playing, track);

        if (track.isEmpty()) {
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

    public static boolean isPlaying() { return playing; }
    public static String getCurrentTrack() { return currentTrack; }
    public static String getCurrentSession() { return currentSession; }
}