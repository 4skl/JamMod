package com.skl.jammod.network;

import com.skl.jammod.JamMod;
import com.skl.jammod.client.ClientJamConfig;
import com.skl.jammod.client.OverlayCorner;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CSetOverlayConfigPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(JamMod.MODID + ":set_overlay_config");

    private final boolean toggle;
    private final boolean enabled;
    private final boolean setCorner;
    private final OverlayCorner corner;

    public S2CSetOverlayConfigPacket(boolean toggle, boolean enabled, boolean setCorner, OverlayCorner corner) {
        this.toggle = toggle;
        this.enabled = enabled;
        this.setCorner = setCorner;
        this.corner = corner != null ? corner : OverlayCorner.TOP_RIGHT;
    }

    public S2CSetOverlayConfigPacket(FriendlyByteBuf buf) {
        this.toggle = buf.readBoolean();
        this.enabled = buf.readBoolean();
        this.setCorner = buf.readBoolean();
        this.corner = buf.readEnum(OverlayCorner.class);
    }

    public static S2CSetOverlayConfigPacket toggle() {
        return new S2CSetOverlayConfigPacket(true, false, false, OverlayCorner.TOP_RIGHT);
    }

    public static S2CSetOverlayConfigPacket corner(OverlayCorner corner) {
        return new S2CSetOverlayConfigPacket(false, false, true, corner);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(this.toggle);
        buf.writeBoolean(this.enabled);
        buf.writeBoolean(this.setCorner);
        buf.writeEnum(this.corner);
    }

    public static void handle(S2CSetOverlayConfigPacket msg, IPayloadContext ctx) {
        ctx.workHandler().execute(() -> {
            if (msg.toggle) {
                ClientJamConfig.toggleOverlay();
            } else if (msg.setCorner) {
                ClientJamConfig.setOverlayCorner(msg.corner);
            }
        });
    }
}