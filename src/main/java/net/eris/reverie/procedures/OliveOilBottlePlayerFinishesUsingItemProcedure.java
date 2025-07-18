package net.eris.reverie.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;

import net.eris.reverie.init.ReverieModMobEffects;

public class OliveOilBottlePlayerFinishesUsingItemProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
			_entity.addEffect(new MobEffectInstance(ReverieModMobEffects.FOCUS.get(), 900, 1, true, true));
	}
}
