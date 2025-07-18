package net.eris.reverie.mixins;

import net.eris.reverie.init.ReverieModMobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public abstract class ProjectileFocusMixin {
    private Entity reverie$focusTarget;
    private int reverie$focusHomingTicks = 0;
    private int reverie$focusHomingMaxTicks = 0;

    @Inject(
      method = "shoot(DDDFF)V",
      at = @At("TAIL")
    )
    private void onShoot(double x, double y, double z,
                         float velocity, float inaccuracy,
                         CallbackInfo ci) {
        Projectile self = (Projectile)(Object)this;
        Entity owner = self.getOwner();
        if (!(owner instanceof LivingEntity living)) return;
        if (living.getEffect(ReverieModMobEffects.FOCUS.get()) == null) return;

        int amp = living.getEffect(ReverieModMobEffects.FOCUS.get()).getAmplifier();
        double radius = 10 + amp * 5;

        Entity target = living.level().getEntitiesOfClass(
            LivingEntity.class,
            living.getBoundingBox().inflate(radius),
            // *** BURADA FİLTRE VAR! ***
            e -> e != living
                 && e.isAlive()
                 && !e.isInvulnerable()
                 && (!(e instanceof net.minecraft.world.entity.player.Player player) || !player.isCreative())
        ).stream().min((a, b) ->
            Double.compare(a.distanceToSqr(living), b.distanceToSqr(living))
        ).orElse(null);

        this.reverie$focusTarget = target;
        this.reverie$focusHomingTicks = 0;

        if (target != null) {
            double distance = Math.sqrt(target.distanceToSqr(living));
            this.reverie$focusHomingMaxTicks = Math.max(10, (int)(distance / 10.0 * 10));
            Vec3 dir = target.getEyePosition(1.0F)
                             .subtract(self.position())
                             .normalize();
            self.setDeltaMovement(dir.scale(self.getDeltaMovement().length()));
        } else {
            this.reverie$focusHomingMaxTicks = 0;
        }
    }

    @Inject(
      method = "tick",
      at = @At("TAIL")
    )
    private void onTick(CallbackInfo ci) {
        if (this.reverie$focusHomingTicks >= this.reverie$focusHomingMaxTicks) return;
        this.reverie$focusHomingTicks++;

        Projectile self = (Projectile)(Object)this;
        Entity target = this.reverie$focusTarget;
        if (!(target instanceof LivingEntity living) || !living.isAlive()) return;

        Vec3 dir = living.getEyePosition(1.0F)
                        .subtract(self.position())
                        .normalize();
        self.setDeltaMovement(dir.scale(self.getDeltaMovement().length()));
    }
}
