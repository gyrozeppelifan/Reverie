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
import org.joml.Vector3f;

public class HogRainbowTrailLayer extends RenderLayer<HogEntity, HogModel> {

    private static final ResourceLocation RAINBOW_TEXTURE = new ResourceLocation(ReverieMod.MODID, "textures/particle/rainbow_trail.png");

    public HogRainbowTrailLayer(RenderLayerParent<HogEntity, HogModel> pRenderer) {
        super(pRenderer);
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, HogEntity pHog, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        if (pHog.isInvisible() || pHog.trailHistory.size() < 2) return;

        VertexConsumer consumer = pBuffer.getBuffer(RenderType.entityTranslucent(RAINBOW_TEXTURE));

        pPoseStack.pushPose();
        // NOT: Burada PoseStack'i döndürmüyoruz!
        // Bunun yerine aşağıda noktaları hesaplarken ters döndüreceğiz.

        Matrix4f matrix = pPoseStack.last().pose();

        double lerpX = Mth.lerp(pPartialTick, pHog.xo, pHog.getX());
        double lerpY = Mth.lerp(pPartialTick, pHog.yo, pHog.getY());
        double lerpZ = Mth.lerp(pPartialTick, pHog.zo, pHog.getZ());

        float bodyRot = Mth.rotLerp(pPartialTick, pHog.yBodyRotO, pHog.yBodyRot);
        // Domuzun render rotasyonunun tersi (Yerel uzaya çevirmek için)
        float inverseRotRad = (float)Math.toRadians(bodyRot - 180.0F);

        float width = 0.5F;
        int size = pHog.trailHistory.size();

        for (int i = 0; i < size - 1; i++) {
            Vec3 worldPos1 = pHog.trailHistory.get(i);
            Vec3 worldPos2 = pHog.trailHistory.get(i + 1);

            // 1. Dünya pozisyonundan Entity pozisyonunu çıkar (Relative Position)
            Vec3 rel1 = worldPos1.subtract(lerpX, lerpY, lerpZ);
            Vec3 rel2 = worldPos2.subtract(lerpX, lerpY, lerpZ);

            // 2. Bu vektörleri domuzun dönüşünün tersi yönünde döndür.
            // Böylece Render sistemi domuzu döndürdüğünde, bu noktalar sabit kalır.
            Vector3f v1 = new Vector3f((float)rel1.x, (float)rel1.y, (float)rel1.z);
            v1.rotateY(inverseRotRad);

            Vector3f v2 = new Vector3f((float)rel2.x, (float)rel2.y, (float)rel2.z);
            v2.rotateY(inverseRotRad);

            // UV
            float u1 = (float) i / size;
            float u2 = (float) (i + 1) / size;

            // Çizim (Artık v1 ve v2 yerel koordinatta sabitlendi)
            addVertex(consumer, matrix, v1, width, u1, 0, 15728880);
            addVertex(consumer, matrix, v2, width, u2, 0, 15728880);
            addVertex(consumer, matrix, v2, -width, u2, 1, 15728880);
            addVertex(consumer, matrix, v1, -width, u1, 1, 15728880);
        }

        pPoseStack.popPose();
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, Vector3f pos, float offset, float u, float v, int light) {
        consumer.vertex(matrix, pos.x, pos.y + offset, pos.z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(0, 1, 0)
                .endVertex();
    }
}