
package net.eris.reverie.potion;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;

public class FocusMobEffect extends MobEffect {
	public FocusMobEffect() {
		super(MobEffectCategory.NEUTRAL, -6697984);
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return true;
	}
}
