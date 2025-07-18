package net.eris.reverie.procedures;

import net.eris.reverie.entity.DrunkardEntity;
import net.eris.reverie.init.ReverieModMobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class DrunkardStagesProcedure {
    public static void execute(LevelAccessor world, Entity entity) {
        if (!(entity instanceof DrunkardEntity drunkard) 
         || !(world instanceof Level lvl) 
         || lvl.isClientSide)
            return;

        boolean isDrinking = drunkard.getEntityData().get(DrunkardEntity.DATA_isDrinking);
        int     canDrink   = drunkard.getEntityData().get(DrunkardEntity.DATA_canDrink);
        int     timer      = drunkard.getEntityData().get(DrunkardEntity.DATA_DrinkTime);

        // 1) İçme aşaması
        if (isDrinking) {
            drunkard.setNoAi(true);
            drunkard.getNavigation().stop();
            drunkard.setDeltaMovement(0, 0, 0);

            int t = timer - 1;
            drunkard.getEntityData().set(DrunkardEntity.DATA_DrinkTime, t);

            if (t <= 0) {
                // AI tekrar aç
                drunkard.setNoAi(false);
                drunkard.getEntityData().set(DrunkardEntity.DATA_isDrinking, false);

                // **30s Drunken Rage efekti ver**
                drunkard.addEffect(new MobEffectInstance(
                    ReverieModMobEffects.DRUNKEN_RAGE.get(),
                    600,   // 600 tick = 30s
                    0,     // amplification = 0
                    false, // showParticles
                    true   // showIcon
                ));

                // 60s cooldown başlat
                drunkard.getEntityData().set(DrunkardEntity.DATA_DrinkTime, 1200);
                drunkard.getEntityData().set(DrunkardEntity.DATA_canDrink,   2);
            }
            return;
        }

        // 2) Cooldown aşaması
        if (canDrink == 2) {
            int t = timer - 1;
            drunkard.getEntityData().set(DrunkardEntity.DATA_DrinkTime, t);
            if (t <= 0) {
                drunkard.getEntityData().set(DrunkardEntity.DATA_canDrink, 1);
            }
        }
    }
}
