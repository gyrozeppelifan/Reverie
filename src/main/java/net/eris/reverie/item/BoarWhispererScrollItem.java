package net.eris.reverie.item;

import net.eris.reverie.init.ReverieModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BoarWhispererScrollItem extends Item {
    public BoarWhispererScrollItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        // Sadece oyuncu elinde tutuyorsa çalışsın
        if (!pIsSelected || !(pEntity instanceof Player player) || pLevel.isClientSide) {
            return;
        }

        // Oyuncunun etrafında kamp ateşi var mı diye bakıyoruz
        boolean nearCampfire = false;
        BlockPos pos = player.blockPosition();

        // Oyuncunun olduğu blok ve çevresindeki 1 blokluk alanı tara
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockState state = pLevel.getBlockState(pos.offset(x, y, z));
                    if (state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)) {
                        nearCampfire = true;
                        break;
                    }
                }
            }
        }

        if (nearCampfire) {
            CompoundTag tag = pStack.getOrCreateTag();
            int purifyProgress = tag.getInt("PurifyProgress");

            // İlerleme ekle
            purifyProgress++;
            tag.putInt("PurifyProgress", purifyProgress);

            // Görsel ve ses efektleri (her 20 tickte bir, yani saniyede 1)
            if (purifyProgress % 20 == 0) {
                pLevel.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.5f, 2.0f);
            }

            // 3 Saniye (60 tick) doldu mu?
            if (purifyProgress >= 60) {
                // Eşyayı dönüştür! (Burada senin PURIFIED_SCROLL itemını vereceğiz)
                ItemStack purified = new ItemStack(ReverieModItems.PURIFIED_SCROLL.get());
                player.getInventory().setItem(pSlotId, purified);

                // Efekt patlat
                pLevel.playSound(null, pos, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.5f, 1.5f);
                player.displayClientMessage(Component.literal("§6Parşömen arındı ve kutsandı..."), true);
            }
        } else {
            // Ateşten uzaklaşırsa ilerlemeyi sıfırla
            if (pStack.hasTag() && pStack.getTag().contains("PurifyProgress")) {
                pStack.getTag().remove("PurifyProgress");
            }
        }
    }
}