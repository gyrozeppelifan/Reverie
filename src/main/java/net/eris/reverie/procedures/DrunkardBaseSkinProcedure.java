package net.eris.reverie.procedures;

import net.minecraft.world.entity.Entity;

import net.eris.reverie.entity.DrunkardEntity;

public class DrunkardBaseSkinProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if ((entity instanceof DrunkardEntity _datEntL0 && _datEntL0.getEntityData().get(DrunkardEntity.DATA_Cyclops)) == false) {
			return true;
		}
		return false;
	}
}
