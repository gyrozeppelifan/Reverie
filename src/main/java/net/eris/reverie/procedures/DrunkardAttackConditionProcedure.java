package net.eris.reverie.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;

import net.eris.reverie.entity.DrunkardEntity;

public class DrunkardAttackConditionProcedure {
	public static boolean execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return false;
		if (!world.isClientSide()) {
			if ((entity instanceof DrunkardEntity _datEntL1 && _datEntL1.getEntityData().get(DrunkardEntity.DATA_isAttacking)) == true) {
				return true;
			}
		}
		return false;
	}
}
