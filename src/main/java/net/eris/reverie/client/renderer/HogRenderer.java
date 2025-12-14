package net.eris.reverie.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.model.HogModel;
import net.eris.reverie.client.model.animations.HogAnimation;
// import net.eris.reverie.client.renderer.layer.HogRainbowTrailLayer; // ARTIK GEREK YOK
//import net.eris.reverie.client.renderer.layer.HogRiderLayer;
import net.eris.reverie.entity.HogEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class HogRenderer extends MobRenderer<HogEntity, HogModel> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(ReverieMod.MODID, "textures/entities/hog.png");

    public HogRenderer(EntityRendererProvider.Context context) {
        super(context, new HogModel(context.bakeLayer(HogModel.LAYER_LOCATION)) {
            @Override
            public void setupAnim(HogEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
                // 1. Reset
                this.root().getAllParts().forEach(ModelPart::resetPose);

                // 2. Animasyonları Oynat
                float speedFactor = Math.abs(entity.currentSpeed) > 0.01F ? Math.abs(entity.currentSpeed) * 4.0F : limbSwingAmount;
                float walkSpeed = Math.min(speedFactor, 2.5F);

                this.animate(entity.walkState, HogAnimation.walking, ageInTicks, walkSpeed);
                this.animate(entity.idleState, HogAnimation.idle, ageInTicks, 1.0F);
                this.animate(entity.roarState, HogAnimation.roar, ageInTicks, 1.0F);
                this.animate(entity.flyState, HogAnimation.fly, ageInTicks, 1.0F);

                // 3. FİZİK & DASH POZU
                if (entity.isDashing()) {
                    this.root.xRot += 0.35F;
                    this.head.xRot += 0.4F;
                    this.root.y += 2.0F;
                }

                if (!entity.onGround() && !entity.isUnicornMode()) {
                    float verticalSpeed = (float) entity.getDeltaMovement().y;
                    float targetRot = -verticalSpeed * 1.5F;
                    targetRot = Mth.clamp(targetRot, -0.8F, 0.8F);

                    this.root.xRot += targetRot;
                    this.leg1left.xRot += targetRot * 0.5F;
                    this.leg1right.xRot += targetRot * 0.5F;
                    this.leg2left.xRot -= targetRot * 0.5F;
                    this.leg2right.xRot -= targetRot * 0.5F;
                }

                if (entity.isUnicornMode()) {
                    this.root.xRot = entity.getXRot() * ((float)Math.PI / 180F);
                }

                // 4. Görünürlük
                this.setWingsVisible(entity.isUnicornMode());
            }
        }, 1.0F);

        //this.addLayer(new HogRiderLayer(this));
        // this.addLayer(new HogRainbowTrailLayer(this)); // BU SATIRI SİLDİM VEYA YORUMA ALDIM
    }

    @Override
    public ResourceLocation getTextureLocation(HogEntity entity) {
        return TEXTURE;
    }
}