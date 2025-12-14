package net.eris.reverie.network.packet;

import net.eris.reverie.capability.MeditationProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundSyncMeditationPacket {
    private final int entityId; // <-- YENİ: Hangi oyuncu?
    private final boolean isMeditating;
    private final double originY;

    // Yapıcıya ID ekledik
    public ClientboundSyncMeditationPacket(int entityId, boolean isMeditating, double originY) {
        this.entityId = entityId;
        this.isMeditating = isMeditating;
        this.originY = originY;
    }

    // Decoder
    public ClientboundSyncMeditationPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt(); // ID'yi oku
        this.isMeditating = buf.readBoolean();
        this.originY = buf.readDouble();
    }

    // Encoder
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId); // ID'yi yaz
        buf.writeBoolean(isMeditating);
        buf.writeDouble(originY);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Sadece Client tarafında çalıştır
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level != null) {
                    // ID'den dünyadaki oyuncuyu bul
                    Entity entity = mc.level.getEntity(this.entityId);
                    if (entity instanceof Player player) {
                        player.getCapability(MeditationProvider.PLAYER_MEDITATION).ifPresent(cap -> {
                            cap.setMeditating(this.isMeditating);
                            cap.setOriginY(this.originY);
                        });
                    }
                }
            });
        });
        context.setPacketHandled(true);
        return true;
    }
}