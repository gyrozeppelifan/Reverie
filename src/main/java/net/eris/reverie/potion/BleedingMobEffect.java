package net.eris.reverie.potion;

import net.eris.reverie.init.ReverieModParticleTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class BleedingMobEffect extends MobEffect {
    public BleedingMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B0000);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        List<ItemStack> cures = new ArrayList<>();
        cures.add(new ItemStack(Items.TOTEM_OF_UNDYING));
        return cures;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Her tick çalışsın
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        Level world = entity.level();

        // 1) Her 30 tick'te bleed hasarı
        if (entity.tickCount % 30 == 0) {
            ResourceKey<DamageType> key = ResourceKey.create(
                Registries.DAMAGE_TYPE,
                new ResourceLocation("reverie", "bleed")
            );
            Holder<DamageType> typeHolder = world.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(key);
            DamageSource bleedSrc = new DamageSource(typeHolder);
            entity.hurt(bleedSrc, 1.0F);
        }

        // 2) Her 8 tick'te blood partiküllerini tüm client'lara gönder
        if (world instanceof ServerLevel server && entity.tickCount % 8 == 0) {
            double px = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * entity.getBbWidth();
            double py = entity.getY() + entity.getBbHeight() * 0.5;
            double pz = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * entity.getBbWidth();
            double vx = (entity.getRandom().nextDouble() - 0.5) * 0.05;
            double vy = 0.06 + entity.getRandom().nextDouble() * 0.02;
            double vz = (entity.getRandom().nextDouble() - 0.5) * 0.05;

            SimpleParticleType bloodType = ReverieModParticleTypes.BLOOD.get();
            server.sendParticles(
                bloodType,
                px, py, pz,
                1,    // count
                vx, vy, vz,
                0.0D // speed (unused)
            );
        }

        // 3) Can oranına göre Invisible Slowness uygulama
        float ratio = entity.getHealth() / entity.getMaxHealth();
        int lvl = ratio < 0.2f ? 4
                : ratio < 0.4f ? 3
                : ratio < 0.6f ? 2
                : ratio < 0.8f ? 1
                : 0;
        if (lvl > 0) {
            entity.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                2,        // duration (ticks)
                lvl - 1,  // amplifier
                false,    // ambient
                false,    // particles
                false     // icon
            ));
        }
    }
}
