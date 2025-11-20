package net.eris.reverie.entity.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.eris.reverie.entity.GoblinBruteEntity;
import net.eris.reverie.util.GoblinReputation;

import java.util.EnumSet;
import java.util.List;

public class GrabPlayerGoal extends Goal {
    public static final double SEARCH_RADIUS = 16.0;
    private static final double PICKUP_DISTANCE = 2.5;
    // NEW: sqrt’sız karşılaştırma
    private static final double PICKUP_DISTANCE_SQR = PICKUP_DISTANCE * PICKUP_DISTANCE;

    private final GoblinBruteEntity brute;
    private Player target;

    // repath throttling
    private int repathCooldown = 0;

    public GrabPlayerGoal(GoblinBruteEntity brute) {
        this.brute = brute;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (brute.level().isClientSide) return false;
        if (brute.grabCooldown > 0) return false;
        if (brute.getState() != GoblinBruteEntity.BruteState.SEEK_PLAYER) return false;
        if (!brute.getPassengers().isEmpty()) return false;

        // Cooldown ve ateş hedefi olmadan hiç yeltenme
        if (!brute.isFireThrowOffCooldown()) return false;
        if (brute.getCachedFireTarget() == null) return false;

        List<Player> list = brute.level().getEntitiesOfClass(
                Player.class,
                brute.getBoundingBox().inflate(SEARCH_RADIUS, 3, SEARCH_RADIUS),
                p -> {
                    if (p.isCreative() || p.isSpectator()) return false;
                    var rep = GoblinReputation.getState(p);
                    if (rep == GoblinReputation.State.AGGRESSIVE) return true;
                    return rep == GoblinReputation.State.NEUTRAL && brute.getLastHurtByMob() == p;
                }
        );

        if (list.isEmpty()) {
            target = null;
            return false;
        }

        // NEW: distanceToSqr ile min seçimi
        target = list.stream()
                .min((a, b) -> Double.compare(brute.distanceToSqr(a), brute.distanceToSqr(b)))
                .orElse(null);

        return target != null;
    }

    @Override
    public void start() {
        repathCooldown = 0;
        if (target != null) brute.getNavigation().moveTo(target, 1.3);
    }

    @Override
    public boolean canContinueToUse() {
        if (brute.level().isClientSide) return false;
        if (target == null || !target.isAlive()) return false;
        if (brute.getState() != GoblinBruteEntity.BruteState.SEEK_PLAYER) return false;

        // NEW: hedef creative/spectator olduysa bırak
        if (target.isCreative() || target.isSpectator()) return false;

        // Cooldown/ateş hedefi uçtuysa kovalama iptal
        if (!brute.isFireThrowOffCooldown()) return false;
        if (brute.getCachedFireTarget() == null) return false;

        return brute.distanceTo(target) <= SEARCH_RADIUS;
    }

    @Override
    public void tick() {
        if (target == null || brute.level().isClientSide) return;

        double distSqr = brute.distanceToSqr(target);

        // repath throttling
        if (repathCooldown > 0) {
            repathCooldown--;
        } else if (brute.getNavigation().isDone()) {
            brute.getNavigation().moveTo(target, 1.15);
            repathCooldown = 10;
        }

        // NEW: sqrt’sız pickup check
        if (distSqr <= PICKUP_DISTANCE_SQR) {
            if (target.startRiding(brute, true)) {
                brute.setState(GoblinBruteEntity.BruteState.CARRY_PLAYER);
                brute.grabCooldown = 40;
            }
            this.stop();
        }
    }

    @Override
    public void stop() {
        target = null;
        brute.getNavigation().stop();
    }
}
