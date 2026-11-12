package com.skl.jammod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.skl.jammod.client.ClientAudioEngine;
import com.skl.jammod.client.ClientJamManager;
import com.skl.jammod.network.C2SJamActionPacket;
import com.skl.jammod.network.ModNetworking;
import com.skl.jammod.network.S2CSyncJamListPacket;
import com.skl.jammod.session.JamEditPolicy;
import com.skl.jammod.session.JamTrack;
import com.skl.jammod.session.SyncedJam;
import com.skl.jammod.client.ClientJamConfig;
import com.skl.jammod.client.OverlayCorner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

import java.util.List;

//? if >=1.20 {
/*import net.minecraft.client.gui.GuiGraphics;
*///?} else {
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
//?}

//? if <=1.18.2 {
import net.minecraft.network.chat.TextComponent;
 //?}

public class JamScreen extends Screen {
    private static final String ICON_NOTE = "♪ ";
    //? if >=1.20.1 {
    /*private static final String ICON_MUSIC = "🎵 ";
    private static final String ICON_MEMBERS = "👥 ";
    private static final String ICON_SETTINGS = "⚙ ";
    private static final String ICON_CLOSE = "✕";
    private static final String ICON_PREV = "⏮";
    private static final String ICON_PLAY = "▶";
    private static final String ICON_PAUSE = "⏸";
    private static final String ICON_NEXT = "⏭";
    private static final String ICON_REPEAT_ALL = "🔁 ALL";
    private static final String ICON_REPEAT_ONE = "🔂 ONE";
    private static final String ICON_REPEAT_OFF = "🔁 OFF";
    private static final String ICON_SHUFFLE_ON = "🔀 ON";
    private static final String ICON_SHUFFLE_OFF = "🔀 OFF";
    private static final String ICON_LINK = "🔗 ";
    private static final String ICON_DELETE = "🗑 ";
    private static final String ICON_INVITE = "✉ ";
    private static final String ICON_LOCK = "🔒 ";
    private static final String BTN_UP = "▲";
    private static final String BTN_DOWN = "▼";
    private static final String BTN_PLAY = "▶";
    private static final String BTN_CLOSE = "✕";
    private static final String BTN_ROLE = "Role ⟳";
    private static final String BULLET = " • ";
    *///?} else {
    private static final String ICON_MUSIC = "";
    private static final String ICON_MEMBERS = "";
    private static final String ICON_SETTINGS = "";
    private static final String ICON_CLOSE = "X";
    private static final String ICON_PREV = "|<";
    private static final String ICON_PLAY = ">";
    private static final String ICON_PAUSE = "||";
    private static final String ICON_NEXT = ">|";
    private static final String ICON_REPEAT_ALL = "Repeat: ALL";
    private static final String ICON_REPEAT_ONE = "Repeat: ONE";
    private static final String ICON_REPEAT_OFF = "Repeat: OFF";
    private static final String ICON_SHUFFLE_ON = "Shuffle: ON";
    private static final String ICON_SHUFFLE_OFF = "Shuffle: OFF";
    private static final String ICON_LINK = "";
    private static final String ICON_DELETE = "";
    private static final String ICON_INVITE = "";
    private static final String ICON_LOCK = "[Private] ";
    private static final String BTN_UP = "^";
    private static final String BTN_DOWN = "v";
    private static final String BTN_PLAY = ">";
    private static final String BTN_CLOSE = "X";
    private static final String BTN_ROLE = "Role >";
    private static final String BULLET = " - ";
    //?}

    public enum Tab {
        QUEUE, MEMBERS, SETTINGS
    }

    private Tab currentTab = Tab.QUEUE;
    private boolean createIsPrivate = false;

    // Header & Navigation
    private Button closeBtn;
    private Button refreshBtn;
    private Button leaveBtn;
    private Button queueTabBtn;
    private Button membersTabBtn;
    private Button settingsTabBtn;

    // Tab 1: Queue Widgets
    private EditBox urlInput;
    private Button addBtn;

    // Tab 2: Members Widgets
    private EditBox inviteInput;
    private Button inviteBtn;

    // Tab 3: Settings Widgets
    private Button privacyBtn;
    private Button policyBtn;
    private Button deleteJamBtn;
    private Button hudToggleBtn;
    private Button hudCornerBtn;

    // Bottom Player Controls
    private Button prevBtn;
    private Button togglePlayBtn;
    private Button skipBtn;
    private Button repeatBtn;
    private Button shuffleBtn;
    private VolumeSlider volumeSlider;

    // Directory View Widgets
    private EditBox nameInput;
    private Button createPrivacyBtn;
    private Button createBtn;

    // Scroll Offsets
    private int queueScrollOffset = 0;
    private int membersScrollOffset = 0;
    private int directoryScrollOffset = 0;

    private static long lastKnownPos = 0L;
    private static int lastTrackIndex = -1;

    public JamScreen() {
        //? if >=1.19 {
        /*super(Component.literal("JamMod"));
        *///?} else {
        super(new TextComponent("JamMod"));
         //?}
    }

    private static Component text(String s) {
        //? if >=1.19 {
        /*return Component.literal(s);
        *///?} else {
        return new TextComponent(s);
         //?}
    }

    private Button createButton(Component message, int x, int y, int width, int height, Button.OnPress onPress) {
        //? if >=1.19.3 {
        /*return Button.builder(message, onPress).bounds(x, y, width, height).build();
        *///?} else {
        return new Button(x, y, width, height, message, onPress);
         //?}
    }

    private static String formatCorner(OverlayCorner corner) {
        if (corner == null) return "Top-Right";
        return switch (corner) {
            case TOP_LEFT -> "Top-Left";
            case TOP_RIGHT -> "Top-Right";
            case BOTTOM_LEFT -> "Bottom-Left";
            case BOTTOM_RIGHT -> "Bottom-Right";
        };
    }

    private static String getHudToggleLabel() {
        return "HUD: " + (ClientJamConfig.isOverlayEnabled() ? "§aEnabled" : "§cDisabled");
    }

    private static String getHudCornerLabel() {
        return "Position: §e" + formatCorner(ClientJamConfig.getOverlayCorner());
    }

    private class VolumeSlider extends AbstractSliderButton {
        public VolumeSlider(int x, int y, int width, int height, double value) {
            super(x, y, width, height, text("Vol: " + (int) Math.round(value * 100.0) + "%"), value);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(text("Vol: " + (int) Math.round(this.value * 100.0) + "%"));
        }

        @Override
        protected void applyValue() {
            Minecraft mc = Minecraft.getInstance();
            //? if >1.19.2 {
            /*mc.options.getSoundSourceOptionInstance(SoundSource.MUSIC).set(this.value);
            *///?} else {
            mc.options.setSoundCategoryVolume(SoundSource.MUSIC, (float) this.value);
             //?}
            mc.options.save();
            ClientAudioEngine.updateVolume();
        }
    }

    @Override
    protected void init() {
        super.init();
        ModNetworking.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.REQUEST_SYNC));

        int centerX = this.width / 2;

        // Top Navigation
        closeBtn = createButton(text(BTN_CLOSE), this.width - 24, 8, 18, 18, b -> this.onClose());
        this.addRenderableWidget(closeBtn);

        refreshBtn = createButton(text("Refresh"), this.width - 82, 8, 55, 18, b -> {
            ModNetworking.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.REQUEST_SYNC));
        });
        this.addRenderableWidget(refreshBtn);

        leaveBtn = createButton(text("Leave"), this.width - 140, 8, 55, 18, b -> {
            ModNetworking.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.LEAVE_JAM));
            this.onClose();
        });
        this.addRenderableWidget(leaveBtn);

        queueTabBtn = createButton(text(ICON_NOTE + "Queue"), 10, 6, 80, 18, b -> {
            currentTab = Tab.QUEUE;
            updateVisibilities();
        });
        this.addRenderableWidget(queueTabBtn);

        membersTabBtn = createButton(text("Members"), 94, 6, 75, 18, b -> {
            currentTab = Tab.MEMBERS;
            updateVisibilities();
        });
        this.addRenderableWidget(membersTabBtn);

        settingsTabBtn = createButton(text("Settings"), 173, 6, 75, 18, b -> {
            currentTab = Tab.SETTINGS;
            updateVisibilities();
        });
        this.addRenderableWidget(settingsTabBtn);

        // Tab 1: Queue Input
        urlInput = new EditBox(this.font, 10, this.height - 82, this.width - 120, 18, text("Stream URL"));
        urlInput.setMaxLength(2048);
        this.addRenderableWidget(urlInput);

        addBtn = createButton(text("+ Add Track"), this.width - 105, this.height - 82, 95, 18, b -> {
            String url = urlInput.getValue().trim();
            if (!url.isEmpty()) {
                ModNetworking.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.ADD_TRACK, "", url, false, 0));
                urlInput.setValue("");
            }
        });
        this.addRenderableWidget(addBtn);

        // Tab 2: Members Input
        inviteInput = new EditBox(this.font, 10, this.height - 82, this.width - 135, 18, text("Player Name"));
        inviteInput.setMaxLength(32);
        this.addRenderableWidget(inviteInput);

        inviteBtn = createButton(text("Invite Player"), this.width - 120, this.height - 82, 110, 18, b -> {
            String name = inviteInput.getValue().trim();
            if (!name.isEmpty()) {
                ModNetworking.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.INVITE_PLAYER, "", name));
                inviteInput.setValue("");
            }
        });
        this.addRenderableWidget(inviteBtn);

        // Tab 3: Settings Buttons (Two-Column Layout)
        int col1X = centerX - 155;
        int col2X = centerX + 5;

        // Column 1: Room Settings
        privacyBtn = createButton(text("Privacy: " + (ClientJamManager.isPrivate() ? "Private" : "Public")), col1X, 50, 150, 20, b -> {
            ModNetworking.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.TOGGLE_PRIVACY));
        });
        this.addRenderableWidget(privacyBtn);

        policyBtn = createButton(text("Edit Policy: " + ClientJamManager.getEditPolicy().name()), col1X, 74, 150, 20, b -> {
            ModNetworking.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.SET_POLICY));
        });
        this.addRenderableWidget(policyBtn);

        deleteJamBtn = createButton(text("Delete Jam Room"), col1X, 98, 150, 20, b -> {
            ModNetworking.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.DELETE_JAM, ClientJamManager.getJamId()));
            this.onClose();
        });
        this.addRenderableWidget(deleteJamBtn);

        // Column 2: Client HUD Settings
        hudToggleBtn = createButton(text(getHudToggleLabel()), col2X, 50, 150, 20, b -> {
            ClientJamConfig.setOverlayEnabled(!ClientJamConfig.isOverlayEnabled());
            hudToggleBtn.setMessage(text(getHudToggleLabel()));
        });
        this.addRenderableWidget(hudToggleBtn);

        hudCornerBtn = createButton(text(getHudCornerLabel()), col2X, 74, 150, 20, b -> {
            OverlayCorner current = ClientJamConfig.getOverlayCorner();
            OverlayCorner next = switch (current) {
                case TOP_LEFT -> OverlayCorner.TOP_RIGHT;
                case TOP_RIGHT -> OverlayCorner.BOTTOM_RIGHT;
                case BOTTOM_RIGHT -> OverlayCorner.BOTTOM_LEFT;
                case BOTTOM_LEFT -> OverlayCorner.TOP_LEFT;
                default -> OverlayCorner.TOP_RIGHT;
            };
            ClientJamConfig.setOverlayCorner(next);
            hudCornerBtn.setMessage(text(getHudCornerLabel()));
        });
        this.addRenderableWidget(hudCornerBtn);

        // Player Controls
        int playerBarY = this.height - 22;
        prevBtn = createButton(text(ICON_PREV), centerX - 42, playerBarY, 24, 18, b -> {
            ModNetworking.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.PREVIOUS));
        });
        this.addRenderableWidget(prevBtn);

        togglePlayBtn = createButton(text(ClientJamManager.isPlaying() ? ICON_PAUSE : ICON_PLAY), centerX - 15, playerBarY, 30, 18, b -> {
            ModNetworking.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.TOGGLE_PLAY));
        });
        this.addRenderableWidget(togglePlayBtn);

        skipBtn = createButton(text(ICON_NEXT), centerX + 18, playerBarY, 24, 18, b -> {
            ModNetworking.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.SKIP));
        });
        this.addRenderableWidget(skipBtn);

        repeatBtn = createButton(text(getRepeatLabel()), 10, playerBarY, 65, 18, b -> {
            ModNetworking.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.CYCLE_REPEAT));
        });
        this.addRenderableWidget(repeatBtn);

        shuffleBtn = createButton(text(ClientJamManager.isShuffle() ? ICON_SHUFFLE_ON : ICON_SHUFFLE_OFF), 78, playerBarY, 60, 18, b -> {
            ModNetworking.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.TOGGLE_SHUFFLE));
        });
        this.addRenderableWidget(shuffleBtn);

        double musicVol = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MUSIC);
        volumeSlider = new VolumeSlider(this.width - 95, playerBarY, 85, 18, musicVol);
        this.addRenderableWidget(volumeSlider);

        // Directory View Inputs
        nameInput = new EditBox(this.font, 10, this.height - 25, this.width - 200, 18, text("Jam Name"));
        nameInput.setMaxLength(32);
        this.addRenderableWidget(nameInput);

        createPrivacyBtn = createButton(text(createIsPrivate ? "[Private]" : "[Public]"), this.width - 185, this.height - 25, 75, 18, b -> {
            createIsPrivate = !createIsPrivate;
            createPrivacyBtn.setMessage(text(createIsPrivate ? "[Private]" : "[Public]"));
        });
        this.addRenderableWidget(createPrivacyBtn);

        createBtn = createButton(text("Create Jam"), this.width - 105, this.height - 25, 95, 18, b -> {
            String name = nameInput.getValue().trim();
            if (!name.isEmpty()) {
                ModNetworking.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.CREATE_JAM, "", name, createIsPrivate, 0));
                nameInput.setValue("");
            }
        });
        this.addRenderableWidget(createBtn);

        updateVisibilities();
    }

    private String getRepeatLabel() {
        SyncedJam.RepeatMode mode = ClientJamManager.getRepeatMode();
        if (mode == null) return ICON_REPEAT_OFF;
        return switch (mode) {
            case ALL -> ICON_REPEAT_ALL;
            case ONE -> ICON_REPEAT_ONE;
            default -> ICON_REPEAT_OFF;
        };
    }

    private void updateVisibilities() {
        boolean inJam = ClientJamManager.isInJam();
        boolean canEdit = ClientJamManager.canEdit();
        boolean isOwner = ClientJamManager.isOwner();
        boolean canManage = ClientJamManager.canManage();

        refreshBtn.visible = true;
        leaveBtn.visible = inJam;
        queueTabBtn.visible = inJam;
        membersTabBtn.visible = inJam;
        settingsTabBtn.visible = inJam;

        if (inJam) {
            queueTabBtn.setMessage(text(ICON_NOTE + "Queue (" + ClientJamManager.getTracks().size() + ")"));
            membersTabBtn.setMessage(text("Members (" + ClientJamManager.getMembers().size() + ")"));
        }

        boolean showQueue = inJam && currentTab == Tab.QUEUE;
        urlInput.visible = showQueue && canEdit;
        addBtn.visible = showQueue && canEdit;

        boolean showMembers = inJam && currentTab == Tab.MEMBERS;
        inviteInput.visible = showMembers && canManage;
        inviteBtn.visible = showMembers && canManage;

        boolean showSettings = inJam && currentTab == Tab.SETTINGS;
        privacyBtn.visible = showSettings && isOwner;
        privacyBtn.setMessage(text("Privacy: " + (ClientJamManager.isPrivate() ? "Private" : "Public")));
        policyBtn.visible = showSettings && canManage;
        policyBtn.setMessage(text("Edit Policy: " + ClientJamManager.getEditPolicy().name()));
        deleteJamBtn.visible = showSettings && isOwner;

        // HUD preferences are always accessible to any player in the room
        hudToggleBtn.visible = showSettings;
        hudToggleBtn.setMessage(text(getHudToggleLabel()));
        hudCornerBtn.visible = showSettings;
        hudCornerBtn.setMessage(text(getHudCornerLabel()));

        prevBtn.visible = inJam && canEdit;
        togglePlayBtn.visible = inJam && canEdit;
        togglePlayBtn.setMessage(text(ClientJamManager.isPlaying() ? ICON_PAUSE : ICON_PLAY));
        skipBtn.visible = inJam && canEdit;
        repeatBtn.visible = inJam && canEdit;
        repeatBtn.setMessage(text(getRepeatLabel()));
        shuffleBtn.visible = inJam && canEdit;
        shuffleBtn.setMessage(text(ClientJamManager.isShuffle() ? ICON_SHUFFLE_ON : ICON_SHUFFLE_OFF));
        volumeSlider.visible = inJam;

        nameInput.visible = !inJam;
        createPrivacyBtn.visible = !inJam;
        createBtn.visible = !inJam;
    }

    private long getCurrentDuration() {
        int idx = ClientJamManager.getCurrentTrackIndex();
        List<JamTrack> tracks = ClientJamManager.getTracks();
        if (idx >= 0 && idx < tracks.size()) {
            return tracks.get(idx).getDurationMs();
        }
        return 0L;
    }

    private static String formatTime(long ms) {
        if (ms <= 0) return "00:00";
        long totalSec = ms / 1000L;
        long min = totalSec / 60L;
        long sec = totalSec % 60L;
        return String.format("%02d:%02d", min, sec);
    }

    // Defensive helper to ensure the song title is always on line 1 and the URL on line 2
    private static String[] resolveDisplayNames(String rawTitle, String rawUrl) {
        String title = (rawTitle != null) ? rawTitle.trim() : "";
        String url = (rawUrl != null) ? rawUrl.trim() : "";

        boolean titleIsUrl = title.startsWith("http://") || title.startsWith("https://") || title.contains("youtu.be");
        boolean urlIsUrl = url.startsWith("http://") || url.startsWith("https://") || url.contains("youtu.be");

        if (titleIsUrl && !urlIsUrl && !url.isBlank()) {
            String tmp = title;
            title = url;
            url = tmp;
        }

        if (title.isBlank() && !url.isBlank()) {
            title = url;
            url = "";
        } else if (title.equalsIgnoreCase(url)) {
            url = "";
        }

        return new String[]{title, url};
    }

    //? if >1.20.1 {
    /*@Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
    *///?} else if >1.19.4 {
    /*@Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        *///?} else {
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
    //?}
        updateVisibilities();

        boolean inJam = ClientJamManager.isInJam();
        if (inJam) {
            renderSession(
                    //? if >=1.20 {
                    /*graphics,
                    *///?} else {
                    poseStack,
                     //?}
                    mouseX, mouseY
            );
        } else {
            renderDirectory(
                    //? if >=1.20 {
                    /*graphics,
                    *///?} else {
                    poseStack,
                     //?}
                    mouseX, mouseY
            );
        }

        //? if >=1.20 {
        /*super.render(graphics, mouseX, mouseY, partialTick);
        *///?} else {
        super.render(poseStack, mouseX, mouseY, partialTick);
         //?}
    }

    //? if >=1.20 {
    /*private void renderSession(GuiGraphics graphics, int mouseX, int mouseY) {
        *///?} else {
        private void renderSession(PoseStack poseStack, int mouseX, int mouseY) {
         //?}
        switch (currentTab) {
            case QUEUE -> renderQueueTab(
                    //? if >=1.20 {
                    /*graphics,
                    *///?} else {
                    poseStack,
                     //?}
                    mouseX, mouseY
            );
            case MEMBERS -> renderMembersTab(
                    //? if >=1.20 {
                    /*graphics,
                    *///?} else {
                    poseStack,
                     //?}
                    mouseX, mouseY
            );
            case SETTINGS -> renderSettingsTab(
                    //? if >=1.20 {
                    /*graphics
                    *///?} else {
                    poseStack
                     //?}
            );
        }

        // Expanded, fully-opaque player bar (no chat bleed)
        int barH = 56;
        int barY = this.height - barH;

        //? if >=1.20 {
        /*graphics.fill(0, barY, this.width, this.height, 0xFF111114);
        graphics.fill(0, barY, this.width, barY + 1, 0xFFFFAA00);
        *///?} else {
        GuiComponent.fill(poseStack, 0, barY, this.width, this.height, 0xFF111114);
        GuiComponent.fill(poseStack, 0, barY, this.width, barY + 1, 0xFFFFAA00);
        //?}

        // Reset cached timeline position if track changed
        int currentTrackIdx = ClientJamManager.getCurrentTrackIndex();
        if (currentTrackIdx != lastTrackIndex) {
            lastTrackIndex = currentTrackIdx;
            lastKnownPos = 0L;
        }

        long dur = getCurrentDuration();
        long pos = 0L;

        if (ClientJamManager.isPlaying()) {
            pos = System.currentTimeMillis() - ClientJamManager.getStartTimestamp();
            if (pos > 0) {
                lastKnownPos = pos;
            }
        } else {
            // When paused, query Lavaplayer's frozen playback position
            pos = ClientAudioEngine.getPosition();
            // Fallback to last known position if audio engine is buffering/flushed
            if (pos <= 0 && lastKnownPos > 0) {
                pos = lastKnownPos;
            }
        }

        if (dur > 0 && pos > dur) pos = dur;
        if (pos < 0) pos = 0;

        String timeStr = formatTime(pos) + " / " + formatTime(dur);

        int maxTitleW = this.width - this.font.width(timeStr) - 30;

        String rawTitle = ClientJamManager.getCurrentTrackTitle();
        String rawUrl = ClientJamManager.getCurrentTrackUrl();

        String[] resolved = resolveDisplayNames(rawTitle, rawUrl);
        String songName = resolved[0].isBlank() ? "No track playing" : resolved[0];
        String songUrl = resolved[1];

        String displayTitle = songName;
        if (this.font.width(displayTitle) > maxTitleW) {
            while (displayTitle.length() > 4 && this.font.width(displayTitle + "...") > maxTitleW) {
                displayTitle = displayTitle.substring(0, displayTitle.length() - 1);
            }
            displayTitle += "...";
        }

        // Line 1: Song Name in white
        //? if >=1.20 {
        /*graphics.drawString(this.font, "§6" + ICON_NOTE + "Now Playing: §f" + displayTitle, 10, barY + 4, 0xFFFFFF);
        graphics.drawString(this.font, "§e" + timeStr, this.width - this.font.width(timeStr) - 10, barY + 4, 0xFFFFFF);
        *///?} else {
        this.font.draw(poseStack, "§6" + ICON_NOTE + "Now Playing: §f" + displayTitle, 10, barY + 4, 0xFFFFFF);
        this.font.draw(poseStack, "§e" + timeStr, this.width - this.font.width(timeStr) - 10, barY + 4, 0xFFFFFF);
        //?}

        // Line 2: URL Grayed Underneath
        if (!songUrl.isBlank()) {
            int maxUrlW = this.width - 25;
            String displayUrl = songUrl;
            if (this.font.width(displayUrl) > maxUrlW) {
                while (displayUrl.length() > 4 && this.font.width(displayUrl + "...") > maxUrlW) {
                    displayUrl = displayUrl.substring(0, displayUrl.length() - 1);
                }
                displayUrl += "...";
            }

            //? if >=1.20 {
            /*graphics.drawString(this.font, "§7" + displayUrl + " §8(Click to Copy)", 10, barY + 15, 0x888888);
            *///?} else {
            this.font.draw(poseStack, "§7" + displayUrl + " §8(Click to Copy)", 10, barY + 15, 0x888888);
             //?}
        }

        // Timeline Progress Bar
        int progressX = 10;
        int progressW = this.width - 20;
        int progressY = barY + 25;

        //? if >=1.20 {
        /*graphics.fill(progressX, progressY, progressX + progressW, progressY + 4, 0xFF333333);
        if (dur > 0) {
            int filled = (int) (((double) pos / dur) * progressW);
            graphics.fill(progressX, progressY, progressX + filled, progressY + 4, 0xFFFFAA00);
            graphics.fill(progressX + filled - 1, progressY - 1, progressX + filled + 2, progressY + 5, 0xFFFFFFFF);
        }
        *///?} else {
        GuiComponent.fill(poseStack, progressX, progressY, progressX + progressW, progressY + 4, 0xFF333333);
        if (dur > 0) {
            int filled = (int) (((double) pos / dur) * progressW);
            GuiComponent.fill(poseStack, progressX, progressY, progressX + filled, progressY + 4, 0xFFFFAA00);
            GuiComponent.fill(poseStack, progressX + filled - 1, progressY - 1, progressX + filled + 2, progressY + 5, 0xFFFFFFFF);
        }
        //?}
    }

    //? if >=1.20 {
    /*private void renderQueueTab(GuiGraphics graphics, int mouseX, int mouseY) {
        *///?} else {
        private void renderQueueTab(PoseStack poseStack, int mouseX, int mouseY) {
         //?}
        List<JamTrack> tracks = ClientJamManager.getTracks();
        int listY = 30;
        int listH = this.height - 118;
        boolean canEdit = ClientJamManager.canEdit();

        if (tracks.isEmpty()) {
            //? if >=1.20 {
            /*graphics.drawCenteredString(this.font, "§7The queue is empty. Paste a URL below to add audio!", this.width / 2, listY + 30, 0x888888);
            *///?} else {
            drawCenteredString(poseStack, this.font, "§7The queue is empty. Paste a URL below to add audio!", this.width / 2, listY + 30, 0x888888);
             //?}
            return;
        }

        int rowHeight = 20;
        for (int i = queueScrollOffset; i < tracks.size() && (i - queueScrollOffset) * rowHeight < listH; i++) {
            JamTrack t = tracks.get(i);
            int y = listY + (i - queueScrollOffset) * rowHeight;
            boolean isCur = (i == ClientJamManager.getCurrentTrackIndex());

            if (mouseX >= 8 && mouseX <= this.width - 8 && mouseY >= y && mouseY < y + 18) {
                //? if >=1.20 {
                /*graphics.fill(8, y - 1, this.width - 8, y + 18, 0x22FFFFFF);
                *///?} else {
                GuiComponent.fill(poseStack, 8, y - 1, this.width - 8, y + 18, 0x22FFFFFF);
                 //?}
            }

            String prefix = isCur ? "§a" + BTN_PLAY + " " : "§7";
            String[] resolved = resolveDisplayNames(t.getTitle(), t.getUrl());
            String title = resolved[0];
            String url = resolved[1];

            if (title.length() > 36) title = title.substring(0, 34) + "...";
            String dur = t.getDurationMs() > 0 ? " §8(" + formatTime(t.getDurationMs()) + ")" : "";

            if (!url.isBlank()) {
                String subUrl = url.length() > 46 ? url.substring(0, 43) + "..." : url;
                // Line 1: Song Title
                //? if >=1.20 {
                /*graphics.drawString(this.font, prefix + (i + 1) + ". §f" + title + dur, 12, y + 1, 0xFFFFFF);
                graphics.drawString(this.font, "   §7" + subUrl, 12, y + 10, 0x888888);
                *///?} else {
                this.font.draw(poseStack, prefix + (i + 1) + ". §f" + title + dur, 12, y + 1, 0xFFFFFF);
                this.font.draw(poseStack, "   §7" + subUrl, 12, y + 10, 0x888888);
                //?}
            } else {
                //? if >=1.20 {
                /*graphics.drawString(this.font, prefix + (i + 1) + ". §f" + title + dur, 12, y + 5, 0xFFFFFF);
                *///?} else {
                this.font.draw(poseStack, prefix + (i + 1) + ". §f" + title + dur, 12, y + 5, 0xFFFFFF);
                 //?}
            }

            if (canEdit) {
                drawMiniButton(
                        //? if >=1.20 {
                        /*graphics,
                        *///?} else {
                        poseStack,
                         //?}
                        BTN_PLAY, this.width - 76, y + 3, 14, 13, mouseX, mouseY
                );
                drawMiniButton(
                        //? if >=1.20 {
                        /*graphics,
                        *///?} else {
                        poseStack,
                         //?}
                        BTN_UP, this.width - 58, y + 3, 14, 13, mouseX, mouseY
                );
                drawMiniButton(
                        //? if >=1.20 {
                        /*graphics,
                        *///?} else {
                        poseStack,
                         //?}
                        BTN_DOWN, this.width - 40, y + 3, 14, 13, mouseX, mouseY
                );
                drawMiniButton(
                        //? if >=1.20 {
                        /*graphics,
                        *///?} else {
                        poseStack,
                         //?}
                        BTN_CLOSE, this.width - 22, y + 3, 14, 13, mouseX, mouseY
                );
            }
        }
    }

    //? if >=1.20 {
    /*private void renderMembersTab(GuiGraphics graphics, int mouseX, int mouseY) {
        *///?} else {
        private void renderMembersTab(PoseStack poseStack, int mouseX, int mouseY) {
         //?}
        List<SyncedJam.MemberInfo> members = ClientJamManager.getMembers();
        int listY = 30;
        int listH = this.height - 118;
        boolean canManage = ClientJamManager.canManage();

        for (int i = membersScrollOffset; i < members.size() && (i - membersScrollOffset) * 16 < listH; i++) {
            SyncedJam.MemberInfo m = members.get(i);
            int y = listY + (i - membersScrollOffset) * 16;

            if (mouseX >= 8 && mouseX <= this.width - 8 && mouseY >= y && mouseY < y + 14) {
                //? if >=1.20 {
                /*graphics.fill(8, y - 1, this.width - 8, y + 14, 0x22FFFFFF);
                *///?} else {
                GuiComponent.fill(poseStack, 8, y - 1, this.width - 8, y + 14, 0x22FFFFFF);
                 //?}
            }

            String roleColor = switch (m.role()) {
                case OWNER -> "§6[Host]";
                case ADMIN -> "§c[Admin]";
                case EDITOR -> "§b[Editor]";
                case LISTENER -> "§7[Listener]";
            };

            //? if >=1.20 {
            /*graphics.drawString(this.font, roleColor + " §f" + m.name(), 12, y + 2, 0xFFFFFF);
            *///?} else {
            this.font.draw(poseStack, roleColor + " §f" + m.name(), 12, y + 2, 0xFFFFFF);
             //?}

            if (canManage && m.role() != SyncedJam.Role.OWNER) {
                drawMiniButton(
                        //? if >=1.20 {
                        /*graphics,
                        *///?} else {
                        poseStack,
                         //?}
                        "Role >", this.width - 76, y, 42, 12, mouseX, mouseY
                );
                drawMiniButton(
                        //? if >=1.20 {
                        /*graphics,
                        *///?} else {
                        poseStack,
                         //?}
                        "Kick", this.width - 30, y, 22, 12, mouseX, mouseY
                );
            }
        }
    }

    //? if >=1.20 {
    /*private void renderSettingsTab(GuiGraphics graphics) {
        *///?} else {
        private void renderSettingsTab(PoseStack poseStack) {
         //?}
        int centerX = this.width / 2;
        boolean canManage = ClientJamManager.canManage();

        //? if >=1.20 {
        /*if (canManage) {
            graphics.drawCenteredString(this.font, "§6§lRoom Settings", centerX - 80, 36, 0xFFFFFF);
        } else {
            graphics.drawCenteredString(this.font, "§7(Room managed by host)", centerX - 80, 60, 0x888888);
        }
        graphics.drawCenteredString(this.font, "§e§lHUD Overlay", centerX + 80, 36, 0xFFFFFF);
        *///?} else {
        if (canManage) {
            drawCenteredString(poseStack, this.font, "§6§lRoom Settings", centerX - 80, 36, 0xFFFFFF);
        } else {
            drawCenteredString(poseStack, this.font, "§7(Room managed by host)", centerX - 80, 60, 0x888888);
        }
        drawCenteredString(poseStack, this.font, "§e§lHUD Overlay", centerX + 80, 36, 0xFFFFFF);
        //?}
    }

    //? if >=1.20 {
    /*private void renderDirectory(GuiGraphics graphics, int mouseX, int mouseY) {
        *///?} else {
        private void renderDirectory(PoseStack poseStack, int mouseX, int mouseY) {
         //?}
        int centerX = this.width / 2;

        //? if >=1.20 {
        /*graphics.drawCenteredString(this.font, "§6§lActive Jam Sessions", centerX, 10, 0xFFFFFF);
        *///?} else {
        drawCenteredString(poseStack, this.font, "§6§lActive Jam Sessions", centerX, 10, 0xFFFFFF);
         //?}

        List<S2CSyncJamListPacket.JamSummary> dir = ClientJamManager.getDirectory();

        if (dir.isEmpty()) {
            int cardW = 320;
            int cardH = 145;
            int cardX = centerX - (cardW / 2);
            int cardY = 32;

            //? if >=1.20 {
            /*graphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, 0xDD121212);
            graphics.fill(cardX, cardY, cardX + cardW, cardY + 2, 0xFFFFAA00);
            graphics.drawCenteredString(this.font, "§6§lWelcome to JamMod!", centerX, cardY + 8, 0xFFFFFF);
            graphics.drawCenteredString(this.font, "§7No active jam sessions found on this server.", centerX, cardY + 20, 0xAAAAAA);

            graphics.drawString(this.font, "§e1. Create a Session:", cardX + 12, cardY + 36, 0xFFFFFF);
            graphics.drawString(this.font, "§7   Enter a name below and click §a[Create Jam]§7 (or §f/jam create§7).", cardX + 12, cardY + 47, 0xCCCCCC);

            graphics.drawString(this.font, "§e2. Add Music & Audio:", cardX + 12, cardY + 61, 0xFFFFFF);
            graphics.drawString(this.font, "§7   Paste YouTube, SoundCloud, or stream URLs with §a[+ Add Track]§7.", cardX + 12, cardY + 72, 0xCCCCCC);

            graphics.drawString(this.font, "§e3. Synchronized Listening:", cardX + 12, cardY + 86, 0xFFFFFF);
            graphics.drawString(this.font, "§7   Everyone in your room hears the same song at the exact millisecond.", cardX + 12, cardY + 97, 0xCCCCCC);

            graphics.drawString(this.font, "§e4. Manage & Invite:", cardX + 12, cardY + 111, 0xFFFFFF);
            graphics.drawString(this.font, "§7   Promote friends to §bEditor§7 or §cAdmin§7, or invite via §f/jam invite§7.", cardX + 12, cardY + 122, 0xCCCCCC);
            *///?} else {
            GuiComponent.fill(poseStack, cardX, cardY, cardX + cardW, cardY + cardH, 0xDD121212);
            GuiComponent.fill(poseStack, cardX, cardY, cardX + cardW, cardY + 2, 0xFFFFAA00);
            drawCenteredString(poseStack, this.font, "§6§lWelcome to JamMod!", centerX, cardY + 8, 0xFFFFFF);
            drawCenteredString(poseStack, this.font, "§7No active jam sessions found on this server.", centerX, cardY + 20, 0xAAAAAA);

            this.font.draw(poseStack, "§e1. Create a Session:", cardX + 12, cardY + 36, 0xFFFFFF);
            this.font.draw(poseStack, "§7   Enter a name below and click §a[Create Jam]§7 (or §f/jam create§7).", cardX + 12, cardY + 47, 0xCCCCCC);

            this.font.draw(poseStack, "§e2. Add Music & Audio:", cardX + 12, cardY + 61, 0xFFFFFF);
            this.font.draw(poseStack, "§7   Paste YouTube, SoundCloud, or stream URLs with §a[+ Add Track]§7.", cardX + 12, cardY + 72, 0xCCCCCC);

            this.font.draw(poseStack, "§e3. Synchronized Listening:", cardX + 12, cardY + 86, 0xFFFFFF);
            this.font.draw(poseStack, "§7   Everyone in your room hears the same song at the exact millisecond.", cardX + 12, cardY + 97, 0xCCCCCC);

            this.font.draw(poseStack, "§e4. Manage & Invite:", cardX + 12, cardY + 111, 0xFFFFFF);
            this.font.draw(poseStack, "§7   Promote friends to §bEditor§7 or §cAdmin§7, or invite via §f/jam invite§7.", cardX + 12, cardY + 122, 0xCCCCCC);
            //?}
        } else {
            int listY = 28;
            int listH = this.height - 62;
            for (int i = directoryScrollOffset; i < dir.size() && (i - directoryScrollOffset) * 22 < listH; i++) {
                S2CSyncJamListPacket.JamSummary sum = dir.get(i);
                int y = listY + (i - directoryScrollOffset) * 22;

                if (mouseX >= 8 && mouseX <= this.width - 8 && mouseY >= y && mouseY < y + 20) {
                    //? if >=1.20 {
                    /*graphics.fill(8, y, this.width - 8, y + 20, 0x22FFFFFF);
                    *///?} else {
                    GuiComponent.fill(poseStack, 8, y, this.width - 8, y + 20, 0x22FFFFFF);
                     //?}
                }

                String privacyBadge = sum.isPrivate() ? "§c[Private]" : "§a[Public]";
                String playing = sum.isPlaying() ? "§6" + ICON_NOTE + sum.currentTrackTitle() : "§8(Idle)";

                //? if >=1.20 {
                /*graphics.drawString(this.font, "§f" + sum.name() + " " + privacyBadge + " §7by " + sum.ownerName(), 12, y + 2, 0xFFFFFF);
                graphics.drawString(this.font, "§7" + sum.listeners() + " listeners - " + playing, 12, y + 11, 0x888888);
                *///?} else {
                this.font.draw(poseStack, "§f" + sum.name() + " " + privacyBadge + " §7by " + sum.ownerName(), 12, y + 2, 0xFFFFFF);
                this.font.draw(poseStack, "§7" + sum.listeners() + " listeners - " + playing, 12, y + 11, 0x888888);
                //?}

                if (!sum.isPrivate()) {
                    drawMiniButton(
                            //? if >=1.20 {
                            /*graphics,
                            *///?} else {
                            poseStack,
                             //?}
                            "Join", this.width - 50, y + 3, 38, 14, mouseX, mouseY
                    );
                }
            }
        }
    }

    //? if >=1.20 {
    /*private void drawMiniButton(GuiGraphics graphics, String label, int x, int y, int w, int h, int mouseX, int mouseY) {
        *///?} else {
        private void drawMiniButton(PoseStack poseStack, String label, int x, int y, int w, int h, int mouseX, int mouseY) {
         //?}
        boolean hovered = (mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h);
        int bg = hovered ? 0xDD444444 : 0xAA222222;
        int border = hovered ? 0xFFFFAA00 : 0xFF555555;

        //? if >=1.20 {
        /*graphics.fill(x, y, x + w, y + h, bg);
        graphics.fill(x, y, x + w, y + 1, border);
        graphics.fill(x, y + h - 1, x + w, y + h, border);
        graphics.fill(x, y, x + 1, y + h, border);
        graphics.fill(x + w - 1, y, x + w, y + h, border);
        graphics.drawCenteredString(this.font, label, x + (w / 2), y + (h - 8) / 2, hovered ? 0xFFFFFF : 0xCCCCCC);
        *///?} else {
        GuiComponent.fill(poseStack, x, y, x + w, y + h, bg);
        GuiComponent.fill(poseStack, x, y, x + w, y + 1, border);
        GuiComponent.fill(poseStack, x, y + h - 1, x + w, y + h, border);
        GuiComponent.fill(poseStack, x, y, x + 1, y + h, border);
        GuiComponent.fill(poseStack, x + w - 1, y, x + w, y + h, border);
        drawCenteredString(poseStack, this.font, label, x + (w / 2), y + (h - 8) / 2, hovered ? 0xFFFFFF : 0xCCCCCC);
        //?}
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean inJam = ClientJamManager.isInJam();

        if (inJam) {
            int barH = 56;
            int barY = this.height - barH;

            // Copy Link
            if (mouseY >= barY + 13 && mouseY <= barY + 23 && mouseX >= 10 && mouseX <= this.width - 10) {
                String url = ClientJamManager.getCurrentTrackUrl();
                if (url != null && !url.isEmpty()) {
                    Minecraft.getInstance().keyboardHandler.setClipboard(url);
                    return true;
                }
            }

            // Seekbar Click
            int progressX = 10;
            int progressW = this.width - 20;
            int progressY = barY + 25;

            if (mouseY >= progressY - 3 && mouseY <= progressY + 7 && mouseX >= progressX && mouseX <= progressX + progressW) {
                if (ClientJamManager.canEdit()) {
                    long dur = getCurrentDuration();
                    if (dur > 0) {
                        float pct = (float) (mouseX - progressX) / progressW;
                        long target = (long) (dur * pct);
                        lastKnownPos = target;
                        ModNetworking.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.SEEK, String.valueOf(target)));
                        return true;
                    }
                }
            }

            // Tab 1: Queue Actions
            if (currentTab == Tab.QUEUE && ClientJamManager.canEdit()) {
                List<JamTrack> tracks = ClientJamManager.getTracks();
                int listY = 30;
                int listH = this.height - 118;
                int rowHeight = 20;
                for (int i = queueScrollOffset; i < tracks.size() && (i - queueScrollOffset) * rowHeight < listH; i++) {
                    int y = listY + (i - queueScrollOffset) * rowHeight;
                    if (mouseY >= y && mouseY < y + 18) {
                        if (mouseX >= this.width - 76 && mouseX < this.width - 62) {
                            ModNetworking.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.JUMP_TRACK, "", "", false, i));
                            return true;
                        }
                        if (mouseX >= this.width - 58 && mouseX < this.width - 44 && i > 0) {
                            ModNetworking.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.MOVE_TRACK, "", "", false, i, i - 1));
                            return true;
                        }
                        if (mouseX >= this.width - 40 && mouseX < this.width - 26 && i < tracks.size() - 1) {
                            ModNetworking.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.MOVE_TRACK, "", "", false, i, i + 1));
                            return true;
                        }
                        if (mouseX >= this.width - 22 && mouseX < this.width - 8) {
                            ModNetworking.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.REMOVE_TRACK, "", "", false, i));
                            return true;
                        }
                    }
                }
            }

            // Tab 2: Members Actions
            if (currentTab == Tab.MEMBERS && ClientJamManager.canManage()) {
                List<SyncedJam.MemberInfo> members = ClientJamManager.getMembers();
                int listY = 30;
                int listH = this.height - 118;
                for (int i = membersScrollOffset; i < members.size() && (i - membersScrollOffset) * 16 < listH; i++) {
                    SyncedJam.MemberInfo m = members.get(i);
                    int y = listY + (i - membersScrollOffset) * 16;
                    if (m.role() != SyncedJam.Role.OWNER && mouseY >= y && mouseY < y + 14) {
                        if (mouseX >= this.width - 76 && mouseX < this.width - 34) {
                            ModNetworking.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.CYCLE_MEMBER_ROLE, m.uuid().toString()));
                            return true;
                        }
                        if (mouseX >= this.width - 30 && mouseX < this.width - 8) {
                            ModNetworking.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.REMOVE_MEMBER, m.uuid().toString()));
                            return true;
                        }
                    }
                }
            }
        } else {
            List<S2CSyncJamListPacket.JamSummary> dir = ClientJamManager.getDirectory();
            int listY = 28;
            int listH = this.height - 62;
            for (int i = directoryScrollOffset; i < dir.size() && (i - directoryScrollOffset) * 22 < listH; i++) {
                S2CSyncJamListPacket.JamSummary sum = dir.get(i);
                int y = listY + (i - directoryScrollOffset) * 22;
                if (!sum.isPrivate() && mouseY >= y && mouseY < y + 20 && mouseX >= 8 && mouseX <= this.width - 8) {
                    ModNetworking.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.JOIN_JAM, sum.id()));
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    //? if >1.20.1 {
    /*@Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double delta = scrollY;
    *///?} else {
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        //?}
        if (ClientJamManager.isInJam()) {
            if (currentTab == Tab.QUEUE) {
                queueScrollOffset -= (int) Math.signum(delta);
                queueScrollOffset = Math.max(0, Math.min(queueScrollOffset, ClientJamManager.getTracks().size() - 1));
            } else if (currentTab == Tab.MEMBERS) {
                membersScrollOffset -= (int) Math.signum(delta);
                membersScrollOffset = Math.max(0, Math.min(membersScrollOffset, ClientJamManager.getMembers().size() - 1));
            }
        } else {
            directoryScrollOffset -= (int) Math.signum(delta);
            directoryScrollOffset = Math.max(0, Math.min(directoryScrollOffset, ClientJamManager.getDirectory().size() - 1));
        }
        return true;
    }

    //? if >1.20.4 {
    /*@Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(graphics);
        graphics.fill(0, 0, this.width, this.height, 0xCC000000);
    }
    *///?} else if >1.20.1 {
    /*@Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(0, 0, this.width, this.height, 0xCC000000);
    }
    *///?} else if >1.19.4 {
    /*@Override
    public void renderBackground(GuiGraphics graphics) {
        super.renderBackground(graphics);
        graphics.fill(0, 0, this.width, this.height, 0xCC000000);
    }
    *///?} else {
    @Override
    public void renderBackground(PoseStack poseStack) {
        super.renderBackground(poseStack);
        GuiComponent.fill(poseStack, 0, 0, this.width, this.height, 0xCC000000);
    }
    //?}

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}