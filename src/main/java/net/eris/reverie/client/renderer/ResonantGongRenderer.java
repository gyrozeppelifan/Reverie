package net.eris.reverie.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.block.ResonantGongBlock;
import net.eris.reverie.block.entity.ResonantGongBlockEntity;
import net.eris.reverie.client.model.ResonantGongModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ResonantGongRenderer implements BlockEntityRenderer<ResonantGongBlockEntity> {

    private final ResonantGongModel model;
    private static final ResourceLocation TEXTURE = new ResourceLocation(ReverieMod.MODID, "textures/entity/resonant_gong.png");

    public ResonantGongRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new ResonantGongModel(context.bakeLayer(ResonantGongModel.LAYER_LOCATION));
    }
    @Override
    public void render(ResonantGongBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        // 1. Konumlandırma (Aynı kalıyor)
        Direction facing = entity.getBlockState().getValue(ResonantGongBlock.FACING);
        double xOff = 0.5;
        double zOff = 0.5;

        if (facing == Direction.NORTH) xOff = 1.0;
        if (facing == Direction.SOUTH) xOff = 0.0;
        if (facing == Direction.WEST)  zOff = 1.0;
        if (facing == Direction.EAST)  zOff = 0.0;

        poseStack.translate(xOff, 1.0, zOff);
        poseStack.scale(-1.0F, -1.0F, 1.0F);

        // 2. Yönlendirme (Aynı kalıyor)
        float yRot = -facing.toYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));

        // 3. ANİMASYON (YENİLENMİŞ AĞIR METAL FİZİĞİ)
        float swing = 0.0F;
        if (entity.ringTicks > 0) {
            float time = (float)entity.ringTicks - partialTick;

            // --- DEĞİŞİKLİK BURADA ---
            // Eski: Mth.sin(time * 0.3F) * 0.015F * time; (Çok hızlı ve genişti)

            // Yeni Formül Açıklaması:
            // time * 0.15F  -> Frekans (Hız). Düşürdük ki "ağır" sallansın.
            // 0.2F          -> Maksimum Açı (Radyan). ~11 derece. Gonglar 90 derece dönmez, azıcık oynar.
            // (time / 100.0F) -> Sönümleme. Zaman geçtikçe yavaşça durur.

            swing = Mth.sin(time * 0.15F) * 0.2F * (time / 100.0F);
        }

        this.model.setupAnimation(swing);

        // 4. Çizim (Aynı kalıyor)
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }
}