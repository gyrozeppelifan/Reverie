package net.eris.reverie.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.model.HogModel;
import net.eris.reverie.client.model.animations.HogAnimation;
import net.eris.reverie.client.renderer.layer.HogRainbowTrailLayer;
import net.eris.reverie.client.renderer.layer.HogRiderLayer;
import net.eris.reverie.entity.HogEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class HogRenderer extends MobRenderer<HogEntity, HogModel> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(ReverieMod.MODID, "textures/entities/hog.png");

    public HogRenderer(EntityRendererProvider.Context context) {
        super(context, new AnimatedModel(context.bakeLayer(HogModel.LAYER_LOCATION)), 1.0F);

        // --- KATMANLAR ---
        this.addLayer(new HogRiderLayer(this));       // Biniciyi kemiğe yapıştır
        this.addLayer(new HogRainbowTrailLayer(this)); // Nyan Cat İzi
    }

    @Override
    public ResourceLocation getTextureLocation(HogEntity entity) {
        return TEXTURE;
    }

    // --- ANİMASYON VE FİZİK YÖNETİCİSİ ---
    private static final class AnimatedModel extends HogModel {
        private final ModelPart root;

        public AnimatedModel(ModelPart root) {
            super(root);
            this.root = root;
        }

        private final HierarchicalModel<HogEntity> animator = new HierarchicalModel<HogEntity>() {
            @Override public ModelPart root() { return root; }

            @Override
            public void setupAnim(HogEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
                this.root().getAllParts().forEach(ModelPart::resetPose);

                // 1. KEYFRAME ANIMASYONLARI
                // Hıza göre yürüme hızını ayarla
                float walkSpeed = Math.min(limbSwingAmount * 1.5F, 1.0F);
                this.animate(entity.walkState, HogAnimation.walking, ageInTicks, walkSpeed);

                this.animate(entity.idleState, HogAnimation.idle, ageInTicks, 1.0F);
                this.animate(entity.dashState, HogAnimation.dash, ageInTicks, 1.0F);
                this.animate(entity.roarState, HogAnimation.roar, ageInTicks, 1.0F);
                this.animate(entity.flyState, HogAnimation.fly, ageInTicks, 1.0F);

                // 2. PROCEDURAL FİZİK (Kodla Bükme)

                // A) Viraj Falsosu (Banking) - Tank gibi dönerken yana yatma
                float turnSpeed = Mth.wrapDegrees(entity.yBodyRot - entity.yBodyRotO);
                this.root().zRot += turnSpeed * 0.05F;

                // B) Tilki Atlayışı (Nose Dive) - Düşerken burnunu dikme
                float verticalSpeed = (float) entity.getDeltaMovement().y;

                if (entity.isUnicornMode()) {
                    // Uçarken daha yumuşak
                    this.root().xRot = verticalSpeed * -0.5F;
                } else {
                    // Düşerken/Zıplarken
                    if (verticalSpeed > 0.1) {
                        this.root().xRot -= 0.3F; // Şahlanma
                    } else if (verticalSpeed < -0.1) {
                        // Düşerken öne eğil (Maksimum 45 derece)
                        this.root().xRot += Math.min(Math.abs(verticalSpeed) * 1.5F, 0.8F);
                    }
                }

                // Görünürlük Ayarları
                this.setWingsVisible(entity.isUnicornMode());
                // Zırh kontrolü (Eğer envanterden okuyorsan buraya ekle)
                // this.setArmorVisible(entity.hasArmor());
            }

            private void setWingsVisible(boolean unicornMode) {
            }
        };

        @Override
        public void setupAnim(HogEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            animator.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }
}