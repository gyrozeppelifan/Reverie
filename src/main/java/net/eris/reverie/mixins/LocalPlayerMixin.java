package net.eris.reverie.mixins;

import net.eris.reverie.entity.PossessionPuppetEntity;
import net.eris.reverie.init.ReverieModMobEffects;
import net.eris.reverie.possess.Targeting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void reverie$headLockOnTick(CallbackInfo ci){
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = (LocalPlayer)(Object)this;
        if (p == null) return;

        // Menü açıkken dokunma ki ESC'de tıklamalar çalışsın
        if (mc.screen != null) return;

        if (!p.hasEffect(ReverieModMobEffects.POSSESSION.get())) return;

        // Önce canlı hedef; yoksa bakılan nokta
        LivingEntity t = Targeting.findPreferredTarget(p, 16);
        Vec3 aim = null;
        if (t != null && !(t instanceof PossessionPuppetEntity)) {
            aim = t.position().add(0, t.getBbHeight() * 0.75, 0);
        } else {
            var bp = Targeting.lookPoint(p, 12);
            if (bp != null) aim = new Vec3(bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5);
        }
        if (aim == null) return;

        Vec3 diff = aim.subtract(p.getEyePosition());
        double yawDeg   = Math.toDegrees(Math.atan2(-diff.x, diff.z));
        double pitchDeg = Math.toDegrees(-Math.atan2(diff.y, Math.sqrt(diff.x*diff.x + diff.z*diff.z)));

        float targetYaw   = (float)yawDeg;
        float targetPitch = Mth.clamp((float)pitchDeg, -89.9f, 89.9f);

        // yumuşak dönüş
        float newYaw   = lerpAngle(p.getYRot(), targetYaw,   0.35f);
        float newPitch = lerpAngle(p.getXRot(), targetPitch, 0.25f);

        p.setYRot(newYaw);
        p.setXRot(newPitch);
        p.yHeadRot = newYaw;
        p.yBodyRot = newYaw;
    }

    private static float lerpAngle(float a, float b, float t){
        float d = (float)(((b - a + 540f) % 360f) - 180f);
        return a + d * t;
    }
}
