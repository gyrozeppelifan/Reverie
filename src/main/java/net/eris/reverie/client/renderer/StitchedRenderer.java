package net.eris.reverie.client.renderer;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.model.Stitched;
import net.eris.reverie.client.model.animations.StitchedAnimation;
import net.eris.reverie.entity.StitchedEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class StitchedRenderer extends MobRenderer<StitchedEntity, Stitched<StitchedEntity>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(ReverieMod.MODID, "textures/entities/stitched.png");
    private static final ResourceLocation TEXTURE_SKELETON = new ResourceLocation(ReverieMod.MODID, "textures/entities/stitched_skeleton.png");

    public StitchedRenderer(EntityRendererProvider.Context context) {
        super(context, new AnimatedModel(context.bakeLayer(Stitched.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(StitchedEntity entity) {
        // SADECE State 1 (Electrocuted) iken yanıp sön!
        // State 2 (Standup) veya 4 (Waiting) iken normal texture kalsın.
        if (entity.getState() == 1) {
            if (entity.tickCount % 2 == 0) {
                return TEXTURE_SKELETON;
            }
        }
        return TEXTURE;
    }

    private static final class AnimatedModel extends Stitched<StitchedEntity> {
        private final ModelPart root;

        private final HierarchicalModel<StitchedEntity> animator = new HierarchicalModel<StitchedEntity>() {
            @Override
            public ModelPart root() {
                return root;
            }

            @Override
            public void setupAnim(StitchedEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
                this.root().getAllParts().forEach(ModelPart::resetPose);

                // State 0 veya 4 -> Passive Animasyon
                if (entity.getState() == 0 || entity.getState() == 4) {
                    this.animate(entity.passiveState, StitchedAnimation.passive, ageInTicks, 1f);
                } else {
                    this.animate(entity.passiveState, StitchedAnimation.passive, ageInTicks, 1f); // Fallback
                }

                this.animate(entity.electrocutedState, StitchedAnimation.electrocuted, ageInTicks, 1.3f);
                this.animate(entity.standupState, StitchedAnimation.standup, ageInTicks, 0.7f);

                // this.animate(entity.walkState, StitchedAnimation.walk, ageInTicks, 1f);
            }
        };

        public AnimatedModel(ModelPart root) {
            super(root);
            this.root = root;
        }

        @Override
        public void setupAnim(StitchedEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            animator.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }
}