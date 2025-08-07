package net.eris.reverie.entity.goal;

import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.eris.reverie.util.GoblinReputation;

public class GoblinBruteTargetPlayerGoal extends TargetGoal {
    private final Mob brute;
    private Player targetPlayer;

    public GoblinBruteTargetPlayerGoal(Mob brute) {
        super(brute, false, true);
        this.brute = brute;
    }

    @Override
    public boolean canUse() {
        // 16 blok yarıçapta saldırılabilir oyuncu ara
        for (Player p : brute.level().getEntitiesOfClass(Player.class, brute.getBoundingBox().inflate(64))) {
            if (p.isCreative() || p.isSpectator()) continue;
            var rep = GoblinReputation.getState(p);
            if (rep == GoblinReputation.State.AGGRESSIVE ||
                    (rep == GoblinReputation.State.NEUTRAL && brute.getLastHurtByMob() == p)) {
                this.targetPlayer = p;
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        if (targetPlayer != null && targetPlayer.isAlive()) {
            brute.setTarget(targetPlayer);
        }
        super.start();
    }
}
