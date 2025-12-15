package net.eris.reverie.item;

import net.eris.reverie.init.ReverieModEnchantments;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;

public class BoarMonkSealItem extends Item {
    public BoarMonkSealItem() {
        super(new Item.Properties()
                .rarity(Rarity.RARE)
                .durability(10)); // <-- DİKKAT: .stacksTo(1) satırını sildik!
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    @Override
    public boolean isEnchantable(ItemStack pStack) {
        return true;
    }
}