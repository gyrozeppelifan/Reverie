package net.eris.reverie.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

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

        if (ReverieClientEvents.ancientCloakShader != null) {
            float time = (entity.tickCount + partialTicks) / 20.0F;
            ReverieClientEvents.ancientCloakShader.getUniform("GameTime").set(time);
        }

        float yRot = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F;
        float xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());

        // --- ANA OK ---
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(xRot));

        // DÜZELTME: İNCECİK SCALE
        // Uzunluk (X): 2.0F (Daha uzun ışın)
        // Kalınlık (Y, Z): 0.15F (Çok ince)
        poseStack.scale(1.0F, 0.15F, 0.15F);

        VertexConsumer auraBuffer = buffer.getBuffer(MagicRenderType.getAquaAura(ARROW_LOCATION));
        this.drawArrowVertex(poseStack, auraBuffer, 255);

        poseStack.popPose();

        // --- TRAIL (İZLER) ---
        for (int i = 1; i <= 4; i++) {
            poseStack.pushPose();

            poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
            poseStack.mulPose(Axis.ZP.rotationDegrees(xRot));

            // İz mesafesi (Boyut uzadığı için biraz açtık)
            float trailDist = i * 0.5F;
            poseStack.translate(-trailDist, 0, 0);

            float trailScale = 1.0F - (i * 0.15F);
            // İzler de ince olsun
            poseStack.scale(2.0F * trailScale, 0.15F * trailScale, 0.15F * trailScale);

            VertexConsumer trailBuffer = buffer.getBuffer(MagicRenderType.getAquaAura(ARROW_LOCATION));

            int alpha = Math.max(0, 120 - (i * 30));

            this.drawArrowVertex(poseStack, trailBuffer, alpha);

            poseStack.popPose();
        }
    }

    private void drawArrowVertex(PoseStack poseStack, VertexConsumer consumer, int alpha) {
        PoseStack.Pose pose = poseStack.last();

        for(int j = 0; j < 4; ++j) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));

            consumer.vertex(pose.pose(), -8, -2, 0).color(100, 255, 255, alpha)
                    .uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(pose.normal(), 0, 0, 0).endVertex();
            consumer.vertex(pose.pose(), 8, -2, 0).color(100, 255, 255, alpha)
                    .uv(0.5F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(pose.normal(), 0, 0, 0).endVertex();
            consumer.vertex(pose.pose(), 8, 2, 0).color(100, 255, 255, alpha)
                    .uv(0.5F, 0.15625F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(pose.normal(), 0, 0, 0).endVertex();
            consumer.vertex(pose.pose(), -8, 2, 0).color(100, 255, 255, alpha)
                    .uv(0.0F, 0.15625F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(pose.normal(), 0, 0, 0).endVertex();
        }
    }

    private static class MagicRenderType extends RenderType {
        public MagicRenderType(String s, VertexFormat v, VertexFormat.Mode m, int i, boolean b, boolean b1, Runnable r, Runnable r1) {
            super(s, v, m, i, b, b1, r, r1);
        }
        public static RenderType getAquaAura(ResourceLocation texture) {
            return create("ancient_cloak_aura",
                    DefaultVertexFormat.POSITION_COLOR_TEX,
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