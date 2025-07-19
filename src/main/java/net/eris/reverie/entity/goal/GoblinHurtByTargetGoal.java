package net.eris.reverie.entity.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.eris.reverie.util.GoblinReputation;
import net.minecraft.world.entity.player.Player;

public class GoblinHurtByTargetGoal extends HurtByTargetGoal {
    private final Mob mob;

    public GoblinHurtByTargetGoal(PathfinderMob mob) {
        super(mob);
        this.mob = mob;
    }

    @Override
    protected boolean canAttack(LivingEntity target, net.minecraft.world.entity.ai.targeting.TargetingConditions conditions) {
        if (target instanceof Player player) {
            GoblinReputation.State state = GoblinReputation.getState(player);
            // SADECE AGGRESSIVE veya NEUTRAL modda intikam alınsın!
            return state == GoblinReputation.State.AGGRESSIVE || state == GoblinReputation.State.NEUTRAL;
        }
        return super.canAttack(target, conditions);
    }
}