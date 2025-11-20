package net.eris.reverie.entity.goal;

import net.eris.reverie.entity.GoblinBruteEntity;
import net.eris.reverie.entity.ShooterGoblinEntity;
import net.eris.reverie.init.ReverieModItems;
import net.eris.reverie.util.GoblinReputation;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;

public class GenericRangedAttackGoal extends Goal {
    private static final TagKey<EntityType<?>> GOBLINS =
            TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("reverie", "goblins"));

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

    private static boolean isGoblin(Entity e) {
        return e != null && e.getType().is(GOBLINS);
    }

    // Bu goal’un hedef olarak kabul edeceği koşullar (mevcut mantığı bozmadan)
    private boolean isValidTarget(LivingEntity t) {
        if (t == null || !t.isAlive()) return false;
        if (isGoblin(t)) return false; // goblin taglıya asla hedef yok

        if (t instanceof Player p) {
            // 1.20.1 için doğru creative/spectator kontrolü
            if (p.isCreative() || p.isSpectator()) return false;

            GoblinReputation.State state = GoblinReputation.getState(p);
            // Sadece FRIENDLY/HELPFUL dışı (yani AGGRESSIVE veya NEUTRAL) saldırılabilir
            if (state == GoblinReputation.State.FRIENDLY || state == GoblinReputation.State.HELPFUL) return false;
        }
        return true;
    }

    private boolean hasWeapon() {
        ItemStack held = this.mob.getMainHandItem();
        return held != null && held.is(ReverieModItems.BONE_SPEAR.get());
    }

    @Override
    public boolean canUse() {
        LivingEntity possible = this.mob.getTarget();
        if (!hasWeapon()) return false;               // silah yoksa çalışmasın
        if (!isValidTarget(possible)) return false;   // goblin/creative/spectator/rep filtreleri

        this.target = possible;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        // Hedef geçersizleştiyse (ölü, creative’a geçti, goblin oldu vs.) bırak
        if (!hasWeapon()) return false;
        return isValidTarget(this.mob.getTarget());
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

        // Hedef koşulları her tick yeniden doğrula
        if (!isValidTarget(this.target)) {
            this.mob.setTarget(null);
            return;
        }

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
                if (this.mob instanceof ShooterGoblinEntity shooter) {
                    shooter.performRangedAttack(this.target, 1.0f);
                }
                this.attackTime = this.cooldown;
            }
        } else {
            this.mob.getNavigation().moveTo(this.target, this.speed);
        }
    }
}
