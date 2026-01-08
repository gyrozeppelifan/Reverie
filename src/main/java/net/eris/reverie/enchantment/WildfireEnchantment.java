package net.eris.reverie.enchantment;

import net.eris.reverie.init.ReverieModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class WildfireEnchantment extends Enchantment {
    public WildfireEnchantment() {
        // Rarity: RARE (Nadir)
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 1; // Tek seviye yeterli (Yanıyor mu yanmıyor mu?)
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // Sadece Spiked Log'a basılabilir
        return stack.is(ReverieModItems.SPIKED_LOG_ITEM.get());
    }
}