package net.eris.reverie.enchantment;

import net.eris.reverie.init.ReverieModEnchantments; // Bunu import etmeyi unutma!
import net.eris.reverie.init.ReverieModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class SpiritDeflectionEnchantment extends Enchantment {
    public SpiritDeflectionEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.BREAKABLE, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 30;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.is(ReverieModItems.BOAR_MONK_SEAL.get()) || super.canEnchant(stack);
    }

    // --- EKLENEN KISIM: UYUMLULUK KONTROLÜ ---
    @Override
    public boolean checkCompatibility(Enchantment other) {
        // Eğer diğer büyü Spirit Guard ise FALSE döndür
        return other != ReverieModEnchantments.SPIRIT_GUARD.get() && super.checkCompatibility(other);
    }
}