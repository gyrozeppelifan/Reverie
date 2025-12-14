package net.eris.reverie.network.packet;

import net.eris.reverie.capability.MeditationProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundSyncMeditationPacket {
    private final boolean isMeditating;
    private final double originY;

    // Veriyi paketle
    public ClientboundSyncMeditationPacket(boolean isMeditating, double originY) {
        this.isMeditating = isMeditating;
        this.originY = originY;
    }

    // Decoder
    public ClientboundSyncMeditationPacket(FriendlyByteBuf buf) {
        this.isMeditating = buf.readBoolean();
        this.originY = buf.readDouble();
    }

    // Encoder
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(isMeditating);
        buf.writeDouble(originY);
    }

    // Client tarafında paketi aç
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // BURASI CLIENT TARAFI
            // Minecraft.getInstance() sadece Client'ta çalışır, güvenlidir.
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                player.getCapability(MeditationProvider.PLAYER_MEDITATION).ifPresent(cap -> {
                    cap.setMeditating(this.isMeditating);
                    cap.setOriginY(this.originY);
                });
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}