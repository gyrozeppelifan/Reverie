package net.eris.reverie.enchantment;

import net.eris.reverie.init.ReverieModEnchantments; // Bunu import etmeyi unutma!
import net.eris.reverie.init.ReverieModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class SpiritGuardEnchantment extends Enchantment {
    public SpiritGuardEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public int getMinCost(int level) {
        return 20 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.is(ReverieModItems.BOAR_MONK_SEAL.get()) || super.canEnchant(stack);
    }

    // --- EKLENEN KISIM: UYUMLULUK KONTROLÜ ---
    @Override
    public boolean checkCompatibility(Enchantment other) {
        // Eğer diğer büyü Spirit Deflection ise FALSE döndür (Yani uyumsuz)
        return other != ReverieModEnchantments.SPIRIT_DEFLECTION.get() && super.checkCompatibility(other);
    }
}