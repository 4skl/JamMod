package com.skl.jammod.gui;

import com.skl.jammod.client.ClientJamManager;
import com.skl.jammod.network.C2SJamActionPacket;
import com.skl.jammod.network.ModNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class JamScreen extends Screen {
    private EditBox inputField;

    public JamScreen() {
        super(Component.literal("Jam Session Manager"));
    }

    @Override
    protected void init() {
        int midX = this.width / 2;
        int startY = 40;

        this.inputField = new EditBox(this.font, midX - 140, startY, 280, 20, Component.literal("Track URL"));
        this.inputField.setMaxLength(1024);
        this.inputField.setHint(Component.literal("Paste YouTube / Media URL & Press Enter..."));
        this.addRenderableWidget(inputField);

        // Play Button
        this.addRenderableWidget(Button.builder(Component.literal("Play URL"), b -> submitUrl())
                .bounds(midX - 140, startY + 26, 90, 20).build());

        // Pause / Resume Button
        this.addRenderableWidget(Button.builder(Component.literal("Pause/Resume"), b ->
                ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.TOGGLE_PLAY, ""))
        ).bounds(midX - 45, startY + 26, 90, 20).build());

        // Skip Button
        this.addRenderableWidget(Button.builder(Component.literal("Skip"), b ->
                ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.SKIP, ""))
        ).bounds(midX + 50, startY + 26, 90, 20).build());

        // Room Controls
        this.addRenderableWidget(Button.builder(Component.literal("Create Room"), b -> {
            String text = inputField.getValue().trim();
            if (!text.isEmpty()) {
                ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.CREATE, text));
                inputField.setValue("");
            }
        }).bounds(midX - 140, startY + 54, 90, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Join Room"), b -> {
            String text = inputField.getValue().trim();
            if (!text.isEmpty()) {
                ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.JOIN, text));
                inputField.setValue("");
            }
        }).bounds(midX - 45, startY + 54, 90, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Leave Room"), b ->
                ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.LEAVE, ""))
        ).bounds(midX + 50, startY + 54, 90, 20).build());
    }

    private void submitUrl() {
        String text = inputField.getValue().trim();
        if (!text.isEmpty()) {
            ModNetworking.CHANNEL.sendToServer(new C2SJamActionPacket(C2SJamActionPacket.Action.PLAY_URL, text));
            inputField.setValue("");
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            submitUrl();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);

        int midX = this.width / 2;
        String sessionName = ClientJamManager.getCurrentSession().isEmpty() ? "None" : ClientJamManager.getCurrentSession();
        String currentTrack = ClientJamManager.getCurrentTrack().isEmpty() ? "None" : ClientJamManager.getCurrentTrack();
        String status = ClientJamManager.isPlaying() ? "§aPlaying" : "§cPaused / Idle";

        int infoY = 130;
        graphics.drawString(this.font, "Session: §f" + sessionName, midX - 140, infoY, 0xAAAAAA);
        graphics.drawString(this.font, "Status: " + status, midX - 140, infoY + 14, 0xFFFFFF);
        graphics.drawString(this.font, "Track: §e" + currentTrack, midX - 140, infoY + 28, 0xFFFFFF);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}