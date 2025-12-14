package net.eris.reverie.enchantment;

import net.eris.reverie.init.ReverieModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class InnerPeaceEnchantment extends Enchantment {
    public InnerPeaceEnchantment() {
        // Rarity.RARE: Nadir (Büyü masasında zor çıkar)
        // WEAPON: Silah kategorisi (Seal için uygun)
        // MAINHAND/OFFHAND: İki elde de çalışsın
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3; // Maksimum seviye 3 olsun
    }

    @Override
    public int getMinCost(int level) {
        return 15 + (level - 1) * 9; // Büyü masasında çıkma zorluğu
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // Sadece bizim Seal eşyasına basılabilsin!
        return stack.is(ReverieModItems.BOAR_MONK_SEAL.get()) || super.canEnchant(stack);
    }
}