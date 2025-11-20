package net.eris.reverie.entity.goal;

import net.eris.reverie.entity.GoblinBruteEntity;
import net.eris.reverie.entity.ShooterGoblinEntity;
import net.eris.reverie.init.ReverieModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * Goal for Goblin Brute to allow a Shooter Goblin to mount on its back.
 */
public class ShooterMountGoal extends Goal {
    private final GoblinBruteEntity brute;
    private ShooterGoblinEntity shooter;

    public ShooterMountGoal(GoblinBruteEntity brute) {
        this.brute = brute;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (brute.getState() != GoblinBruteEntity.BruteState.SEEK_SHOOTER) return false;

        List<ShooterGoblinEntity> list = brute.level().getEntitiesOfClass(
                ShooterGoblinEntity.class,
                brute.getBoundingBox().inflate(5.0),
                e -> e.getVehicle() == null
        );
        if (list.isEmpty()) return false;

        shooter = list.stream()
                .min(Comparator.comparingDouble(e -> brute.distanceTo(e)))
                .orElse(list.get(0));
        return true;
    }

    @Override
    public void start() {
        // if already within 3 blocks, mount immediately
        if (shooter != null && brute.distanceTo(shooter) <= 3.0) {
            mountShooter();
        } else if (shooter != null) {
            brute.getNavigation().moveTo(shooter, 1.0);
        }
    }

    @Override
    public void tick() {
        if (shooter == null) return;

        double dist = brute.distanceTo(shooter);
        if (dist <= 3.0) {
            mountShooter();
        } else {
            brute.getNavigation().moveTo(shooter, 1.0);
        }
    }

    private void mountShooter() {
        shooter.startRiding(brute, true);
        brute.setState(GoblinBruteEntity.BruteState.CARRY_SHOOTER);

        // play team-up sound
        brute.level().playSound(
                null,
                brute.getX(), brute.getY(), brute.getZ(),
                ReverieModSounds.SHOOTER_BRUTE_TEAMUP.get(),
                SoundSource.HOSTILE,
                1.2F, 1.0F
        );

        // spawn visible particles
        if (brute.level() instanceof ServerLevel server) {
            Vec3 pos = brute.position().add(0, 1.2, 0);
            server.sendParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    pos.x, pos.y, pos.z,
                    20,
                    0.5, 0.5, 0.5,
                    0.05
            );
        }
    }

    @Override
    public boolean canContinueToUse() {
        return brute.getPassengers().stream()
                .noneMatch(e -> e instanceof ShooterGoblinEntity);
    }

    @Override
    public void stop() {
        shooter = null;
        super.stop();
    }
}
