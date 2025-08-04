package net.eris.reverie.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelPart;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.entity.GoblinBarrelEntity;
import net.eris.reverie.client.model.animations.ModelGoblinBarrelAnimation;
import net.eris.reverie.client.model.Modelgoblin_barrel;

import java.util.Set;
import java.util.Locale;

public class GoblinBarrelRenderer extends MobRenderer<GoblinBarrelEntity, Modelgoblin_barrel<GoblinBarrelEntity>> {
    // — NERD tester nick’leri, hepsi lowercase —
    private static final Set<String> NERD_TESTERS = Set.of(
            "orenburglu",
            "steve"
            // … başka tester nick’leri ekle
    );

    public GoblinBarrelRenderer(EntityRendererProvider.Context context) {
        super(context,
                new AnimatedModel(context.bakeLayer(Modelgoblin_barrel.LAYER_LOCATION)),
                0.5f
        );
    }

    @Override
    public ResourceLocation getTextureLocation(GoblinBarrelEntity entity) {
        String owner = entity.getOwnerName();
        if (owner != null && NERD_TESTERS.contains(owner.toLowerCase(Locale.ROOT))) {
            // Nerd tester’lara özel texture
            return new ResourceLocation(
                    ReverieMod.MODID,
                    "textures/entities/nerd_goblin_barrel.png"
            );
        }
        // Default texture
        return new ResourceLocation(
                ReverieMod.MODID,
                "textures/entities/goblin_barrel.png"
        );
    }

    private static final class AnimatedModel extends Modelgoblin_barrel<GoblinBarrelEntity> {
        private final ModelPart root;
        private final net.minecraft.client.model.HierarchicalModel<GoblinBarrelEntity> animator =
                new net.minecraft.client.model.HierarchicalModel<>() {
                    @Override
                    public ModelPart root() {
                        return root;
                    }
                    @Override
                    public void setupAnim(GoblinBarrelEntity entity, float limbSwing, float limbSwingAmount,
                                          float ageInTicks, float netHeadYaw, float headPitch) {
                        this.root().getAllParts().forEach(ModelPart::resetPose);
                        this.animate(entity.fallState,
                                ModelGoblinBarrelAnimation.fall, ageInTicks, 0.9f);
                        this.animate(entity.hitState,
                                ModelGoblinBarrelAnimation.hit, ageInTicks, 1f);
                        this.animate(entity.shakeState,
                                ModelGoblinBarrelAnimation.shake, ageInTicks, 0.8f);
                        this.animate(entity.preexplosionState,
                                ModelGoblinBarrelAnimation.preexplosion, ageInTicks, 0.7f);
                    }
                };

        public AnimatedModel(ModelPart root) {
            super(root);
            this.root = root;
        }

        @Override
        public void setupAnim(GoblinBarrelEntity entity, float limbSwing, float limbSwingAmount,
                              float ageInTicks, float netHeadYaw, float headPitch) {
            animator.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }
}
