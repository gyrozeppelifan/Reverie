package net.eris.reverie.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class BoarMonkSealItem extends Item {
    public BoarMonkSealItem() {
        super(new Item.Properties()
                .rarity(Rarity.RARE) // İsim rengi Mavi
                .durability(10)      // Canı 10
                .stacksTo(1));       // Tekli taşınır
    }

    // --- EKLENEN KISIM: BÜYÜ DEĞERİ ---
    @Override
    public int getEnchantmentValue() {
        return 15; // Demir aletlerle aynı seviye. Yüksek yaparsan (örn: 22) daha iyi büyüler gelir.
    }

    // Bu metod şart değil (durability varsa zaten true döner) ama garanti olsun diye ekleyelim.
    @Override
    public boolean isEnchantable(ItemStack pStack) {
        return true;
    }
}