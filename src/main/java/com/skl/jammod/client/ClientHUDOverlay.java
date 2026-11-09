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
        popupTitle = (title != null && !title.isBlank()) ? title.trim() : "";
        popupUrl = (url != null && !url.isBlank()) ? url.trim() : "";

        if (popupTitle.isEmpty() && !popupUrl.isEmpty()) {
            popupTitle = popupUrl;
            popupUrl = "";
        } else if (popupTitle.equalsIgnoreCase(popupUrl)) {
            popupUrl = "";
        }

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

        // Smooth Fade Out & Slide-in Animation
        float alpha = 1.0f;
        if (elapsed > POPUP_DURATION - 800) {
            alpha = Math.max(0.0f, (POPUP_DURATION - elapsed) / 800.0f);
        }

        float slide = 1.0f;
        if (elapsed < 300) {
            slide = elapsed / 300.0f;
        }

        boolean hasSubUrl = !popupUrl.isEmpty();
        int width = 225;
        int height = hasSubUrl ? 38 : 26;

        OverlayCorner corner = ClientJamConfig.getOverlayCorner();
        int baseX, y;

        switch (corner) {
            case TOP_LEFT -> {
                baseX = (int) (10 * slide);
                y = 10;
            }
            case BOTTOM_LEFT -> {
                baseX = (int) (10 * slide);
                y = screenHeight - height - 12;
            }
            case BOTTOM_RIGHT -> {
                baseX = screenWidth - (int) ((width + 10) * slide);
                y = screenHeight - height - 12;
            }
            case TOP_RIGHT -> {
                baseX = screenWidth - (int) ((width + 10) * slide);
                y = 10;
            }
            default -> {
                baseX = screenWidth - (int) ((width + 10) * slide);
                y = 10;
            }
        }

        int x = baseX;

        RenderSystem.enableBlend();
        int bgCol = ((int) (alpha * 0xEA) << 24) | 0x111116;
        int borderCol = ((int) (alpha * 0x55) << 24) | 0xFFFFAA00;
        int barCol = ((int) (alpha * 0xFF) << 24) | 0xFFAA00;
        int textCol = ((int) (alpha * 255) << 24) | 0xFFFFFF;
        int subCol = ((int) (alpha * 180) << 24) | 0xAAAAAA;

        String truncTitle = popupTitle.length() > 30 ? popupTitle.substring(0, 28) + "..." : popupTitle;
        String truncUrl = popupUrl.length() > 34 ? popupUrl.substring(0, 31) + "..." : popupUrl;

        //? if >=1.20 {
        // Outer glow & card background
        graphics.fill(x, y, x + width, y + height, bgCol);
        graphics.fill(x, y, x + width, y + 1, borderCol);
        graphics.fill(x, y + height - 1, x + width, y + height, borderCol);
        graphics.fill(x + width - 1, y, x + width, y + height, borderCol);

        // Accent indicator
        graphics.fill(x, y, x + 3, y + height, barCol);

        // Content
        graphics.drawString(mc.font, "§6🎵 Now Playing", x + 8, y + 4, textCol);
        graphics.drawString(mc.font, "§f" + truncTitle, x + 8, y + 14, textCol);
        if (hasSubUrl) {
            graphics.drawString(mc.font, "§7🔗 " + truncUrl, x + 8, y + 25, subCol);
        }
        //?} else {
        /*GuiComponent.fill(poseStack, x, y, x + width, y + height, bgCol);
        GuiComponent.fill(poseStack, x, y, x + width, y + 1, borderCol);
        GuiComponent.fill(poseStack, x, y + height - 1, x + width, y + height, borderCol);
        GuiComponent.fill(poseStack, x + width - 1, y, x + width, y + height, borderCol);
        GuiComponent.fill(poseStack, x, y, x + 3, y + height, barCol);

        mc.font.draw(poseStack, "§6🎵 Now Playing", x + 8, y + 4, textCol);
        mc.font.draw(poseStack, "§f" + truncTitle, x + 8, y + 14, textCol);
        if (hasSubUrl) {
            mc.font.draw(poseStack, "§7🔗 " + truncUrl, x + 8, y + 25, subCol);
        }
        *///?}

        RenderSystem.disableBlend();
    }
}