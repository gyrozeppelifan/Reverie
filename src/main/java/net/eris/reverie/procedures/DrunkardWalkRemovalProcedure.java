package net.eris.reverie.procedures;

import net.minecraft.world.entity.Entity;

public class DrunkardWalkRemovalProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (!entity.isSprinting()) {
			return true;
		}
		return false;
	}
}
