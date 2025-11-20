package net.eris.reverie.entity.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.eris.reverie.entity.GoblinBruteEntity;
import net.eris.reverie.util.GoblinReputation;

public class BruteMeleeAttackGoal extends MeleeAttackGoal {
    private final GoblinBruteEntity brute;
    private final double speed;

    public BruteMeleeAttackGoal(GoblinBruteEntity brute, double speed, boolean useLongMemory) {
        super(brute, speed, useLongMemory);
        this.brute = brute;
        this.speed = speed;
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) return false;

        GoblinBruteEntity.BruteState state = brute.getState();
        if (state == GoblinBruteEntity.BruteState.SEEK_PLAYER
                || state == GoblinBruteEntity.BruteState.CARRY_PLAYER
                || state == GoblinBruteEntity.BruteState.SEEK_SHOOTER
                || state == GoblinBruteEntity.BruteState.CARRY_SHOOTER) {
            return false;
        }

        LivingEntity tgt = brute.getTarget();
        if (!(tgt instanceof Player player)) return false;

        var rep = GoblinReputation.getState(player);
        if (rep == GoblinReputation.State.AGGRESSIVE) return true;
        if (rep == GoblinReputation.State.NEUTRAL)
            return brute.getLastHurtByMob() == player;

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && canUse();
    }

    @Override
    public void tick() {
        super.tick();
    }
}
