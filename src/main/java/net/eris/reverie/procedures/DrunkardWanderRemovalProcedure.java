package net.eris.reverie.procedures;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class DrunkardWanderRemovalProcedure {
    public static boolean execute(Entity entity) {
        if (entity instanceof Mob mob) {
            LivingEntity target = mob.getTarget();
            return target == null; // ❗ hedef yoksa wander'a izin ver
        }
        return true; // ❗ Mob değilse yine wander'a izin ver
    }
}
