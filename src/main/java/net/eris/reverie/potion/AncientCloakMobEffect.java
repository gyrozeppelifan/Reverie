package net.eris.reverie.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class AncientCloakMobEffect extends MobEffect {
    public AncientCloakMobEffect() {
        // BENEFICIAL: Yararlı efekt (Mavi/Yeşil çerçeve)
        // Renk: 0x00FFCC (Turkuazımsı hayalet rengi)
        super(MobEffectCategory.BENEFICIAL, 0x00FFCC);
    }

    // Bu efektin tick başına yaptığı özel bir işlem yok,
    // tüm büyü Event'lerde gerçekleşecek.
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Boş bırakıyoruz
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}