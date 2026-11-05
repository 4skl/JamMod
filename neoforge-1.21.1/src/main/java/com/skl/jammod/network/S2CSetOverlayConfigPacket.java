package com.skl.jammod.network;

import com.skl.jammod.JamMod;
import com.skl.jammod.client.ClientJamConfig;
import com.skl.jammod.client.OverlayCorner;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CSetOverlayConfigPacket implements CustomPacketPayload {
    public static final Type<S2CSetOverlayConfigPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(JamMod.MODID, "set_overlay_config"));

    public static final StreamCodec<FriendlyByteBuf, S2CSetOverlayConfigPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> msg.encode(buf),
            S2CSetOverlayConfigPacket::decode
    );

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

    public static S2CSetOverlayConfigPacket toggle() {
        return new S2CSetOverlayConfigPacket(true, false, false, OverlayCorner.TOP_RIGHT);
    }

    public static S2CSetOverlayConfigPacket corner(OverlayCorner corner) {
        return new S2CSetOverlayConfigPacket(false, false, true, corner);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.toggle);
        buf.writeBoolean(this.enabled);
        buf.writeBoolean(this.setCorner);
        buf.writeEnum(this.corner);
    }

    public static S2CSetOverlayConfigPacket decode(FriendlyByteBuf buf) {
        return new S2CSetOverlayConfigPacket(
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readEnum(OverlayCorner.class)
        );
    }

    public static void handle(S2CSetOverlayConfigPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (msg.toggle) {
                ClientJamConfig.toggleOverlay();
            } else if (msg.setCorner) {
                ClientJamConfig.setOverlayCorner(msg.corner);
            }
        });
    }
}