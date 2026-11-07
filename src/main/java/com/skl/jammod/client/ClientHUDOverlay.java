package com.skl.jammod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.skl.jammod.JamMod;
import net.minecraft.client.Minecraft;

//? if >=1.20 {
import net.minecraft.client.gui.GuiGraphics;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
*///?}

//? if >1.20.1 {
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
*///?} else if >=1.19 {
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
//?} else {
/*import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
*///?}

//? if >=1.21 {
/*import net.neoforged.fml.common.EventBusSubscriber;
 *///?} else if >1.20.1 {
/*import net.neoforged.fml.common.Mod.EventBusSubscriber;
*///?} else {
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
 //?}

@EventBusSubscriber(modid = JamMod.MODID, value = Dist.CLIENT)
public class ClientHUDOverlay {
    private static String popupTitle = "";
    private static String popupUrl = "";
    private static long popupStartTime = 0;
    private static final long POPUP_DURATION = 6000;

    public static void triggerPopup(String title, String url) {
        if ((title == null || title.isBlank()) && (url == null || url.isBlank())) return;
        popupTitle = (title != null && !title.isBlank()) ? title : url;
        popupUrl = (url != null && !url.isBlank()) ? url : "";
        popupStartTime = System.currentTimeMillis();
    }

    //? if <=1.18.2 {
    /*@SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
    *///?} else {
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        //?}
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;
        if (!ClientJamConfig.isOverlayEnabled()) return;

        if (mc.screen != null) {
            popupStartTime = System.currentTimeMillis();
            return;
        }

        long elapsed = System.currentTimeMillis() - popupStartTime;
        if (elapsed > POPUP_DURATION || popupTitle.isEmpty()) return;

        //? if >=1.20 {
        GuiGraphics graphics = event.getGuiGraphics();
        //?} else if >=1.19 {
        /*PoseStack poseStack = event.getPoseStack();
         *///?} else {
        /*PoseStack poseStack = event.getMatrixStack();
         *///?}

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        float alpha = 1.0f;
        if (elapsed > POPUP_DURATION - 1000) {
            alpha = Math.max(0.0f, 1.0f - ((elapsed - (POPUP_DURATION - 1000)) / 1000.0f));
        }

        int width = 215;
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
        int barAlpha = ((int) (alpha * 0xFF) << 24) | 0xFFAA00;
        int textAlpha = (int) (alpha * 255) << 24;
        int linkAlpha = (int) (alpha * 180) << 24;

        String truncTitle = popupTitle.length() > 28 ? popupTitle.substring(0, 26) + "..." : popupTitle;
        String truncUrl = popupUrl.length() > 32 ? popupUrl.substring(0, 30) + "..." : popupUrl;

        //? if >=1.20 {
        graphics.fill(x, y, x + width, y + height, bgAlpha | 0x141414);
        graphics.fill(x, y, x + 3, y + height, barAlpha);
        graphics.drawString(mc.font, "§6🎵 Now Playing", x + 8, y + 4, textAlpha | 0xFFFFFF);
        graphics.drawString(mc.font, "§f" + truncTitle, x + 8, y + 15, textAlpha | 0xDDDDDD);
        //?} else {
        /*GuiComponent.fill(poseStack, x, y, x + width, y + height, bgAlpha | 0x141414);
        GuiComponent.fill(poseStack, x, y, x + 3, y + height, barAlpha);
        GuiComponent.drawString(poseStack, mc.font, "§6🎵 Now Playing", x + 8, y + 4, textAlpha | 0xFFFFFF);
        GuiComponent.drawString(poseStack, mc.font, "§f" + truncTitle, x + 8, y + 15, textAlpha | 0xDDDDDD);
        *///?}
        RenderSystem.disableBlend();
    }
}