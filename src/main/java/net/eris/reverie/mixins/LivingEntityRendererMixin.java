package net.eris.reverie.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.eris.reverie.client.renderer.layer.DrunkenOutlineLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Inject(
        method = "<init>(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Lnet/minecraft/client/model/EntityModel;F)V", 
        at = @At("RETURN")
    )
    private void onCtor(Context ctx, M model, float shadowSize, CallbackInfo ci) {
        @SuppressWarnings("unchecked")
        LivingEntityRenderer<T, M> self = (LivingEntityRenderer<T, M>)(Object)this;
        self.addLayer(new DrunkenOutlineLayer<>(self));
    }
}
