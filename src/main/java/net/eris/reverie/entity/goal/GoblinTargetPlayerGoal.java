package net.eris.reverie.entity.goal;

import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.eris.reverie.util.GoblinReputation;

public class GoblinTargetPlayerGoal extends TargetGoal {
    private final Mob mob;
    private Player targetPlayer;

    public GoblinTargetPlayerGoal(Mob mob) {
        super(mob, false, true);
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        for (Player player : mob.level().getEntitiesOfClass(Player.class, mob.getBoundingBox().inflate(16))) {
            // **Creative veya Spectator'da olanı direkt es geç**
            if (player.isCreative() || player.isSpectator()) continue;

            GoblinReputation.State state = GoblinReputation.getState(player);
            if (state == GoblinReputation.State.AGGRESSIVE) {
                this.targetPlayer = player;
                return true;
            }
            if (state == GoblinReputation.State.NEUTRAL && mob.getLastHurtByMob() == player) {
                this.targetPlayer = player;
                return true;
            }
        }
        return false;
    }


    @Override
    public void start() {
        if (this.targetPlayer != null && this.targetPlayer.isAlive()) {
            mob.setTarget(this.targetPlayer);
        }
        super.start();
    }
}
