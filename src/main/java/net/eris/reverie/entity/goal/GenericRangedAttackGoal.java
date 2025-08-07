package net.eris.reverie.entity.goal;

import net.eris.reverie.entity.GoblinBruteEntity;
import net.eris.reverie.entity.ShooterGoblinEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.eris.reverie.init.ReverieModItems;
import net.eris.reverie.util.GoblinReputation;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class GenericRangedAttackGoal extends Goal {
    private final PathfinderMob mob;
    private LivingEntity target;
    private final double speed;
    private final int cooldown;
    private int attackTime = 0;
    private final float attackRange;
    private final float minAttackRange; // Yaklaşma limiti
    private int retreatTicks = 0;

    public GenericRangedAttackGoal(PathfinderMob mob, double speed, int cooldown, float range) {
        this.mob = mob;
        this.speed = speed;
        this.cooldown = cooldown;
        this.attackRange = range * range;
        this.minAttackRange = 6.0f * 6.0f; // 6 blok altına inerse kaçmaya başlar
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity possibleTarget = this.mob.getTarget();
        if (possibleTarget == null || !possibleTarget.isAlive()) return false;
        if (possibleTarget instanceof Player player) {
            GoblinReputation.State state = GoblinReputation.getState(player);
            // Sadece aggressive/neutral ise AI çalışsın:
            if (state == GoblinReputation.State.FRIENDLY || state == GoblinReputation.State.HELPFUL) return false;
            if (player.isCreative() || player.isSpectator()) return false;
        }
        ItemStack held = this.mob.getMainHandItem();
        return held != null && held.is(ReverieModItems.BONE_SPEAR.get());
    }


    @Override
    public void start() {
        this.target = this.mob.getTarget();
        this.attackTime = 0;
    }

    @Override
    public void stop() {
        this.target = null;
        this.attackTime = 0;
        this.retreatTicks = 0;
    }

    @Override
    public void tick() {
        if (this.target == null) return;

        double distSq = this.mob.distanceToSqr(this.target);
        boolean canSee = this.mob.getSensing().hasLineOfSight(this.target);

        // Eğer GoblinBrute'un üzerinde bir ShooterGoblin ise kaçma mantığını atla:
        boolean isRidden = this.mob.isPassenger() && this.mob.getVehicle() instanceof GoblinBruteEntity;
        if (!isRidden) {
            // Orijinal retreat mantığı
            if (distSq <= minAttackRange) {
                this.retreatTicks = 20;
            }
            if (this.retreatTicks > 0) {
                double dx = this.mob.getX() - this.target.getX();
                double dz = this.mob.getZ() - this.target.getZ();
                double mag = Math.sqrt(dx * dx + dz * dz);
                if (mag > 0.01) {
                    double nx = this.mob.getX() + (dx / mag) * 2.0;
                    double nz = this.mob.getZ() + (dz / mag) * 2.0;
                    this.mob.getNavigation().moveTo(nx, this.mob.getY(), nz, this.speed * 1.5);
                }
                this.retreatTicks--;
                this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
                return;
            }
        }

        // Normal AI: menzile girince ateşle, yoksa yaklaş
        if (distSq <= attackRange && canSee) {
            this.mob.getNavigation().stop();
            this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
            if (--this.attackTime <= 0) {
                ((ShooterGoblinEntity)this.mob).performRangedAttack(this.target, 1.0f);
                this.attackTime = this.cooldown;
            }
        } else {
            this.mob.getNavigation().moveTo(this.target, this.speed);
        }
    }
}
