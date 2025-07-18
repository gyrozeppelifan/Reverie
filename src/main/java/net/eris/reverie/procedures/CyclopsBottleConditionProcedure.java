package net.eris.reverie.procedures;

import net.minecraft.world.entity.Entity;

import net.eris.reverie.entity.DrunkardEntity;

public class CyclopsBottleConditionProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if ((entity instanceof DrunkardEntity _datEntL0 && _datEntL0.getEntityData().get(DrunkardEntity.DATA_hasBrokenBottle)) == false
				&& (entity instanceof DrunkardEntity _datEntL1 && _datEntL1.getEntityData().get(DrunkardEntity.DATA_Cyclops)) == true) {
			return true;
		}
		return false;
	}
}
