package net.eris.reverie.network.packet;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.capability.MeditationProvider;
import net.eris.reverie.init.ReverieModItems;
import net.eris.reverie.item.BoarMonkSealItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

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
                    stopMeditation(player, cap);
                } else {
                    // --- BAŞLATMA ---
                    ItemStack mainHand = player.getMainHandItem();
                    ItemStack offHand = player.getOffhandItem();
                    boolean hasSealMain = mainHand.getItem() instanceof BoarMonkSealItem || mainHand.is(ReverieModItems.BOAR_MONK_SEAL.get());
                    boolean hasSealOff = offHand.getItem() instanceof BoarMonkSealItem || offHand.is(ReverieModItems.BOAR_MONK_SEAL.get());

                    if (hasSealMain || hasSealOff) {
                        if (player.getCooldowns().isOnCooldown(ReverieModItems.BOAR_MONK_SEAL.get())) {
                            player.displayClientMessage(Component.translatable("reverie.message.meditation_cooldown"), true);
                            syncToClient(player, false, 0);
                            return;
                        }

                        cap.setMeditating(true);
                        cap.setOriginY(player.getY());

                        player.getCooldowns().addCooldown(ReverieModItems.BOAR_MONK_SEAL.get(), 400);

                        ItemStack stackToDamage = hasSealMain ? mainHand : offHand;
                        InteractionHand handToBreak = hasSealMain ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

                        stackToDamage.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(handToBreak));

                        player.displayClientMessage(Component.translatable("reverie.message.meditation_start"), true);

                        syncToClient(player, true, player.getY());

                    } else {
                        player.displayClientMessage(Component.translatable("reverie.message.meditation_need_seal"), true);
                        syncToClient(player, false, 0);
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

        syncToClient(player, false, 0);
    }

    // --- GÜNCELLENEN KISIM: HERKESE YOLLA ---
    private void syncToClient(ServerPlayer player, boolean isMeditating, double originY) {
        ReverieMod.PACKET_HANDLER.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), // Kendisine VE etrafındakilere yolla
                new ClientboundSyncMeditationPacket(player.getId(), isMeditating, originY) // ID'yi de ekle
        );
    }
}