package net.eris.reverie.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.HierarchicalModel;

import net.eris.reverie.entity.SpikedLogEntity;
import net.eris.reverie.client.model.animations.spiked_logAnimation;
import net.eris.reverie.client.model.Modelspiked_log;

public class SpikedLogRenderer extends MobRenderer<SpikedLogEntity, Modelspiked_log<SpikedLogEntity>> {
    public SpikedLogRenderer(EntityRendererProvider.Context context) {
        super(context, new AnimatedModel(context.bakeLayer(Modelspiked_log.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(SpikedLogEntity entity) {
        return new ResourceLocation("reverie:textures/entities/spiked_log.png");
    }

    private static final class AnimatedModel extends Modelspiked_log<SpikedLogEntity> {
        private final ModelPart root;
        private final HierarchicalModel<SpikedLogEntity> animator = new HierarchicalModel<>() {
            @Override
            public ModelPart root() {
                return root;
            }

            @Override
            public void setupAnim(SpikedLogEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
                // Her frame önce pozları sıfırla
                this.root().getAllParts().forEach(ModelPart::resetPose);

                // Animasyon sadece isRolling == true iken
                if (entity.isRolling()) {
                    this.animate(entity.animationState0, spiked_logAnimation.roll, ageInTicks, 0.7f);
                    // jump animasyonunuz varsa:
                    // this.animate(entity.animationState0, spiked_logAnimation.jump, ageInTicks, 1f);
                }
            }
        };

        public AnimatedModel(ModelPart root) {
            super(root);
            this.root = root;
        }

        @Override
        public void setupAnim(SpikedLogEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            // Sadece bizim animator’ü çalıştır; super.setupAnim çağrısı yok
            animator.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }
}
