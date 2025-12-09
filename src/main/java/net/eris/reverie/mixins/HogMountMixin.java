package net.eris.reverie.mixins;

import net.eris.reverie.entity.HogEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class HogMountMixin<T extends Entity> {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cancelRenderIfRidingHog(T entity, float entityYaw, float partialTicks, com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        // Eğer renderlanan entity bir Hog'a biniyorsa, VANILLA render'ı iptal et.
        // Çünkü biz onu HogRiderLayer ile zaten çiziyoruz.
        if (entity.getVehicle() instanceof HogEntity) {
            ci.cancel();
        }
    }
}