package net.eris.reverie.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.model.HogModel;
import net.eris.reverie.client.model.animations.HogAnimation;
import net.eris.reverie.client.renderer.layer.HogRainbowTrailLayer;
import net.eris.reverie.client.renderer.layer.HogRiderLayer;
import net.eris.reverie.entity.HogEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class HogRenderer extends MobRenderer<HogEntity, HogModel> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(ReverieMod.MODID, "textures/entities/hog.png");

    public HogRenderer(EntityRendererProvider.Context context) {
        // --- STITCHED USULÜ ---
        // Modeli constructor içinde oluşturup, metodunu burada eziyoruz.
        super(context, new HogModel(context.bakeLayer(HogModel.LAYER_LOCATION)) {
            @Override
            public void setupAnim(HogEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
                // 1. Reset
                this.root().getAllParts().forEach(ModelPart::resetPose);

                // 2. Animasyonları Oynat
                float walkSpeed = Math.min(limbSwingAmount * 1.5F, 1.0F);

                // HOG ANIMATION WALKING KULLANIYORUZ
                this.animate(entity.walkState, HogAnimation.walking, ageInTicks, walkSpeed);
                this.animate(entity.idleState, HogAnimation.idle, ageInTicks, 1.0F);
                this.animate(entity.dashState, HogAnimation.dash, ageInTicks, 1.0F);
                this.animate(entity.roarState, HogAnimation.roar, ageInTicks, 1.0F);
                this.animate(entity.flyState, HogAnimation.fly, ageInTicks, 1.0F);

                // 3. FİZİK (Nose Dive)
                float verticalSpeed = (float) entity.getDeltaMovement().y;

                if (entity.isUnicornMode()) {
                    this.root.xRot = entity.getXRot() * ((float)Math.PI / 180F);
                } else {
                    if (verticalSpeed > 0.05) {
                        // Zıplama
                        this.root.xRot -= 0.4F;
                        this.leg1left.xRot -= 0.6F;
                        this.leg1right.xRot -= 0.6F;
                    }
                    else if (verticalSpeed < -0.05) {
                        // Düşme
                        this.root.xRot += Math.min(Math.abs(verticalSpeed) * 2.0F, 0.8F);

                        float drag = Math.min(Math.abs(verticalSpeed), 1.2F);
                        this.leg2left.xRot += drag;
                        this.leg2right.xRot += drag;
                    }
                }

                // 4. Görünürlük
                this.setWingsVisible(entity.isUnicornMode());
            }
        }, 1.0F);

        this.addLayer(new HogRiderLayer(this));
        this.addLayer(new HogRainbowTrailLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(HogEntity entity) {
        return TEXTURE;
    }
}