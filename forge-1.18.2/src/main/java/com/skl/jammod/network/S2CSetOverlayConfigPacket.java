package com.skl.jammod.network;

import com.skl.jammod.client.ClientJamConfig;
import com.skl.jammod.client.OverlayCorner;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSetOverlayConfigPacket {
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

    public static void encode(S2CSetOverlayConfigPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.toggle);
        buf.writeBoolean(msg.enabled);
        buf.writeBoolean(msg.setCorner);
        buf.writeEnum(msg.corner);
    }

    public static S2CSetOverlayConfigPacket decode(FriendlyByteBuf buf) {
        return new S2CSetOverlayConfigPacket(
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readEnum(OverlayCorner.class)
        );
    }

    public static void handle(S2CSetOverlayConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    if (msg.toggle) {
                        ClientJamConfig.toggleOverlay();
                    } else if (msg.setCorner) {
                        ClientJamConfig.setOverlayCorner(msg.corner);
                    }
                })
        );
        ctx.get().setPacketHandled(true);
    }
}