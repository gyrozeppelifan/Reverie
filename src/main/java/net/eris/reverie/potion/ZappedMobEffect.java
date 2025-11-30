package net.eris.reverie.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ZappedMobEffect extends MobEffect {

    public ZappedMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x00FFFF); // Renk: Electric Cyan (Parlak Camgöbeği)

        // Hareketi kilitliyoruz (Slowness etkisi gibi ama bizim kontrolümüzde)
        // -1.0D speed = %100 yavaşlama (Olduğu yerde kalır)
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-514C1F160890",
                -1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Her 20 tickte (1 saniye) bir hasar vur
        if (entity.level().getGameTime() % 20 == 0) {
            entity.hurt(entity.damageSources().magic(), 2.0F); // 1 Kalp (2 Can) hasar

            // İstersen buraya minik bir titreme veya ses efekti de ekleyebiliriz ilerde
            // entity.playSound(SoundEvents.CREEPER_HURT, 1.0F, 2.0F);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // Her tick çalışsın
    }
}