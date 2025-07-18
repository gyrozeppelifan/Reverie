package net.eris.reverie.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;

public class DrunkardPlaybackConditionProcedure {
	public static boolean execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return false;
		if (!world.isClientSide()) {
			if (entity.isSprinting()) {
				return false;
			}
		}
		return true;
	}
}
