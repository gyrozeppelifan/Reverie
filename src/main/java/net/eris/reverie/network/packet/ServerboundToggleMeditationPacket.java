package net.eris.reverie.network.packet;

import net.eris.reverie.capability.MeditationProvider;
import net.eris.reverie.init.ReverieModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundToggleMeditationPacket {

    public ServerboundToggleMeditationPacket() { }
    public ServerboundToggleMeditationPacket(FriendlyByteBuf buf) { }
    public void toBytes(FriendlyByteBuf buf) { }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            player.getCapability(MeditationProvider.PLAYER_MEDITATION).ifPresent(cap -> {
                if (cap.isMeditating()) {
                    // --- DURDURMA ---
                    // Durdururken cooldown kontrolüne gerek yok, istediği zaman inebilmeli.
                    stopMeditation(player, cap);
                } else {
                    // --- BAŞLATMA ---
                    ItemStack mainHand = player.getMainHandItem();
                    ItemStack offHand = player.getOffhandItem();
                    boolean hasSealMain = mainHand.is(ReverieModItems.BOAR_MONK_SEAL.get());
                    boolean hasSealOff = offHand.is(ReverieModItems.BOAR_MONK_SEAL.get());

                    if (hasSealMain || hasSealOff) {
                        // 1. COOLDOWN KONTROLÜ (YENİ)
                        // Eğer mühür cooldown'da ise başlatma!
                        if (player.getCooldowns().isOnCooldown(ReverieModItems.BOAR_MONK_SEAL.get())) {
                            player.displayClientMessage(Component.translatable("reverie.message.meditation_cooldown"), true); // Dil dosyasına ekle
                            return;
                        }

                        // 2. Başlat
                        cap.setMeditating(true);
                        cap.setOriginY(player.getY());

                        // 3. Cooldown Ekle (20 sn = 400 tick)
                        player.getCooldowns().addCooldown(ReverieModItems.BOAR_MONK_SEAL.get(), 400);

                        player.displayClientMessage(Component.translatable("reverie.message.meditation_start"), true);
                    } else {
                        player.displayClientMessage(Component.translatable("reverie.message.meditation_need_seal"), true);
                    }
                }
            });
        });
        context.setPacketHandled(true);
        return true;
    }

    private void stopMeditation(ServerPlayer player, MeditationProvider.PlayerMeditation cap) {
        cap.setMeditating(false);
        player.setNoGravity(false);
        player.displayClientMessage(Component.translatable("reverie.message.meditation_end"), true);
    }
}