package net.eris.reverie.entity.goal;

import java.util.EnumSet;
import net.eris.reverie.entity.BarrelGoblinEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class OwnerAttackGoal extends Goal {
    private final BarrelGoblinEntity goblin;

    public OwnerAttackGoal(BarrelGoblinEntity goblin) {
        this.goblin = goblin;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        long tick = goblin.tickCount;
        LivingEntity target = goblin.consumePendingOwnerAttackTarget(tick);
        if (target != null && target.isAlive()) {
            goblin.setTarget(target);
            return true;
        }
        return false;
    }
}
