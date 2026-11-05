package com.skl.jammod.client;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import com.skl.jammod.JamMod;
import com.skl.jammod.network.C2SJamActionPacket;
import com.skl.jammod.network.ModNetworking;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.YoutubeSourceOptions;
import dev.lavalink.youtube.clients.*;
import dev.lavalink.youtube.clients.skeleton.Client;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import org.apache.http.HttpRequest;
import org.apache.http.protocol.HttpContext;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class ClientAudioEngine {
    private static AudioPlayerManager playerManager;
    private static AudioPlayer player;
    private static YoutubeAudioSourceManager ytSource;
    private static SourceDataLine audioLine;
    private static Thread audioThread;
    private static volatile boolean running = false;
    private static boolean initialized = false;

    private static final AudioDataFormat AUDIO_FORMAT = StandardAudioDataFormats.DISCORD_PCM_S16_BE;

    public static synchronized void init() {
        if (initialized) return;

        try {
            playerManager = new DefaultAudioPlayerManager();
            playerManager.getConfiguration().setOutputFormat(AUDIO_FORMAT);

            YoutubeSourceOptions options = new YoutubeSourceOptions();
            options.setRemoteCipher("https://cipher.kikkia.dev/", "", "JamMod");

            ytSource = new YoutubeAudioSourceManager(options, new Client[]{
                    new Tv(), new TvHtml5Simply(), new AndroidVr(), new Music(), new Web(), new WebEmbedded()
            });

            final String ps4UserAgent = "Mozilla/5.0 (PlayStation; PlayStation 4/12.00) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Safari/605.1.15";
            ytSource.getHttpInterfaceManager().configureBuilder(builder ->
                    builder.addInterceptorFirst((HttpRequest request, HttpContext context) -> {
                        if (request.containsHeader("User-Agent")) {
                            var header = request.getFirstHeader("User-Agent");
                            if (header != null && (header.getValue().contains("Cobalt") || header.getValue().contains("ChromiumStylePlatform"))) {
                                request.setHeader("User-Agent", ps4UserAgent);
                            }
                        }
                        request.setHeader("Accept-Language", "en-US,en;q=0.9");
                    })
            );

            playerManager.registerSourceManager(ytSource);
            AudioSourceManagers.registerRemoteSources(playerManager, com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager.class);

            player = playerManager.createPlayer();
            updateVolume();

            player.addListener(new AudioEventAdapter() {
                @Override
                public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
                    JamMod.LOGGER.error("[JamMod] Audio error on {}: ", track.getInfo().title, exception);
                    sendChatMessage("§c[JamMod] Playback error: " + exception.getMessage());
                }

                @Override
                public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                    if (endReason == AudioTrackEndReason.FINISHED) {
                        ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.SKIP));
                    }
                }
            });

            initAudioLine();
            startAudioThread();

            initialized = true;
            JamMod.LOGGER.info("[JamMod] Audio Engine initialized at {} Hz.", AUDIO_FORMAT.sampleRate);
        } catch (Throwable t) {
            JamMod.LOGGER.error("[JamMod] Failed to initialize Audio Engine: ", t);
        }
    }

    private static void initAudioLine() {
        try {
            AudioFormat format = new AudioFormat(
                    AUDIO_FORMAT.sampleRate,
                    16,
                    AUDIO_FORMAT.channelCount,
                    true,
                    true
            );
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) return;

            audioLine = (SourceDataLine) AudioSystem.getLine(info);
            audioLine.open(format, 3840 * 8);
            audioLine.start();
        } catch (Exception e) {
            JamMod.LOGGER.error("[JamMod] Failed to open audio line: ", e);
        }
    }

    public static void updateVolume() {
        if (player == null) return;
        Minecraft mc = Minecraft.getInstance();
        float master = mc.options.getSoundSourceVolume(SoundSource.MASTER);
        float music = mc.options.getSoundSourceVolume(SoundSource.MUSIC);
        int vol = Math.round(master * music * 100.0f);
        player.setVolume(Math.max(0, Math.min(100, vol)));
    }

    public static int getCurrentCalculatedVolume() {
        Minecraft mc = Minecraft.getInstance();
        return Math.round(mc.options.getSoundSourceVolume(SoundSource.MASTER) *
                mc.options.getSoundSourceVolume(SoundSource.MUSIC) * 100.0f);
    }

    private static void startAudioThread() {
        running = true;
        audioThread = new Thread(() -> {
            while (running) {
                try {
                    if (player == null || audioLine == null || !audioLine.isOpen()) {
                        Thread.sleep(20);
                        continue;
                    }

                    updateVolume();
                    AudioFrame frame = player.provide();
                    if (frame != null) {
                        byte[] pcm = frame.getData();
                        audioLine.write(pcm, 0, pcm.length);
                    } else {
                        Thread.sleep(5);
                    }
                } catch (Exception ignored) {}
            }
        }, "JamMod-AudioOutputThread");
        audioThread.setDaemon(true);
        audioThread.start();
    }

    public static void play(String trackUrl, long offsetMs) {
        init();
        if (playerManager == null || player == null) return;

        player.setPaused(false);
        playerManager.loadItem(trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (offsetMs > 0 && offsetMs < track.getDuration()) {
                    track.setPosition(offsetMs);
                }
                player.playTrack(track);
                sendChatMessage("§6[JamMod]§r Now playing: §a" + track.getInfo().title);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack track = playlist.getSelectedTrack() != null
                        ? playlist.getSelectedTrack()
                        : playlist.getTracks().get(0);

                if (offsetMs > 0 && offsetMs < track.getDuration()) {
                    track.setPosition(offsetMs);
                }
                player.playTrack(track);
                sendChatMessage("§6[JamMod]§r Now playing: §a" + track.getInfo().title);
            }

            @Override
            public void noMatches() {
                sendChatMessage("§c[JamMod] No playable audio found for this URL.");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                sendChatMessage("§c[JamMod] Stream error: " + exception.getMessage());
            }
        });
    }

    public static void setPaused(boolean paused) {
        if (player != null) {
            player.setPaused(paused);
        }
        if (paused && audioLine != null) {
            audioLine.flush();
        }
    }

    public static void stop() {
        if (player != null) {
            player.stopTrack();
        }
        if (audioLine != null) {
            audioLine.flush();
        }
    }

    private static void sendChatMessage(String msg) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.player != null) {
                mc.player.sendSystemMessage(Component.literal(msg));
            }
        });
    }
}