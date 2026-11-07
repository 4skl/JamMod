package com.skl.jammod.network;

import com.skl.jammod.JamMod;
import com.skl.jammod.client.ClientJamConfig;
import com.skl.jammod.client.OverlayCorner;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

//? if >=1.21 {
/*import net.neoforged.neoforge.network.handling.IPayloadContext;
        *///?} else if >1.20.1 {
/*import net.neoforged.neoforge.network.handling.PlayPayloadContext;
 *///?} else {
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
//?}

public class S2CSetOverlayConfigPacket
//? if >1.20.1 {
        /*implements net.minecraft.network.protocol.common.custom.CustomPacketPayload
*///?}
{
    //? if >=1.21 {
    /*public static final net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<S2CSetOverlayConfigPacket> TYPE =
            new net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JamMod.MODID, "set_overlay_config"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, S2CSetOverlayConfigPacket> STREAM_CODEC =
            net.minecraft.network.codec.StreamCodec.ofMember(S2CSetOverlayConfigPacket::write, S2CSetOverlayConfigPacket::decode);

    @Override
    public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
        return TYPE;
    }
    *///?} else if >1.20.1 {
    /*public static final ResourceLocation ID = new ResourceLocation(JamMod.MODID, "set_overlay_config");

    @Override
    public ResourceLocation id() {
        return ID;
    }
    *///?}

    //? if >1.20.1 {
    /*public void write(FriendlyByteBuf buf) {
        encode(this, buf);
    }
    *///?}

    public enum Type { TOGGLE, CORNER }
    public final Type type;
    public final OverlayCorner corner;

    public S2CSetOverlayConfigPacket(Type type, OverlayCorner corner) {
        this.type = type;
        this.corner = corner;
    }

    public static S2CSetOverlayConfigPacket toggle() {
        return new S2CSetOverlayConfigPacket(Type.TOGGLE, null);
    }

    public static S2CSetOverlayConfigPacket corner(OverlayCorner corner) {
        return new S2CSetOverlayConfigPacket(Type.CORNER, corner);
    }

    public static void encode(S2CSetOverlayConfigPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.type);
        if (msg.type == Type.CORNER) {
            buf.writeEnum(msg.corner != null ? msg.corner : OverlayCorner.TOP_RIGHT);
        }
    }

    public static S2CSetOverlayConfigPacket decode(FriendlyByteBuf buf) {
        Type t = buf.readEnum(Type.class);
        OverlayCorner c = (t == Type.CORNER) ? buf.readEnum(OverlayCorner.class) : null;
        return new S2CSetOverlayConfigPacket(t, c);
    }

    private static void applyConfig(S2CSetOverlayConfigPacket msg) {
        if (msg.type == Type.TOGGLE) {
            ClientJamConfig.toggleOverlay();
        } else if (msg.type == Type.CORNER && msg.corner != null) {
            ClientJamConfig.setOverlayCorner(msg.corner);
        }
    }

    //? if >=1.21 {
    /*public static void handle(S2CSetOverlayConfigPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> applyConfig(msg));
    }
    *///?} else if >1.20.1 {
    /*public static void handle(S2CSetOverlayConfigPacket msg, PlayPayloadContext ctx) {
        ctx.workHandler().execute(() -> applyConfig(msg));
    }
    *///?} else {
    public static void handle(S2CSetOverlayConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> applyConfig(msg)));
        ctx.get().setPacketHandled(true);
    }
    //?}
}