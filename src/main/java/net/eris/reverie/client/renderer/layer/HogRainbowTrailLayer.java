package net.eris.reverie.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.model.HogModel;
import net.eris.reverie.entity.HogEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class HogRainbowTrailLayer extends RenderLayer<HogEntity, HogModel> {

    // Gökkuşağı texture'ı (Yatay bir renk şeridi olmalı)
    private static final ResourceLocation RAINBOW_TEXTURE = new ResourceLocation(ReverieMod.MODID, "textures/particle/rainbow_trail.png");

    public HogRainbowTrailLayer(RenderLayerParent<HogEntity, HogModel> pRenderer) {
        super(pRenderer);
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, HogEntity pHog, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        // Eğer iz listesi boşsa veya görünmezse çizme
        if (pHog.isInvisible() || pHog.trailHistory.size() < 2) return;

        // RenderType: Işık saçan, yarı saydam, çift taraflı (Cull yok)
        VertexConsumer consumer = pBuffer.getBuffer(RenderType.entityTranslucent(RAINBOW_TEXTURE));

        pPoseStack.pushPose();

        // Layer, domuzun o anki konumuna göre çizilir (0,0,0 = domuzun merkezi).
        // Ama iz dünya koordinatlarında. O yüzden domuzun hareketini tersine çeviriyoruz (Global Space).
        double lerpX = Mth.lerp(pPartialTick, pHog.xo, pHog.getX());
        double lerpY = Mth.lerp(pPartialTick, pHog.yo, pHog.getY());
        double lerpZ = Mth.lerp(pPartialTick, pHog.zo, pHog.getZ());

        pPoseStack.translate(-lerpX, -lerpY, -lerpZ);

        Matrix4f matrix = pPoseStack.last().pose();

        // Şerit Çizimi
        float width = 0.5F; // Şerit genişliği
        int size = pHog.trailHistory.size();

        for (int i = 0; i < size - 1; i++) {
            Vec3 pos1 = pHog.trailHistory.get(i);
            Vec3 pos2 = pHog.trailHistory.get(i + 1);

            // UV Koordinatları (Akıyormuş gibi görünmesi için zamanla kaydırabilirsin)
            float u1 = (float) i / size;
            float u2 = (float) (i + 1) / size;

            // Quad 1 (Üst/Alt)
            addVertex(consumer, matrix, pos1, width, u1, 0, 15728880);
            addVertex(consumer, matrix, pos2, width, u2, 0, 15728880);
            addVertex(consumer, matrix, pos2, -width, u2, 1, 15728880);
            addVertex(consumer, matrix, pos1, -width, u1, 1, 15728880);
        }

        pPoseStack.popPose();
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, Vec3 pos, float offset, float u, float v, int light) {
        consumer.vertex(matrix, (float)pos.x, (float)pos.y + offset, (float)pos.z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(0, 1, 0)
                .endVertex();
    }
}