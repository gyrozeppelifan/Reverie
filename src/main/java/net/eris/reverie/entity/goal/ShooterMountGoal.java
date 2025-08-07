package net.eris.reverie.entity.goal;

import net.eris.reverie.entity.GoblinBruteEntity;
import net.eris.reverie.entity.ShooterGoblinEntity;
import net.eris.reverie.util.GoblinReputation;
import net.eris.reverie.util.GoblinReputation.State;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/**
 * Goal for Goblin Brute to allow a Shooter Goblin to mount on its back.
 */
public class ShooterMountGoal extends Goal {
    private final GoblinBruteEntity brute;
    public GoblinBruteEntity getBrute() {
        return brute;
    }
    private ShooterGoblinEntity shooter;

    public ShooterMountGoal(GoblinBruteEntity brute) {
        this.brute = brute;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Only when in SEEK_SHOOTER state
        if (brute.getState() != GoblinBruteEntity.BruteState.SEEK_SHOOTER) return false;
        // Find nearest idle shooter within 5 blocks
        List<ShooterGoblinEntity> list = brute.level().getEntitiesOfClass(ShooterGoblinEntity.class,
                brute.getBoundingBox().inflate(5),
                e -> e.getVehicle() == null);
        if (list.isEmpty()) return false;
        shooter = list.get(0);
        return true;
    }

    @Override
    public void start() {
        // Move toward shooter goblin
        brute.getNavigation().moveTo(shooter, 1.0);
    }

    @Override
    public void tick() {
        if (shooter != null) {
            brute.getNavigation().moveTo(shooter, 1.0);
            if (brute.distanceTo(shooter) < 1.5) {
                shooter.startRiding(brute, true);
                brute.setState(GoblinBruteEntity.BruteState.CARRY_SHOOTER);
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        // Stop once shooter has mounted
        return brute.getPassengers().stream().noneMatch(e -> e instanceof ShooterGoblinEntity);
    }
}
