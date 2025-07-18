package net.eris.reverie.events;

import net.eris.reverie.entity.DrunkardEntity;
import net.eris.reverie.init.ReverieModMobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "reverie", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DrunkardBleedingAttackHandler {
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        DamageSource src = event.getSource();
        Entity attackerEntity = src.getEntity();
        if (attackerEntity instanceof DrunkardEntity drunkard
         && drunkard.getEntityData().get(DrunkardEntity.DATA_hasBrokenBottle)) {
            LivingEntity target = event.getEntity();
            // 5 saniye (100 tick) için Bleeding I efekti uygula
            target.addEffect(new MobEffectInstance(
                ReverieModMobEffects.BLEEDING.get(),
                140,   // duration in ticks
                0,     // amplifier
                false, // showParticles
                true   // showIcon
            ));
        }
    }
}
