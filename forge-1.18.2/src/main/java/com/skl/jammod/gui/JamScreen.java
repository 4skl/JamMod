package com.skl.jammod.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.skl.jammod.client.ClientAudioEngine;
import com.skl.jammod.client.ClientJamConfig;
import com.skl.jammod.client.ClientJamManager;
import com.skl.jammod.network.C2SJamActionPacket;
import com.skl.jammod.network.ModNetworking;
import com.skl.jammod.network.S2CSyncJamListPacket.JamSummary;
import com.skl.jammod.session.JamTrack;
import com.skl.jammod.session.SyncedJam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundSource;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.UUID;

public class JamScreen extends Screen {
    public enum View { PLAYER, ADD_TRACK, SETTINGS, ROLES, INVITE, DIRECTORY_PUBLIC, DIRECTORY_MY, CREATE_JAM }
    private View currentView = View.PLAYER;

    private EditBox inputField;
    private EditBox titleField;
    private EditBox addMemberField;
    private boolean createAsPrivate = false;

    private int page = 0;
    private static final int ITEMS_PER_PAGE = 5;

    private Button prevPageBtn;
    private Button nextPageBtn;

    private Button playPauseButton;
    private Button repeatButton;
    private Button shuffleButton;
    private Button policyButton;
    private Button privacyButton;
    private Button overlayToggleBtn;
    private Button overlayCornerBtn;
    private AbstractSliderButton volumeSlider;

    public JamScreen() {
        super(new TextComponent("Jam Session Manager"));
    }

    private Button btn(int x, int y, int w, int h, Component title, Button.OnPress onPress) {
        return new Button(x, y, w, h, title, onPress);
    }

    private Button btn(int x, int y, int w, int h, Component title, Button.OnPress onPress, Component tooltip) {
        return new Button(x, y, w, h, title, onPress, (b, ps, mx, my) -> this.renderTooltip(ps, tooltip, mx, my));
    }

    @Override
    protected void init() {
        ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.REQUEST_SYNC));

        if (!ClientJamManager.isInJam() && (currentView == View.PLAYER || currentView == View.ADD_TRACK || currentView == View.SETTINGS || currentView == View.ROLES || currentView == View.INVITE)) {
            currentView = View.DIRECTORY_PUBLIC;
        }

        int midX = this.width / 2;

        this.addRenderableWidget(btn(midX - 180, 8, 115, 20, new TextComponent("Now Playing"), b -> switchView(View.PLAYER)));
        this.addRenderableWidget(btn(midX - 60, 8, 120, 20, new TextComponent("Public Jams"), b -> switchView(View.DIRECTORY_PUBLIC)));
        this.addRenderableWidget(btn(midX + 65, 8, 115, 20, new TextComponent("My Jams"), b -> switchView(View.DIRECTORY_MY)));

        initViewWidgets();
    }

    private void switchView(View view) {
        this.currentView = view;
        this.page = 0;
        this.clearWidgets();
        init();
    }

    private int getPaginationY() {
        return switch (currentView) {
            case PLAYER -> 204;
            case ROLES -> 176;
            case INVITE -> 180;
            case DIRECTORY_PUBLIC, DIRECTORY_MY -> 188;
            default -> 190;
        };
    }

    private void initViewWidgets() {
        int midX = this.width / 2;

        if (currentView == View.PLAYER || currentView == View.INVITE || currentView == View.DIRECTORY_PUBLIC || currentView == View.DIRECTORY_MY || currentView == View.ROLES) {
            int py = getPaginationY();
            this.prevPageBtn = btn(midX - 75, py, 48, 16, new TextComponent("◄ Prev"), b -> { if (page > 0) page--; });
            this.nextPageBtn = btn(midX + 27, py, 48, 16, new TextComponent("Next ►"), b -> page++);
            this.addRenderableWidget(prevPageBtn);
            this.addRenderableWidget(nextPageBtn);
        }

        switch (currentView) {
            case PLAYER -> {
                if (!ClientJamManager.isInJam()) return;

                if (ClientJamManager.canEdit()) {
                    this.addRenderableWidget(btn(midX - 180, 32, 55, 18, new TextComponent("➕ Add"), b -> switchView(View.ADD_TRACK)));

                    this.addRenderableWidget(btn(midX - 120, 32, 24, 18, new TextComponent("⏮"), b ->
                            ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.PREVIOUS))
                    ));

                    playPauseButton = btn(midX - 93, 32, 24, 18, new TextComponent(ClientJamManager.isPlaying() ? "⏸" : "▶"), b ->
                            ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.TOGGLE_PLAY))
                    );
                    this.addRenderableWidget(playPauseButton);

                    this.addRenderableWidget(btn(midX - 66, 32, 24, 18, new TextComponent("⏭"), b ->
                            ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.SKIP))
                    ));

                    String repSymbol = switch (ClientJamManager.getRepeatMode()) {
                        case ALL -> "🔁";
                        case ONE -> "🔂";
                        default -> "➡";
                    };
                    repeatButton = btn(midX - 39, 32, 24, 18, new TextComponent(repSymbol), b ->
                                    ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.CYCLE_REPEAT)),
                            new TextComponent("Repeat Mode: Off / All / One")
                    );
                    this.addRenderableWidget(repeatButton);

                    shuffleButton = btn(midX - 12, 32, 24, 18, new TextComponent(ClientJamManager.isShuffle() ? "§a🔀" : "§7🔀"), b ->
                                    ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.TOGGLE_SHUFFLE)),
                            new TextComponent("Toggle Shuffle")
                    );
                    this.addRenderableWidget(shuffleButton);
                }

                this.addRenderableWidget(btn(midX + 16, 32, 24, 18, new TextComponent("👥"), b -> switchView(View.INVITE), new TextComponent("Invite Online Players")));
                this.addRenderableWidget(btn(midX + 43, 32, 24, 18, new TextComponent("⚙"), b -> switchView(View.SETTINGS), new TextComponent("Settings & Preferences")));

                this.addRenderableWidget(btn(midX + 130, 32, 50, 18, new TextComponent("Leave"), b -> {
                    ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.LEAVE_JAM));
                    switchView(View.DIRECTORY_PUBLIC);
                }));

                Minecraft mc = Minecraft.getInstance();
                double currentMusic = mc.options.getSoundSourceVolume(SoundSource.MUSIC);
                this.volumeSlider = new AbstractSliderButton(midX - 180, this.height - 18, 130, 14,
                        new TextComponent("Music: " + Math.round(currentMusic * 100) + "%"), currentMusic) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TextComponent("Music: " + Math.round(this.value * 100) + "%"));
                    }

                    @Override
                    protected void applyValue() {
                        Minecraft mc = Minecraft.getInstance();
                        mc.options.setSoundCategoryVolume(SoundSource.MUSIC, (float) this.value);
                        mc.options.save();
                        ClientAudioEngine.updateVolume();
                    }
                };
                this.addRenderableWidget(volumeSlider);
            }

            case ADD_TRACK -> {
                this.inputField = new EditBox(this.font, midX - 180, 50, 360, 20, new TextComponent("Media URL"));
                this.inputField.setMaxLength(1024);
                this.inputField.setSuggestion("Paste YouTube / Media URL...");
                this.addRenderableWidget(inputField);

                this.titleField = new EditBox(this.font, midX - 180, 78, 360, 20, new TextComponent("Song Title"));
                this.titleField.setMaxLength(128);
                this.titleField.setSuggestion("Optional Title (auto-resolves from web if empty)");
                this.addRenderableWidget(titleField);

                this.addRenderableWidget(btn(midX - 180, 108, 115, 20, new TextComponent("▶ Play Now"), b -> {
                    String url = inputField.getValue().trim();
                    if (!url.isEmpty()) {
                        ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(
                                C2SJamActionPacket.Action.ADD_TRACK, titleField.getValue().trim(), url, true, 0));
                        switchView(View.PLAYER);
                    }
                }));

                this.addRenderableWidget(btn(midX - 60, 108, 115, 20, new TextComponent("➕ Queue"), b -> {
                    String url = inputField.getValue().trim();
                    if (!url.isEmpty()) {
                        ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(
                                C2SJamActionPacket.Action.ADD_TRACK, titleField.getValue().trim(), url, false, 0));
                        switchView(View.PLAYER);
                    }
                }));

                this.addRenderableWidget(btn(midX + 60, 108, 120, 20, new TextComponent("← Back"), b -> switchView(View.PLAYER)));
            }

            case SETTINGS -> {
                this.overlayToggleBtn = btn(midX - 180, 48, 175, 20, new TextComponent("HUD Overlay: " + (ClientJamConfig.isOverlayEnabled() ? "§aON" : "§cOFF")), b -> {
                    ClientJamConfig.toggleOverlay();
                    b.setMessage(new TextComponent("HUD Overlay: " + (ClientJamConfig.isOverlayEnabled() ? "§aON" : "§cOFF")));
                }, new TextComponent("Toggle whether the in-game song notification overlay appears."));
                this.addRenderableWidget(overlayToggleBtn);

                this.overlayCornerBtn = btn(midX + 5, 48, 175, 20, new TextComponent("HUD Position: §e" + ClientJamConfig.getOverlayCorner().getDisplayName()), b -> {
                    ClientJamConfig.cycleOverlayCorner();
                    b.setMessage(new TextComponent("HUD Position: §e" + ClientJamConfig.getOverlayCorner().getDisplayName()));
                }, new TextComponent("Cycle screen corner: Top-Right, Top-Left, Bottom-Right, Bottom-Left."));
                this.addRenderableWidget(overlayCornerBtn);

                if (ClientJamManager.canManage()) {
                    if (ClientJamManager.isOwner()) {
                        privacyButton = btn(midX - 180, 80, 175, 20, new TextComponent("Privacy: " + (ClientJamManager.isPrivate() ? "§cPrivate" : "§aPublic")), b ->
                                        ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.TOGGLE_PRIVACY)),
                                new TextComponent("Toggle whether this Jam appears in the Public directory.")
                        );
                        this.addRenderableWidget(privacyButton);
                    }

                    policyButton = btn(midX + (ClientJamManager.isOwner() ? 5 : -180), 80, ClientJamManager.isOwner() ? 175 : 360, 20,
                            new TextComponent("Policy: §e" + ClientJamManager.getEditPolicy().getDisplayName()), b ->
                                    ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.SET_POLICY)),
                            new TextComponent("Cycle who can edit: Owner & Admins, Collaborators, or Open to All.")
                    );
                    this.addRenderableWidget(policyButton);

                    this.addRenderableWidget(btn(midX - 180, 108, 175, 20, new TextComponent("👥 Manage Member Roles"), b -> switchView(View.ROLES)));

                    if (ClientJamManager.isOwner()) {
                        this.addRenderableWidget(btn(midX + 5, 108, 175, 20, new TextComponent("§cDelete Jam"), b -> {
                            ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(
                                    C2SJamActionPacket.Action.DELETE_JAM, ClientJamManager.getJamId()));
                            switchView(View.DIRECTORY_PUBLIC);
                        }));
                    }
                }

                this.addRenderableWidget(btn(midX - 180, 136, 360, 20, new TextComponent("← Back to Player"), b -> switchView(View.PLAYER)));
            }

            case ROLES -> {
                this.addRenderableWidget(btn(midX - 180, 32, 55, 18, new TextComponent("← Back"), b -> switchView(View.SETTINGS)));

                this.addMemberField = new EditBox(this.font, midX - 120, 32, 210, 18, new TextComponent("Username"));
                this.addMemberField.setMaxLength(32);
                this.addMemberField.setSuggestion("Add Member by Username...");
                this.addRenderableWidget(addMemberField);

                this.addRenderableWidget(btn(midX + 95, 32, 85, 18, new TextComponent("➕ Add"), b -> {
                    String name = addMemberField.getValue().trim();
                    if (!name.isEmpty()) {
                        ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(
                                C2SJamActionPacket.Action.ADD_MEMBER_BY_NAME, "", name));
                        addMemberField.setValue("");
                    }
                }));
            }

            case INVITE -> {
                this.addRenderableWidget(btn(midX - 180, 32, 120, 18, new TextComponent("← Back to Player"), b -> switchView(View.PLAYER)));
            }

            case DIRECTORY_PUBLIC, DIRECTORY_MY -> {
                if (currentView == View.DIRECTORY_MY) {
                    this.addRenderableWidget(btn(midX - 180, 32, 150, 18, new TextComponent("✨ Create New Jam"), b -> switchView(View.CREATE_JAM)));
                }
            }

            case CREATE_JAM -> {
                this.inputField = new EditBox(this.font, midX - 180, 55, 360, 20, new TextComponent("Jam Name"));
                this.inputField.setMaxLength(64);
                this.inputField.setSuggestion("Enter Jam Name...");
                this.addRenderableWidget(inputField);

                this.addRenderableWidget(btn(midX - 180, 85, 115, 20, new TextComponent(createAsPrivate ? "Type: §cPrivate" : "Type: §aPublic"), b -> {
                    createAsPrivate = !createAsPrivate;
                    b.setMessage(new TextComponent(createAsPrivate ? "Type: §cPrivate" : "Type: §aPublic"));
                }));

                this.addRenderableWidget(btn(midX - 60, 85, 120, 20, new TextComponent("Create & Join"), b -> {
                    String name = inputField.getValue().trim();
                    if (!name.isEmpty()) {
                        ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(
                                C2SJamActionPacket.Action.CREATE_JAM, "", name, createAsPrivate, 0));
                        switchView(View.PLAYER);
                    }
                }));

                this.addRenderableWidget(btn(midX + 65, 85, 115, 20, new TextComponent("Cancel"), b -> switchView(View.DIRECTORY_MY)));
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int midX = this.width / 2;

        if (currentView == View.PLAYER && ClientJamManager.isInJam()) {
            List<JamTrack> tracks = ClientJamManager.getTracks();
            int currentIdx = ClientJamManager.getCurrentTrackIndex();
            JamTrack currentTrack = (currentIdx >= 0 && currentIdx < tracks.size()) ? tracks.get(currentIdx) : null;

            if (currentTrack != null && currentTrack.getDurationMs() > 0 && ClientJamManager.canEdit()) {
                int barX = midX - 180;
                int barY = 78;
                int barW = 360;
                if (mouseX >= barX && mouseX <= barX + barW && mouseY >= barY - 4 && mouseY <= barY + 8) {
                    float pct = (float) (mouseX - barX) / (float) barW;
                    long targetMs = (long) (pct * currentTrack.getDurationMs());
                    ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(
                            C2SJamActionPacket.Action.SEEK, String.valueOf(targetMs)));
                    return true;
                }
            }

            int startY = 98;
            int offset = page * ITEMS_PER_PAGE;
            boolean canEdit = ClientJamManager.canEdit();

            for (int i = 0; i < ITEMS_PER_PAGE && (offset + i) < tracks.size(); i++) {
                int idx = offset + i;
                int itemY = startY + (i * 21);

                if (canEdit && mouseX >= midX - 180 && mouseX <= midX + 65 && mouseY >= itemY && mouseY <= itemY + 19) {
                    ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(
                            C2SJamActionPacket.Action.JUMP_TRACK, "", "", false, idx));
                    return true;
                }

                if (canEdit) {
                    if (idx > 0 && mouseX >= midX + 70 && mouseX <= midX + 95 && mouseY >= itemY && mouseY <= itemY + 19) {
                        ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(
                                C2SJamActionPacket.Action.MOVE_TRACK, "", "", false, idx, idx - 1));
                        return true;
                    }
                    if (idx < tracks.size() - 1 && mouseX >= midX + 100 && mouseX <= midX + 125 && mouseY >= itemY && mouseY <= itemY + 19) {
                        ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(
                                C2SJamActionPacket.Action.MOVE_TRACK, "", "", false, idx, idx + 1));
                        return true;
                    }
                    if (mouseX >= midX + 130 && mouseX <= midX + 155 && mouseY >= itemY && mouseY <= itemY + 19) {
                        ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(
                                C2SJamActionPacket.Action.REMOVE_TRACK, "", "", false, idx));
                        return true;
                    }
                }
            }

        } else if (currentView == View.ROLES) {
            List<SyncedJam.MemberInfo> list = ClientJamManager.getMembers();
            int startY = 66;
            int offset = page * ITEMS_PER_PAGE;

            for (int i = 0; i < ITEMS_PER_PAGE && (offset + i) < list.size(); i++) {
                SyncedJam.MemberInfo member = list.get(offset + i);
                int itemY = startY + (i * 21);

                if (ClientJamManager.canManage() && member.role() != SyncedJam.Role.OWNER &&
                        mouseX >= midX + 60 && mouseX <= midX + 150 && mouseY >= itemY && mouseY <= itemY + 19) {
                    ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(
                            C2SJamActionPacket.Action.CYCLE_MEMBER_ROLE, member.uuid().toString(), ""));
                    return true;
                }

                if (ClientJamManager.canManage() && member.role() != SyncedJam.Role.OWNER &&
                        mouseX >= midX + 155 && mouseX <= midX + 180 && mouseY >= itemY && mouseY <= itemY + 19) {
                    ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(
                            C2SJamActionPacket.Action.REMOVE_MEMBER, member.uuid().toString(), ""));
                    return true;
                }
            }

        } else if (currentView == View.INVITE) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getConnection() != null) {
                UUID myUuid = mc.player != null ? mc.player.getUUID() : null;
                List<PlayerInfo> list = mc.getConnection().getOnlinePlayers().stream()
                        .filter(info -> myUuid == null || !info.getProfile().getId().equals(myUuid))
                        .toList();

                int startY = 66;
                int offset = page * ITEMS_PER_PAGE;

                for (int i = 0; i < ITEMS_PER_PAGE && (offset + i) < list.size(); i++) {
                    PlayerInfo info = list.get(offset + i);
                    int itemY = startY + (i * 22);

                    if (mouseX >= midX + 100 && mouseX <= midX + 175 && mouseY >= itemY && mouseY <= itemY + 20) {
                        ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(
                                C2SJamActionPacket.Action.INVITE_PLAYER, "", info.getProfile().getName(), false, 0));
                        return true;
                    }
                }
            }

        } else if (currentView == View.DIRECTORY_PUBLIC || currentView == View.DIRECTORY_MY) {
            UUID myUuid = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : null;
            List<JamSummary> list = ClientJamManager.getDirectory().stream()
                    .filter(j -> currentView == View.DIRECTORY_PUBLIC ? !j.isPrivate() : (myUuid != null && j.ownerId().equals(myUuid)))
                    .toList();

            int startY = 56;
            int offset = page * ITEMS_PER_PAGE;

            for (int i = 0; i < ITEMS_PER_PAGE && (offset + i) < list.size(); i++) {
                JamSummary jam = list.get(offset + i);
                int itemY = startY + (i * 26);

                if (mouseX >= midX - 180 && mouseX <= midX + 180 && mouseY >= itemY && mouseY <= itemY + 24) {
                    ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(
                            C2SJamActionPacket.Action.JOIN_JAM, jam.id(), jam.name()));
                    switchView(View.PLAYER);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (currentView == View.ADD_TRACK && inputField != null && !inputField.getValue().trim().isEmpty()) {
                ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(
                        C2SJamActionPacket.Action.ADD_TRACK, titleField.getValue().trim(), inputField.getValue().trim(), true, 0));
                switchView(View.PLAYER);
                return true;
            } else if (currentView == View.ROLES && addMemberField != null && !addMemberField.getValue().trim().isEmpty()) {
                ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(
                        C2SJamActionPacket.Action.ADD_MEMBER_BY_NAME, "", addMemberField.getValue().trim()));
                addMemberField.setValue("");
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        int midX = this.width / 2;

        if (playPauseButton != null) playPauseButton.setMessage(new TextComponent(ClientJamManager.isPlaying() ? "⏸" : "▶"));
        if (repeatButton != null) {
            String rep = switch (ClientJamManager.getRepeatMode()) {
                case ALL -> "🔁";
                case ONE -> "🔂";
                default -> "➡";
            };
            repeatButton.setMessage(new TextComponent(rep));
        }
        if (shuffleButton != null) shuffleButton.setMessage(new TextComponent(ClientJamManager.isShuffle() ? "§a🔀" : "§7🔀"));
        if (policyButton != null) {
            String color = switch (ClientJamManager.getEditPolicy()) {
                case OWNER_ONLY -> "§c";
                case COLLABORATORS -> "§6";
                case OPEN -> "§a";
            };
            policyButton.setMessage(new TextComponent("Policy: " + color + ClientJamManager.getEditPolicy().getDisplayName()));
        }
        if (privacyButton != null) {
            privacyButton.setMessage(new TextComponent("Privacy: " + (ClientJamManager.isPrivate() ? "§cPrivate" : "§aPublic")));
        }

        int totalItems = 0;
        if (currentView == View.PLAYER) totalItems = ClientJamManager.getTracks().size();
        else if (currentView == View.DIRECTORY_PUBLIC || currentView == View.DIRECTORY_MY) {
            UUID myUuid = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : null;
            totalItems = (int) ClientJamManager.getDirectory().stream()
                    .filter(j -> currentView == View.DIRECTORY_PUBLIC ? !j.isPrivate() : (myUuid != null && j.ownerId().equals(myUuid)))
                    .count();
        } else if (currentView == View.INVITE) {
            UUID myUuid = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : null;
            totalItems = Minecraft.getInstance().getConnection() != null ? (int) Minecraft.getInstance().getConnection().getOnlinePlayers().stream().filter(i -> myUuid == null || !i.getProfile().getId().equals(myUuid)).count() : 0;
        } else if (currentView == View.ROLES) {
            totalItems = ClientJamManager.getMembers().size();
        }

        int maxPage = Math.max(0, (totalItems - 1) / ITEMS_PER_PAGE);
        if (page > maxPage) page = Math.max(0, maxPage);

        boolean hasMultiplePages = totalItems > ITEMS_PER_PAGE;
        if (prevPageBtn != null) {
            prevPageBtn.visible = hasMultiplePages;
            prevPageBtn.active = page > 0;
        }
        if (nextPageBtn != null) {
            nextPageBtn.visible = hasMultiplePages;
            nextPageBtn.active = page < maxPage;
        }

        if (hasMultiplePages) {
            int py = getPaginationY();
            drawCenteredString(poseStack, this.font, (page + 1) + " / " + (maxPage + 1), midX, py + 4, 0xAAAAAA);
        }

        int calculatedVol = ClientAudioEngine.getCurrentCalculatedVolume();
        drawString(poseStack, this.font, "Audio: §a" + calculatedVol + "%", midX - 30, this.height - 15, 0x888888);

        switch (currentView) {
            case PLAYER -> {
                if (!ClientJamManager.isInJam()) {
                    drawString(poseStack, this.font, "§cYou are not in any active Jam.", midX - 180, 80, 0xFF5555);
                    drawString(poseStack, this.font, "§7Click §ePublic Jams §7above to join one, or create one in §eMy Jams§7!", midX - 180, 95, 0xAAAAAA);
                    return;
                }

                String badge = ClientJamManager.isPrivate() ? "§c[Private] " : "§a[Public] ";
                drawString(poseStack, this.font, badge + "§6" + ClientJamManager.getJamName() + " §7(Host: §f" + ClientJamManager.getOwnerName() + "§7)", midX - 180, 52, 0xFFFFFF);

                String currentTitle = ClientJamManager.getCurrentTrackTitle().isEmpty() ? "None" : ClientJamManager.getCurrentTrackTitle();
                drawString(poseStack, this.font, "Now: §e" + currentTitle, midX - 180, 63, 0xFFFF55);

                int currentIdx = ClientJamManager.getCurrentTrackIndex();
                List<JamTrack> tracks = ClientJamManager.getTracks();
                JamTrack curTrack = (currentIdx >= 0 && currentIdx < tracks.size()) ? tracks.get(currentIdx) : null;

                if (curTrack != null && curTrack.getDurationMs() > 0) {
                    long elapsed = ClientJamManager.isPlaying()
                            ? Math.max(0, System.currentTimeMillis() - ClientJamManager.getStartTimestamp())
                            : ClientJamManager.getPausedElapsedMs();
                    elapsed = Math.min(elapsed, curTrack.getDurationMs());

                    float progress = Math.min(1.0f, (float) elapsed / curTrack.getDurationMs());
                    int barX = midX - 180;
                    int barY = 78;
                    int barW = 360;

                    fill(poseStack, barX, barY, barX + barW, barY + 4, 0x50FFFFFF);
                    fill(poseStack, barX, barY, barX + (int) (barW * progress), barY + 4, 0xFFFFAA00);

                    String timeStr = String.format("%02d:%02d / %02d:%02d",
                            (elapsed / 60000), (elapsed / 1000) % 60,
                            (curTrack.getDurationMs() / 60000), (curTrack.getDurationMs() / 1000) % 60);
                    drawString(poseStack, this.font, timeStr, midX + 85, 68, 0xAAAAAA);
                }

                drawString(poseStack, this.font, "§6Tracklist (" + tracks.size() + " songs):", midX - 180, 88, 0xFFAA00);

                int startY = 98;
                int offset = page * ITEMS_PER_PAGE;
                boolean canEdit = ClientJamManager.canEdit();

                for (int i = 0; i < ITEMS_PER_PAGE && (offset + i) < tracks.size(); i++) {
                    int idx = offset + i;
                    int itemY = startY + (i * 21);
                    boolean isCurrent = (idx == currentIdx);

                    JamTrack track = tracks.get(idx);
                    fill(poseStack, midX - 180, itemY, midX + 65, itemY + 19, isCurrent ? 0x80336699 : 0x40000000);

                    String label = (idx + 1) + ". " + (isCurrent ? "§a▶ " : "") + track.getTitle();
                    if (label.length() > 36) label = label.substring(0, 34) + "...";
                    drawString(poseStack, this.font, label, midX - 176, itemY + 3, isCurrent ? 0x55FF55 : 0xEEEEEE);

                    String subUrl = track.getUrl();
                    if (subUrl.length() > 38) subUrl = subUrl.substring(0, 36) + "...";
                    drawString(poseStack, this.font, "§8" + subUrl, midX - 176, itemY + 11, 0x666666);

                    if (canEdit) {
                        fill(poseStack, midX + 70, itemY, midX + 95, itemY + 19, 0x50000000);
                        drawCenteredString(poseStack, this.font, "▲", midX + 82, itemY + 5, idx > 0 ? 0xFFFF55 : 0x444444);

                        fill(poseStack, midX + 100, itemY, midX + 125, itemY + 19, 0x50000000);
                        drawCenteredString(poseStack, this.font, "▼", midX + 112, itemY + 5, idx < tracks.size() - 1 ? 0xFFFF55 : 0x444444);

                        fill(poseStack, midX + 130, itemY, midX + 155, itemY + 19, 0x40AA0000);
                        drawCenteredString(poseStack, this.font, "§c✕", midX + 142, itemY + 5, 0xFF5555);
                    }
                }
            }

            case SETTINGS -> {
                drawString(poseStack, this.font, "§6Settings & Preferences", midX - 180, 34, 0xFFFFFF);
                if (ClientJamManager.canManage()) {
                    drawString(poseStack, this.font, "§7Room Administration:", midX - 180, 68, 0xAAAAAA);
                }
            }

            case ROLES -> {
                drawString(poseStack, this.font, "§6Member Roles in §f" + ClientJamManager.getJamName(), midX - 180, 53, 0xFFFFFF);
                List<SyncedJam.MemberInfo> list = ClientJamManager.getMembers();
                int startY = 66;
                int offset = page * ITEMS_PER_PAGE;
                Minecraft mc = Minecraft.getInstance();

                if (list.isEmpty()) {
                    drawString(poseStack, this.font, "§7No members found.", midX - 180, startY + 10, 0x888888);
                } else {
                    for (int i = 0; i < ITEMS_PER_PAGE && (offset + i) < list.size(); i++) {
                        SyncedJam.MemberInfo member = list.get(offset + i);
                        int itemY = startY + (i * 21);

                        boolean isOnline = mc.getConnection() != null && mc.getConnection().getPlayerInfo(member.uuid()) != null;
                        String statusDot = isOnline ? "§a● " : "§7○ ";

                        fill(poseStack, midX - 180, itemY, midX + 55, itemY + 19, 0x40000000);
                        drawString(poseStack, this.font, statusDot + "§f" + member.name(), midX - 174, itemY + 5, 0xFFFFFF);

                        fill(poseStack, midX + 60, itemY, midX + 150, itemY + 19, 0x60006699);
                        String roleLabel = member.role().getColor() + member.role().getDisplayName() + (member.role() == SyncedJam.Role.OWNER ? "" : " ↻");
                        drawCenteredString(poseStack, this.font, roleLabel, midX + 105, itemY + 5, 0xFFFFFF);

                        if (member.role() != SyncedJam.Role.OWNER && ClientJamManager.canManage()) {
                            fill(poseStack, midX + 155, itemY, midX + 180, itemY + 19, 0x40AA0000);
                            drawCenteredString(poseStack, this.font, "§c✕", midX + 167, itemY + 5, 0xFF5555);
                        }
                    }
                }
            }

            case INVITE -> {
                drawString(poseStack, this.font, "§6Invite Online Players to §f" + ClientJamManager.getJamName(), midX - 180, 52, 0xFFFFFF);
                Minecraft mc = Minecraft.getInstance();
                if (mc.getConnection() != null) {
                    UUID myUuid = mc.player != null ? mc.player.getUUID() : null;
                    List<PlayerInfo> list = mc.getConnection().getOnlinePlayers().stream()
                            .filter(info -> myUuid == null || !info.getProfile().getId().equals(myUuid))
                            .toList();

                    int startY = 66;
                    int offset = page * ITEMS_PER_PAGE;

                    if (list.isEmpty()) {
                        drawString(poseStack, this.font, "§7No other players online to invite.", midX - 180, 80, 0x888888);
                    } else {
                        for (int i = 0; i < ITEMS_PER_PAGE && (offset + i) < list.size(); i++) {
                            PlayerInfo info = list.get(offset + i);
                            int itemY = startY + (i * 22);

                            fill(poseStack, midX - 180, itemY, midX + 90, itemY + 20, 0x40000000);
                            drawString(poseStack, this.font, info.getProfile().getName(), midX - 174, itemY + 6, 0xFFFFFF);

                            fill(poseStack, midX + 100, itemY, midX + 175, itemY + 20, 0x80008800);
                            drawCenteredString(poseStack, this.font, "§aInvite", midX + 137, itemY + 6, 0xFFFFFF);
                        }
                    }
                }
            }

            case DIRECTORY_PUBLIC, DIRECTORY_MY -> {
                UUID myUuid = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : null;
                List<JamSummary> list = ClientJamManager.getDirectory().stream()
                        .filter(j -> currentView == View.DIRECTORY_PUBLIC ? !j.isPrivate() : (myUuid != null && j.ownerId().equals(myUuid)))
                        .toList();

                int startY = 56;
                int offset = page * ITEMS_PER_PAGE;

                if (list.isEmpty()) {
                    drawString(poseStack, this.font, "§7No Jams found in this directory.", midX - 180, startY + 10, 0x888888);
                } else {
                    for (int i = 0; i < ITEMS_PER_PAGE && (offset + i) < list.size(); i++) {
                        JamSummary jam = list.get(offset + i);
                        int itemY = startY + (i * 26);

                        fill(poseStack, midX - 180, itemY, midX + 180, itemY + 24, 0x40000000);
                        drawString(poseStack, this.font, (jam.isPrivate() ? "§c[P] " : "§a[Pub] ") + "§f" + jam.name() + " §7(by " + jam.ownerName() + ")", midX - 176, itemY + 4, 0xFFFFFF);
                        drawString(poseStack, this.font, "§8" + jam.trackCount() + " tracks | 👥 " + jam.listeners() + " listening | Click to Join", midX - 176, itemY + 14, 0x888888);
                    }
                }
            }
            default -> {}
        }

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}