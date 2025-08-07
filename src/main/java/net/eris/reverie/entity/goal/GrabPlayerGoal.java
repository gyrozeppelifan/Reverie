package net.eris.reverie.entity.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.eris.reverie.entity.GoblinBruteEntity;
import net.eris.reverie.util.GoblinReputation;

import java.util.EnumSet;
import java.util.List;

public class GrabPlayerGoal extends Goal {
    // Arama yarıçapını 16 bloka çıkardık
    public static final double SEARCH_RADIUS = 16.0;
    private static final double PICKUP_DISTANCE = 2.5;
    private final GoblinBruteEntity brute;
    private Player target;

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

        // 1) SEARCH_RADIUS içinde bir hedef varsa başla:
        //    Y eksenini 3 blokla sınırladık, X/Z ekseninde SEARCH_RADIUS
        List<Player> list = brute.level().getEntitiesOfClass(
                Player.class,
                brute.getBoundingBox().inflate(SEARCH_RADIUS, 3, SEARCH_RADIUS),
                p -> {
                    if (p.isCreative() || p.isSpectator()) return false;
                    var rep = GoblinReputation.getState(p);
                    if (rep == GoblinReputation.State.AGGRESSIVE) return true;
                    return rep == GoblinReputation.State.NEUTRAL
                            && brute.getLastHurtByMob() == p;
                }
        );

        if (list.isEmpty()) {
            target = null;
            return false;
        }

        // 2) En yakınını seç:
        target = list.stream()
                .min((a, b) -> Double.compare(brute.distanceTo(a), brute.distanceTo(b)))
                .orElse(null);

        return target != null;
    }

    @Override
    public void start() {
        // sadece chase başlasın
        brute.getNavigation().moveTo(target, 1.15);
    }

    @Override
    public boolean canContinueToUse() {
        if (brute.level().isClientSide) return false;
        if (target == null || !target.isAlive()) return false;
        if (brute.getState() != GoblinBruteEntity.BruteState.SEEK_PLAYER) return false;
        // SEARCH_RADIUS içinde kaldığı sürece devam et
        return brute.distanceTo(target) <= SEARCH_RADIUS;
    }

    @Override
    public void tick() {
        if (target == null || brute.level().isClientSide) return;

        double dist = brute.distanceTo(target);

        // eğer önceki yol tamamlandıysa, yeniden hedefe yönlendir
        if (brute.getNavigation().isDone()) {
            brute.getNavigation().moveTo(target, 1.15);
        }

        // sadece PICKUP_DISTANCE içinde bindir:
        if (dist <= PICKUP_DISTANCE) {
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
