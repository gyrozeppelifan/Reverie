package net.eris.reverie.item;

import net.eris.reverie.init.ReverieModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;

public class BrokenBoozeBottleItem extends SwordItem {
    public BrokenBoozeBottleItem() {
        super(
            Tiers.WOOD,      // Tier.WOOD.getAttackDamageBonus() == 0
            2,               // +2 attack damage → toplam 2
            3.0F,            // attack speed = 4
            new Item.Properties()
                .stacksTo(1)
                .durability(32)
                .rarity(Rarity.RARE)
        );
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Sadece oyuncu vurduğunda kanama efekti ekle
        if (attacker instanceof Player) {
            // 6 saniye = 120 tick, amplifier 0
            target.addEffect(new MobEffectInstance(
                ReverieModMobEffects.BLEEDING.get(),
                140,
                0,
                false,  // ambient
                true    // showParticles
            ));
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
