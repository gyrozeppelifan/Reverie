package net.eris.reverie.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.model.MutagenBlobModel;
import net.eris.reverie.entity.projectile.MutagenBlobEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class MutagenBlobRenderer extends EntityRenderer<MutagenBlobEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(ReverieMod.MODID, "textures/entities/mutagen_blob.png");
    private final MutagenBlobModel<MutagenBlobEntity> model;

    public MutagenBlobRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new MutagenBlobModel<>(context.bakeLayer(MutagenBlobModel.LAYER_LOCATION));
    }

    @Override
    public void render(MutagenBlobEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // --- DÜZELTME BURADA ---
        // 0.5D yerine 1.5D yapıyoruz.
        // Blockbench modelleri (Java Entity) Y=24 noktasını taban alır.
        // 24 piksel = 1.5 blok olduğu için modeli 1.5 birim yukarı itmemiz lazım.
        poseStack.translate(0.0D, 1.5D, 0.0D);

        poseStack.scale(-1.0F, -1.0F, 1.0F); // Modeli düzelt (Ters çevir)

        // Havadayken dönüş efekti
        if (!entity.onGround()) {
            float rotY = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
            float rotX = Mth.rotLerp(partialTicks, entity.xRotO, entity.getXRot());
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotY));
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(rotX));
        } else {
            // Yerdeyken düz dursun (isteğe bağlı, top gibi yuvarlansın dersen üstteki if bloğunu kaldırıp direkt rotasyon verebilirsin)
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(entity.getYRot()));
        }

        VertexConsumer vertexconsumer = buffer.getBuffer(this.model.renderType(TEXTURE));
        this.model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTicks, 0.0F, 0.0F);
        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(MutagenBlobEntity entity) {
        return TEXTURE;
    }
}