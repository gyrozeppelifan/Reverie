package net.eris.reverie.entity.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.eris.reverie.entity.BarrelGoblinEntity;

public class FollowOwnerGoal extends Goal {
    private final BarrelGoblinEntity goblin;
    private LivingEntity owner;

    public FollowOwnerGoal(BarrelGoblinEntity goblin) {
        this.goblin = goblin;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        owner = goblin.getOwner();
        return owner != null
                && goblin.distanceToSqr(owner) > 4.0
                && !goblin.getNavigation().isDone();
    }

    @Override
    public void tick() {
        goblin.getNavigation().moveTo(owner, 1.0);
    }
}
