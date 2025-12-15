package net.eris.reverie.events;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.capability.MeditationProvider;
import net.eris.reverie.network.packet.ClientboundSyncMeditationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID)
public class ReverieCapabilityHandler {

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(MeditationProvider.PLAYER_MEDITATION).isPresent()) {
                event.addCapability(new ResourceLocation(ReverieMod.MODID, "meditation"), new MeditationProvider());
            }
        }
    }

    // 1. Oyuna Giriş (Sync)
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        syncMeditation((ServerPlayer) event.getEntity());
    }

    // 2. Boyut Değiştirme (Sync)
    @SubscribeEvent
    public static void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncMeditation((ServerPlayer) event.getEntity());
    }

    // --- DÜZELTİLEN KISIM BURASI (Clone Event) ---
    // Oyuncu öldüğünde veya End'den döndüğünde bu çalışır.
    // Verileri "Eski Beden"den "Yeni Beden"e kopyalar.
    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        // Eski oyuncudan veriyi al
        event.getOriginal().getCapability(MeditationProvider.PLAYER_MEDITATION).ifPresent(oldCap -> {
            // Yeni oyuncuya aktar
            event.getEntity().getCapability(MeditationProvider.PLAYER_MEDITATION).ifPresent(newCap -> {
                // Eğer ölümse (isWasDeath true) veya End dönüşü ise verileri kopyala
                newCap.setMeditating(oldCap.isMeditating());
                newCap.setOriginY(oldCap.getOriginY());
                // Timer'ı kopyalamıyoruz, yeni hayatta sayaç sıfırdan başlasın (isteğe bağlı)
            });
        });

        // Eşitleme (Server -> Client)
        // Clone olayı sunucuda gerçekleşir, Client'ın haberi olması için paket yolluyoruz.
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncMeditation(serverPlayer);
        }
    }
    // ---------------------------------------------

    // Eşitleme Yardımcı Metodu
    private static void syncMeditation(ServerPlayer player) {
        player.getCapability(MeditationProvider.PLAYER_MEDITATION).ifPresent(cap -> {
            ReverieMod.PACKET_HANDLER.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    // Başa player.getId() ekledik
                    new ClientboundSyncMeditationPacket(player.getId(), cap.isMeditating(), cap.getOriginY())
            );
        });
    }
}