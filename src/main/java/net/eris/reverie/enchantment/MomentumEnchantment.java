package net.eris.reverie.enchantment;

import net.eris.reverie.init.ReverieModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class MomentumEnchantment extends Enchantment {
    public MomentumEnchantment() {
        // Rarity: COMMON (Yaygın)
        super(Rarity.COMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3; // 3 Seviye: Hızlandıkça daha sert vurur
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.is(ReverieModItems.SPIKED_LOG_ITEM.get());
    }
}