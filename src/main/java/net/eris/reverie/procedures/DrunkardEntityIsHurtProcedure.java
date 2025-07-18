package net.eris.reverie.procedures;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import net.eris.reverie.entity.DrunkardEntity;

public class DrunkardEntityIsHurtProcedure {
	public static void execute(Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return;
		if ((sourceentity instanceof LivingEntity _entUseItem0 ? _entUseItem0.getUseItem() : ItemStack.EMPTY).getItem() == Items.STICK) {
			if (entity instanceof DrunkardEntity _datEntSetL)
				_datEntSetL.getEntityData().set(DrunkardEntity.DATA_isDrinking, true);
		} else if ((sourceentity instanceof LivingEntity _entUseItem3 ? _entUseItem3.getUseItem() : ItemStack.EMPTY).getItem() == Items.DIAMOND) {
			if (entity instanceof DrunkardEntity _datEntSetL)
				_datEntSetL.getEntityData().set(DrunkardEntity.DATA_isCharging, true);
		}
	}
}
