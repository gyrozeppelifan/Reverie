package net.eris.reverie.client.network;

import net.eris.reverie.fx.client.ScreenShakeClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ScreenShakeS2CPacket {
    private final int durationTicks;
    private final float amplitudeDeg;

    public ScreenShakeS2CPacket(int durationTicks, float amplitudeDeg) {
        this.durationTicks = durationTicks;
        this.amplitudeDeg = amplitudeDeg;
    }

    public static void encode(ScreenShakeS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.durationTicks);
        buf.writeFloat(msg.amplitudeDeg);
    }

    public static ScreenShakeS2CPacket decode(FriendlyByteBuf buf) {
        int dur = buf.readVarInt();
        float amp = buf.readFloat();
        return new ScreenShakeS2CPacket(dur, amp);
    }

    public static void handle(ScreenShakeS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ScreenShakeClient.trigger(msg.durationTicks, msg.amplitudeDeg));
        ctx.get().setPacketHandled(true);
    }
}
