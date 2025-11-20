// net/eris/reverie/mixins/MouseBlockerMixin.java
package net.eris.reverie.mixins;

import net.eris.reverie.init.ReverieModMobEffects;
import net.eris.reverie.possess.Targeting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseBlockerMixin {

    // public void onMove(long window, double dx, double dy)
    @Inject(method = "onMove(JDD)V", at = @At("HEAD"), cancellable = true)
    private void reverie$blockMouseMove(long window, double dx, double dy, CallbackInfo ci){
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null) return;

        if (p.hasEffect(ReverieModMobEffects.POSSESSION.get())){
            // oyuncunun bakışını blokla
            ci.cancel();

            // en yakın hedefe doğru kamerayı yumuşak çevir
            LivingEntity target = Targeting.findPreferredTarget(p, 16);
            if (target != null){
                Vec3 diff = target.position().add(0, target.getBbHeight()*0.5, 0)
                        .subtract(p.getEyePosition());
                double yaw = Math.toDegrees(Math.atan2(-diff.x, diff.z));
                double pitch = Math.toDegrees(-Math.atan2(diff.y, Math.sqrt(diff.x*diff.x+diff.z*diff.z)));

                p.setYRot(lerpAngle(p.getYRot(), (float)yaw, 0.35f));
                p.setXRot(lerpAngle(p.getXRot(), (float)pitch, 0.25f));
                p.yHeadRot = p.getYRot();
                p.yBodyRot = p.getYRot();
            }
        }
    }

    // public void onScroll(long window, double xOffset, double yOffset)
    @Inject(method = "onScroll(JDD)V", at = @At("HEAD"), cancellable = true)
    private void reverie$blockScroll(long window, double xOff, double yOff, CallbackInfo ci){
        var p = Minecraft.getInstance().player;
        if (p != null && p.hasEffect(ReverieModMobEffects.POSSESSION.get())){
            ci.cancel(); // tekerleğe basınca hotbar çevrilmesin
        }
    }

    private static float lerpAngle(float a, float b, float t){
        float d = (float)(((b - a + 540f) % 360f) - 180f);
        return a + d * t;
    }
}
