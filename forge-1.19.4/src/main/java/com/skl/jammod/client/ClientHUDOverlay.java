package com.skl.jammod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.skl.jammod.JamMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = JamMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientHUDOverlay {
    private static String popupTitle = "";
    private static long popupStartTime = 0;
    private static final long POPUP_DURATION = 6000;

    public static void triggerPopup(String title, String url) {
        String display = (title != null && !title.isBlank()) ? title : (url != null ? url : "");
        if (display.isBlank()) return;
        popupTitle = display;
        popupStartTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;
        if (!ClientJamConfig.isOverlayEnabled()) return;

        if (mc.screen != null) {
            popupStartTime = System.currentTimeMillis();
            return;
        }

        long elapsed = System.currentTimeMillis() - popupStartTime;
        if (elapsed > POPUP_DURATION || popupTitle.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        float alpha = 1.0f;
        if (elapsed > POPUP_DURATION - 1000) {
            alpha = Math.max(0.0f, 1.0f - ((elapsed - (POPUP_DURATION - 1000)) / 1000.0f));
        }

        int width = 195;
        int height = 28;

        OverlayCorner corner = ClientJamConfig.getOverlayCorner();
        int x, y;
        switch (corner) {
            case TOP_LEFT -> {
                x = 10;
                y = 10;
            }
            case BOTTOM_LEFT -> {
                x = 10;
                y = screenHeight - height - 12;
            }
            case BOTTOM_RIGHT -> {
                x = screenWidth - width - 10;
                y = screenHeight - height - 12;
            }
            case TOP_RIGHT -> {
                x = screenWidth - width - 10;
                y = 10;
            }
            default -> {
                x = screenWidth - width - 10;
                y = 10;
            }
        }

        RenderSystem.enableBlend();
        int bgAlpha = (int) (alpha * 0xDD) << 24;
        GuiComponent.fill(poseStack, x, y, x + width, y + height, bgAlpha | 0x141414);
        GuiComponent.fill(poseStack, x, y, x + 3, y + height, ((int) (alpha * 0xFF) << 24) | 0xFFAA00);

        int textAlpha = (int) (alpha * 255) << 24;
        GuiComponent.drawString(poseStack, mc.font, "§6🎵 Now Playing", x + 8, y + 4, textAlpha | 0xFFFFFF);

        String truncTitle = popupTitle.length() > 26 ? popupTitle.substring(0, 24) + "..." : popupTitle;
        GuiComponent.drawString(poseStack, mc.font, "§f" + truncTitle, x + 8, y + 15, textAlpha | 0xDDDDDD);
        RenderSystem.disableBlend();
    }
}