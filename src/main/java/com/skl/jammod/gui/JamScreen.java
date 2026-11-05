package com.skl.jammod.gui;

import com.skl.jammod.client.ClientAudioEngine;
import com.skl.jammod.client.ClientJamManager;
import com.skl.jammod.network.C2SJamActionPacket;
import com.skl.jammod.network.ModNetworking;
import com.skl.jammod.network.S2CSyncJamListPacket.JamSummary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.UUID;

public class JamScreen extends Screen {
    private enum Tab { ACTIVE_JAM, PUBLIC_JAMS, MY_JAMS }
    private Tab currentTab = Tab.ACTIVE_JAM;

    private EditBox inputField;
    private boolean createAsPrivate = false;
    private int page = 0;
    private static final int ITEMS_PER_PAGE = 5;

    public JamScreen() {
        super(Component.literal("Synced Jam Manager"));
    }

    @Override
    protected void init() {
        // Request fresh synchronization
        ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.REQUEST_SYNC, "", "", false, 0));

        int midX = this.width / 2;

        this.addRenderableWidget(Button.builder(Component.literal("Active Jam"), b -> switchTab(Tab.ACTIVE_JAM))
                .bounds(midX - 180, 10, 115, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Public Jams"), b -> switchTab(Tab.PUBLIC_JAMS))
                .bounds(midX - 60, 10, 120, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("My Jams"), b -> switchTab(Tab.MY_JAMS))
                .bounds(midX + 65, 10, 115, 20).build());

        this.inputField = new EditBox(this.font, midX - 180, 36, 360, 20, Component.literal("Input Field"));
        this.inputField.setMaxLength(1024);
        this.addRenderableWidget(inputField);

        initTabWidgets();
    }

    private void switchTab(Tab tab) {
        this.currentTab = tab;
        this.page = 0;
        this.clearWidgets();
        init();
    }

    private void initTabWidgets() {
        int midX = this.width / 2;

        if (currentTab == Tab.ACTIVE_JAM) {
            if (ClientJamManager.isInJam()) {
                this.inputField.setHint(Component.literal("Paste YouTube/media URL to add to this Jam..."));

                this.addRenderableWidget(Button.builder(Component.literal("+ Play Now"), b -> {
                    String url = inputField.getValue().trim();
                    if (!url.isEmpty()) {
                        ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.ADD_TRACK, "", url, true, 0));
                        inputField.setValue("");
                    }
                }).bounds(midX - 180, 60, 80, 20).build());

                this.addRenderableWidget(Button.builder(Component.literal("+ Queue"), b -> {
                    String url = inputField.getValue().trim();
                    if (!url.isEmpty()) {
                        ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.ADD_TRACK, "", url, false, 0));
                        inputField.setValue("");
                    }
                }).bounds(midX - 95, 60, 75, 20).build());

                this.addRenderableWidget(Button.builder(Component.literal("⏮"), b ->
                        ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.PREVIOUS, "", "", false, 0))
                ).bounds(midX - 15, 60, 28, 20).build());

                this.addRenderableWidget(Button.builder(Component.literal(ClientJamManager.isPlaying() ? "⏸" : "▶"), b ->
                        ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.TOGGLE_PLAY, "", "", false, 0))
                ).bounds(midX + 18, 60, 28, 20).build());

                this.addRenderableWidget(Button.builder(Component.literal("⏭"), b ->
                        ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.SKIP, "", "", false, 0))
                ).bounds(midX + 51, 60, 28, 20).build());

                this.addRenderableWidget(Button.builder(Component.literal("§cLeave"), b -> {
                    ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.LEAVE_JAM, "", "", false, 0));
                    clearWidgets();
                    init();
                }).bounds(midX + 85, 60, 95, 20).build());
            } else {
                this.inputField.setHint(Component.literal("You are not currently in a Jam. Browse or create one!"));
            }

        } else if (currentTab == Tab.PUBLIC_JAMS) {
            this.inputField.setHint(Component.literal("Search public jams..."));

        } else if (currentTab == Tab.MY_JAMS) {
            this.inputField.setHint(Component.literal("Name for your new Jam playlist..."));

            this.addRenderableWidget(Button.builder(Component.literal("Create Jam"), b -> {
                String name = inputField.getValue().trim();
                if (!name.isEmpty()) {
                    ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(
                            C2SJamActionPacket.Action.CREATE_JAM, "", name, createAsPrivate, 0));
                    inputField.setValue("");
                    switchTab(Tab.ACTIVE_JAM);
                }
            }).bounds(midX - 180, 60, 175, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal(createAsPrivate ? "Type: §cPrivate" : "Type: §aPublic"), b -> {
                createAsPrivate = !createAsPrivate;
                b.setMessage(Component.literal(createAsPrivate ? "Type: §cPrivate" : "Type: §aPublic"));
            }).bounds(midX + 5, 60, 175, 20).build());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int midX = this.width / 2;

        if (currentTab == Tab.ACTIVE_JAM && ClientJamManager.isInJam()) {
            List<String> tracks = ClientJamManager.getTracks();
            int startY = 125;
            int offset = page * ITEMS_PER_PAGE;

            // Track click to jump or delete
            for (int i = 0; i < ITEMS_PER_PAGE && (offset + i) < tracks.size(); i++) {
                int itemY = startY + (i * 20);
                // Jump to song
                if (mouseX >= midX - 180 && mouseX <= midX + 140 && mouseY >= itemY && mouseY <= itemY + 18) {
                    ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(
                            C2SJamActionPacket.Action.JUMP_TRACK, "", "", false, offset + i));
                    return true;
                }
                // Remove button [X]
                if (mouseX >= midX + 150 && mouseX <= midX + 180 && mouseY >= itemY && mouseY <= itemY + 18) {
                    ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(
                            C2SJamActionPacket.Action.REMOVE_TRACK, "", "", false, offset + i));
                    return true;
                }
            }

            // Pagination
            if (mouseX >= midX - 180 && mouseX <= midX - 140 && mouseY >= 230 && mouseY <= 245 && page > 0) {
                page--; return true;
            }
            if (mouseX >= midX + 140 && mouseX <= midX + 180 && mouseY >= 230 && mouseY <= 245 && ((page + 1) * ITEMS_PER_PAGE < tracks.size())) {
                page++; return true;
            }

        } else if (currentTab == Tab.PUBLIC_JAMS || currentTab == Tab.MY_JAMS) {
            UUID myUuid = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : null;
            List<JamSummary> list = ClientJamManager.getDirectory().stream()
                    .filter(j -> currentTab == Tab.PUBLIC_JAMS ? !j.isPrivate() : (myUuid != null && j.ownerId().equals(myUuid)))
                    .toList();

            int startY = (currentTab == Tab.MY_JAMS) ? 95 : 70;
            int offset = page * ITEMS_PER_PAGE;

            for (int i = 0; i < ITEMS_PER_PAGE && (offset + i) < list.size(); i++) {
                JamSummary jam = list.get(offset + i);
                int itemY = startY + (i * 24);

                // Join Button
                if (mouseX >= midX + 80 && mouseX <= midX + 130 && mouseY >= itemY && mouseY <= itemY + 18) {
                    ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(
                            C2SJamActionPacket.Action.JOIN_JAM, jam.id(), "", false, 0));
                    switchTab(Tab.ACTIVE_JAM);
                    return true;
                }

                // Delete Button (if owner in My Jams)
                if (currentTab == Tab.MY_JAMS && mouseX >= midX + 135 && mouseX <= midX + 180 && mouseY >= itemY && mouseY <= itemY + 18) {
                    ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(
                            C2SJamActionPacket.Action.DELETE_JAM, jam.id(), "", false, 0));
                    return true;
                }
            }

            // Pagination
            if (mouseX >= midX - 180 && mouseX <= midX - 140 && mouseY >= 230 && mouseY <= 245 && page > 0) {
                page--; return true;
            }
            if (mouseX >= midX + 140 && mouseX <= midX + 180 && mouseY >= 230 && mouseY <= 245 && ((page + 1) * ITEMS_PER_PAGE < list.size())) {
                page++; return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (currentTab == Tab.ACTIVE_JAM && ClientJamManager.isInJam() && !inputField.getValue().trim().isEmpty()) {
                ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(
                        C2SJamActionPacket.Action.ADD_TRACK, "", inputField.getValue().trim(), true, 0));
                inputField.setValue("");
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int midX = this.width / 2;

        int volume = ClientAudioEngine.getCurrentCalculatedVolume();
        graphics.drawString(this.font, "Volume: §a" + volume + "% §7(Minecraft Music Volume)", midX - 180, this.height - 18, 0xAAAAAA);

        if (currentTab == Tab.ACTIVE_JAM) {
            if (!ClientJamManager.isInJam()) {
                graphics.drawString(this.font, "§cYou are not in any active Jam.", midX - 180, 90, 0xFF5555);
                graphics.drawString(this.font, "§7Join an existing Jam from §ePublic Jams §7or create one in §eMy Jams§7!", midX - 180, 105, 0xAAAAAA);
            } else {
                String badge = ClientJamManager.isPrivate() ? "§c[Private] " : "§a[Public] ";
                graphics.drawString(this.font, badge + "§6" + ClientJamManager.getJamName() + " §7(Host: §f" + ClientJamManager.getOwnerName() + "§7)", midX - 180, 88, 0xFFFFFF);
                graphics.drawString(this.font, "Status: " + (ClientJamManager.isPlaying() ? "§aPlaying" : "§cPaused/Idle") +
                        " §7| 👥 §f" + ClientJamManager.getListenerCount() + " in sync", midX - 180, 100, 0xCCCCCC);

                List<String> tracks = ClientJamManager.getTracks();
                graphics.drawString(this.font, "§6Tracklist (" + tracks.size() + " songs):", midX - 180, 114, 0xFFAA00);

                int startY = 125;
                int offset = page * ITEMS_PER_PAGE;
                for (int i = 0; i < ITEMS_PER_PAGE && (offset + i) < tracks.size(); i++) {
                    int idx = offset + i;
                    int itemY = startY + (i * 20);
                    boolean isCurrent = (idx == ClientJamManager.getCurrentTrackIndex());

                    graphics.fill(midX - 180, itemY, midX + 145, itemY + 18, isCurrent ? 0x80336699 : 0x40000000);
                    String label = (idx + 1) + ". " + (isCurrent ? "§a▶ " : "") + tracks.get(idx);
                    if (label.length() > 45) label = label.substring(0, 43) + "...";
                    graphics.drawString(this.font, label, midX - 176, itemY + 5, isCurrent ? 0x55FF55 : 0xEEEEEE);

                    // Delete button
                    graphics.fill(midX + 150, itemY, midX + 180, itemY + 18, 0x40AA0000);
                    graphics.drawCenteredString(this.font, "§c✕", midX + 165, itemY + 5, 0xFF5555);
                }

                // Pagination
                int maxPage = Math.max(0, (tracks.size() - 1) / ITEMS_PER_PAGE);
                graphics.drawString(this.font, "§e< Prev", midX - 180, 232, page > 0 ? 0xFFFF55 : 0x555555);
                graphics.drawString(this.font, (page + 1) + "/" + (maxPage + 1), midX, 232, 0xAAAAAA);
                graphics.drawString(this.font, "Next >", midX + 145, 232, (page < maxPage) ? 0xFFFF55 : 0x555555);
            }

        } else {
            UUID myUuid = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : null;
            List<JamSummary> list = ClientJamManager.getDirectory().stream()
                    .filter(j -> currentTab == Tab.PUBLIC_JAMS ? !j.isPrivate() : (myUuid != null && j.ownerId().equals(myUuid)))
                    .toList();

            int startY = (currentTab == Tab.MY_JAMS) ? 95 : 70;
            int offset = page * ITEMS_PER_PAGE;

            if (list.isEmpty()) {
                graphics.drawString(this.font, "§7No Jams found.", midX - 180, startY, 0x888888);
            } else {
                for (int i = 0; i < ITEMS_PER_PAGE && (offset + i) < list.size(); i++) {
                    JamSummary jam = list.get(offset + i);
                    int itemY = startY + (i * 24);

                    graphics.fill(midX - 180, itemY, midX + 75, itemY + 22, 0x40000000);
                    graphics.drawString(this.font, (jam.isPrivate() ? "§c[P] " : "§a[Pub] ") + "§f" + jam.name(), midX - 176, itemY + 3, 0xFFFFFF);
                    graphics.drawString(this.font, "§7" + jam.trackCount() + " tracks | 👥 " + jam.listeners() + " listening", midX - 176, itemY + 12, 0x888888);

                    // Join Button
                    graphics.fill(midX + 80, itemY, midX + 130, itemY + 22, 0x80008800);
                    graphics.drawCenteredString(this.font, "§aJoin", midX + 105, itemY + 7, 0xFFFFFF);

                    // Delete Button in My Jams
                    if (currentTab == Tab.MY_JAMS) {
                        graphics.fill(midX + 135, itemY, midX + 180, itemY + 22, 0x80880000);
                        graphics.drawCenteredString(this.font, "§cDelete", midX + 157, itemY + 7, 0xFFFFFF);
                    }
                }

                int maxPage = Math.max(0, (list.size() - 1) / ITEMS_PER_PAGE);
                graphics.drawString(this.font, "§e< Prev", midX - 180, 232, page > 0 ? 0xFFFF55 : 0x555555);
                graphics.drawString(this.font, (page + 1) + "/" + (maxPage + 1), midX, 232, 0xAAAAAA);
                graphics.drawString(this.font, "Next >", midX + 145, 232, (page < maxPage) ? 0xFFFF55 : 0x555555);
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}