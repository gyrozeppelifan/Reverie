package net.eris.reverie.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.eris.reverie.entity.BoneSpearProjectileEntity;
import net.eris.reverie.client.model.ModelBoneSpearProjectile; // Model dosyanın ismini burada kullan!

public class BoneSpearProjectileRenderer extends EntityRenderer<BoneSpearProjectileEntity> {
    private final ModelBoneSpearProjectile model;
    private static final ResourceLocation TEXTURE = new ResourceLocation("reverie", "textures/entities/bone_spear_entity.png");

    public BoneSpearProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ModelBoneSpearProjectile(context.bakeLayer(ModelBoneSpearProjectile.LAYER_LOCATION));
    }

    @Override
    public void render(BoneSpearProjectileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 90.0F));
        poseStack.translate(0.0D, 0.0D, 0.0D);
        model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTicks, 0.0F, 0.0F);
        VertexConsumer vb = buffer.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        model.renderToBuffer(poseStack, vb, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(BoneSpearProjectileEntity entity) {
        return TEXTURE;
    }
}
