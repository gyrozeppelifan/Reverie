package net.eris.reverie.enchantment;

import net.eris.reverie.init.ReverieModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class RicochetEnchantment extends Enchantment {
    public RicochetEnchantment() {
        // Rarity: VERY_RARE (Çok Nadir - Kaos yaratır)
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 2; // Lvl 1: 2 kere seker, Lvl 2: 4 kere seker
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.is(ReverieModItems.SPIKED_LOG_ITEM.get());
    }
}