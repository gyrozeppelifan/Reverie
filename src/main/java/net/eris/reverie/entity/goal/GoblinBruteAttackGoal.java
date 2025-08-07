package net.eris.reverie.entity.goal;

import net.eris.reverie.entity.GoblinBruteEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class GoblinBruteAttackGoal extends Goal {
    private final GoblinBruteEntity brute;
    private LivingEntity target;
    private int seeTime = 0;

    public GoblinBruteAttackGoal(GoblinBruteEntity brute) {
        this.brute = brute;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        target = brute.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        seeTime = 0;
    }

    @Override
    public void stop() {
        target = null;
        brute.setState(GoblinBruteEntity.BruteState.IDLE);
    }

    @Override
    public void tick() {
        brute.getLookControl().setLookAt(target, 30.0F, 30.0F);

        double distanceSq = brute.distanceToSqr(target);

        if (brute.hasLineOfSight(target)) {
            seeTime++;
        } else {
            seeTime = 0;
        }

        // Target'a git!
        if (seeTime > 5) {
            brute.getNavigation().moveTo(target, 1.7D); // 1.7D = daha agresif
            if (brute.getState() != GoblinBruteEntity.BruteState.CHARGING)
                brute.setState(GoblinBruteEntity.BruteState.CHARGING);
        } else {
            brute.getNavigation().stop();
            brute.setState(GoblinBruteEntity.BruteState.SEEK_PLAYER);
        }

        // Saldırı menziline girince vur, state ANGRY'ye geç
        double attackReach = brute.getBbWidth() * 2.0F + target.getBbWidth();
        if (distanceSq <= attackReach * attackReach && seeTime > 5) {
            brute.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            target.hurt(brute.damageSources().mobAttack(brute), 20.0F);
            brute.setState(GoblinBruteEntity.BruteState.ANGRY);
            // Knockback
            float dx = (float)(brute.getX() - target.getX());
            float dz = (float)(brute.getZ() - target.getZ());
            target.knockback(4.0F, dx, dz);
        }

        // Target öldüyse IDLE
        if (!target.isAlive()) {
            brute.setState(GoblinBruteEntity.BruteState.IDLE);
            brute.getNavigation().stop();
        }
    }
}
