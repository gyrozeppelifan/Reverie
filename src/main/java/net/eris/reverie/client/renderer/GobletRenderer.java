
package net.eris.reverie.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.HierarchicalModel;

import net.eris.reverie.entity.GobletEntity;
import net.eris.reverie.client.model.animations.GobletAnimation;
import net.eris.reverie.client.model.ModelGoblet;

public class GobletRenderer extends MobRenderer<GobletEntity, ModelGoblet<GobletEntity>> {
    public GobletRenderer(EntityRendererProvider.Context context) {
        super(context, new AnimatedModel(context.bakeLayer(ModelGoblet.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(GobletEntity entity) {
        return new ResourceLocation("reverie:textures/entities/goblet.png");
    }

    private static final class AnimatedModel extends ModelGoblet<GobletEntity> {
        private final ModelPart root;
        private final HierarchicalModel animator = new HierarchicalModel<GobletEntity>() {
            @Override
            public ModelPart root() {
                return root;
            }

            @Override
            public void setupAnim(GobletEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
                this.root().getAllParts().forEach(ModelPart::resetPose);
                this.animate(entity.animationState0, GobletAnimation.idle, ageInTicks, 1f);
                this.animateWalk(GobletAnimation.walk, limbSwing, limbSwingAmount, 1f, 1f);
            }
        };

        public AnimatedModel(ModelPart root) {
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
