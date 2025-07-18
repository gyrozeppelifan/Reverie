package net.eris.reverie.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import net.eris.reverie.init.ReverieModMobEffects;
import net.eris.reverie.entity.DrunkardEntity;

public class DrunkardStageUpdateProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		if (!world.isClientSide()) {
			if (entity instanceof LivingEntity _livEnt1 && _livEnt1.hasEffect(ReverieModMobEffects.DRUNKEN_RAGE.get())) {
				if (entity instanceof DrunkardEntity _datEntSetL)
					_datEntSetL.getEntityData().set(DrunkardEntity.DATA_isCharging, true);
			} else {
				if (entity instanceof DrunkardEntity _datEntSetL)
					_datEntSetL.getEntityData().set(DrunkardEntity.DATA_isCharging, false);
			}
		}
	}
}
