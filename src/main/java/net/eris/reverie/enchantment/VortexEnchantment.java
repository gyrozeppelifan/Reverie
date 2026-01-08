package net.eris.reverie.enchantment;

import net.eris.reverie.init.ReverieModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class VortexEnchantment extends Enchantment {
    public VortexEnchantment() {
        // Rarity: UNCOMMON (Sıra Dışı)
        super(Rarity.UNCOMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 2; // Seviye arttıkça çekim alanı genişler
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.is(ReverieModItems.SPIKED_LOG_ITEM.get());
    }
}