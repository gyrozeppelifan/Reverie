package net.eris.reverie.enchantment;

import net.eris.reverie.init.ReverieModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class RecallEnchantment extends Enchantment {
    public RecallEnchantment() {
        // Rarity: RARE (Nadir)
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3; // Seviye arttıkça daha hızlı geri dönsün
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.is(ReverieModItems.SPIKED_LOG_ITEM.get());
    }

    // --- KRİTİK KISIM: UYUMSUZLUK ---
    // Ricochet ile aynı anda basılamaz!
    @Override
    public boolean checkCompatibility(Enchantment other) {
        return !(other instanceof RicochetEnchantment) && super.checkCompatibility(other);
    }
}