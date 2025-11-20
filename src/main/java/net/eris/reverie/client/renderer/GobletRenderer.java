package net.eris.reverie.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import net.eris.reverie.entity.GobletEntity;
import net.eris.reverie.client.model.ModelGoblet;

public class GobletRenderer extends MobRenderer<GobletEntity, ModelGoblet<GobletEntity>> {
    public GobletRenderer(EntityRendererProvider.Context context) {
        super(context, new AnimatedModel(context.bakeLayer(ModelGoblet.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(GobletEntity entity) {
        return new ResourceLocation("reverie:textures/entities/goblet.png");
    }

    @Override
    public void render(GobletEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        int time = entity.getRemainingGrowth();
        float s = entity.getStretchScale(partialTicks);

        matrixStack.pushPose();

        // son 100 tick'te shake+stretch
        if (time < 100) {
            float progress = (100 - time) / 100f;
            // shake amplitude max 0.05 blok
            float amp = 0.15f * progress;
            float phase = (entity.tickCount + partialTicks) * 0.8f;
            float shakeX = (float)(Math.sin(phase * 2.7f) * amp);
            float shakeZ = (float)(Math.cos(phase * 3.4f) * amp);
            matrixStack.translate(shakeX, 0, shakeZ);
        }

        // vertical stretch + horizontal squash
        matrixStack.scale(1f / s, s, 1f / s);

        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
        matrixStack.popPose();
    }

    // AnimatedModel aynen duruyor...
    private static final class AnimatedModel extends ModelGoblet<GobletEntity> {
        private final net.minecraft.client.model.geom.ModelPart root;
        private final net.minecraft.client.model.HierarchicalModel<GobletEntity> animator =
                new net.minecraft.client.model.HierarchicalModel<GobletEntity>() {
                    @Override public ModelPart root() { return root; }
                    @Override
                    public void setupAnim(GobletEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
                        this.root().getAllParts().forEach(ModelPart::resetPose);
                        this.animate(entity.animationState0, net.eris.reverie.client.model.animations.GobletAnimation.idle, ageInTicks, 1f);
                        this.animateWalk(net.eris.reverie.client.model.animations.GobletAnimation.walk, limbSwing, limbSwingAmount, 1f, 1f);
                    }
                };

        public AnimatedModel(net.minecraft.client.model.geom.ModelPart root) {
            super(root);
            this.root = root;
        }

        @Override
        public void setupAnim(GobletEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            animator.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }
}
