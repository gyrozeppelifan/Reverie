package net.eris.reverie.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.ReverieClientEvents;
import net.eris.reverie.entity.projectile.MagicArrow;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class MagicArrowRenderer extends ArrowRenderer<MagicArrow> {

    public static final ResourceLocation ARROW_LOCATION = new ResourceLocation("textures/entity/projectiles/arrow.png");

    public MagicArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(MagicArrow entity) {
        return ARROW_LOCATION;
    }

    @Override
    public void render(MagicArrow entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        // 1. NORMAL OK ÇİZİMİ
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        // 2. SHADER ZAMANI GÜNCELLEME
        if (ReverieClientEvents.ancientCloakShader != null) {
            float time = (entity.tickCount + partialTicks) / 20.0F;
            ReverieClientEvents.ancientCloakShader.getUniform("GameTime").set(time);
        }

        // Ortak Dönüş Hesaplamaları (Okun gidiş yönü)
        float yRot = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F;
        float xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());

        // --- 3. AURA KATMANI (SHELL) ---
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(xRot));

        // Oku biraz şişiriyoruz
        float scale = 1.2F;
        poseStack.scale(scale, scale, scale);

        VertexConsumer auraBuffer = buffer.getBuffer(MagicRenderType.getAquaAura(ARROW_LOCATION));

        // Ok modelini manuel çizmek zor olduğu için basit bir "Işık Hüzmesi" hilesi yapıyoruz
        // Veya ArrowRenderer'ın içindeki vertex çizimini kopyalayabiliriz ama
        // şimdilik basitçe aynı oku tekrar çizelim:
        this.renderArrowModel(poseStack, auraBuffer);

        poseStack.popPose();

        // --- 4. TRAIL (HAYALET İZLER) ---
        // Ok çok hızlı olduğu için arkasına 3 tane kopya koyuyoruz
        for (int i = 1; i <= 3; i++) {
            poseStack.pushPose();

            // Oku geriye ötele (Hızına göre değil, sabit mesafe ile)
            // Okun kendi local Z ekseni zaten gidiş yönüdür.
            // Geriye doğru (X ekseni ok modelinde ileri/geridir) kaydırıyoruz.
            float trailDist = i * 0.8F;

            // Dönüşleri uygula
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
            poseStack.mulPose(Axis.ZP.rotationDegrees(xRot));

            // Geriye çek
            poseStack.translate(-trailDist, 0, 0); // Ok modelinde X ekseni uzunluktur

            // Küçült
            float trailScale = 1.0F - (i * 0.2F);
            poseStack.scale(trailScale, trailScale, trailScale);

            // Şeffaf çiz
            VertexConsumer trailBuffer = buffer.getBuffer(MagicRenderType.getAquaAura(ARROW_LOCATION));
            this.renderArrowModel(poseStack, trailBuffer);

            poseStack.popPose();
        }
    }

    // Ok modelinin vertex çizimi (Vanilla kodundan basitleştirildi)
    private void renderArrowModel(PoseStack poseStack, VertexConsumer consumer) {
        PoseStack.Pose pose = poseStack.last();
        // Kuyruk
        this.vertex(pose, consumer, -7, -2, -2, 0.0F, 0.15625F, -1, 0, 0);
        this.vertex(pose, consumer, -7, -2, 2, 0.15625F, 0.15625F, -1, 0, 0);
        this.vertex(pose, consumer, -7, 2, 2, 0.15625F, 0.3125F, -1, 0, 0);
        this.vertex(pose, consumer, -7, 2, -2, 0.0F, 0.3125F, -1, 0, 0);
        // Gövde 1
        this.vertex(pose, consumer, -7, 2, -2, 0.0F, 0.15625F, 1, 0, 0);
        this.vertex(pose, consumer, 8, 2, -2, 0.5F, 0.15625F, 1, 0, 0);
        this.vertex(pose, consumer, 8, 2, 2, 0.5F, 0.3125F, 1, 0, 0);
        this.vertex(pose, consumer, -7, 2, 2, 0.0F, 0.3125F, 1, 0, 0);
        // Gövde 2
        this.vertex(pose, consumer, -7, -2, 2, 0.0F, 0.15625F, 1, 0, 0);
        this.vertex(pose, consumer, 8, -2, 2, 0.5F, 0.15625F, 1, 0, 0);
        this.vertex(pose, consumer, 8, -2, -2, 0.5F, 0.3125F, 1, 0, 0);
        this.vertex(pose, consumer, -7, -2, -2, 0.0F, 0.3125F, 1, 0, 0);
    }

    public void vertex(PoseStack.Pose pose, VertexConsumer consumer, int x, int y, int z, float u, float v, int normalX, int normalZ, int normalY) {
        consumer.vertex(pose.pose(), (float)x, (float)y, (float)z)
                .color(0, 255, 255, 255) // Aqua Renk Zorlaması
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0) // Full Bright
                .normal(pose.normal(), (float)normalX, (float)normalY, (float)normalZ)
                .endVertex();
    }

    // Render Type (Aynı Aqua Shader)
    private static class MagicRenderType extends RenderType {
        public MagicRenderType(String s, VertexFormat v, VertexFormat.Mode m, int i, boolean b, boolean b1, Runnable r, Runnable r1) {
            super(s, v, m, i, b, b1, r, r1);
        }
        public static RenderType getAquaAura(ResourceLocation texture) {
            return create("ancient_cloak_aura",
                    DefaultVertexFormat.POSITION_COLOR_TEX, // Normal verisini shader kullanmadığı için bu yeterli
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    true,
                    RenderType.CompositeState.builder()
                            .setShaderState(new RenderStateShard.ShaderStateShard(() -> ReverieClientEvents.ancientCloakShader))
                            .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setCullState(NO_CULL)
                            .setWriteMaskState(COLOR_WRITE)
                            .createCompositeState(false)
            );
        }
    }
}