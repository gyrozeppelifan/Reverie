package net.eris.reverie.network.packet;

import net.eris.reverie.entity.StitchedEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundNameStitchedPacket {
    private final int entityId;
    private final String name;

    public ServerboundNameStitchedPacket(int entityId, String name) {
        this.entityId = entityId;
        this.name = name;
    }

    // Decoder (Okuyucu)
    public ServerboundNameStitchedPacket(FriendlyByteBuf buffer) {
        this.entityId = buffer.readInt();
        this.name = buffer.readUtf();
    }

    // Encoder (Yazıcı)
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeUtf(this.name);
    }

    // Handler (İşleyici) - STATİK ve VOID yapıldı (En sağlam yöntem)
    public static void handle(ServerboundNameStitchedPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                // Server tarafında entity'yi bul
                var entity = player.level().getEntity(msg.entityId);

                if (entity instanceof StitchedEntity stitched) {
                    // Konsola bilgi bas (Hata ayıklamak için)
                    // System.out.println("Stitched ismi değişiyor: " + msg.name);

                    stitched.setCustomName(Component.literal(msg.name));
                    stitched.setPersistenceRequired(); // Despawn olmasın
                }
            }
        });

        // PAKETİN İŞLENDİĞİNİ ONAYLA (Çok önemli!)
        ctx.get().setPacketHandled(true);
    }
}