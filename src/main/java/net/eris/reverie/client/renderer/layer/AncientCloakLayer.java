package net.eris.reverie.client.renderer.layer;

import net.eris.reverie.util.IAncientCloakData;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.eris.reverie.client.ReverieClientEvents;
import net.eris.reverie.init.ReverieModMobEffects;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class AncientCloakLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    public AncientCloakLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        // 1. EFEKT KONTROLÜ
        boolean hasCloak = ((IAncientCloakData) entity).reverie$hasAncientCloak();

        if (hasCloak) {

            poseStack.pushPose();

            // 3. SHELL (Aura Büyüklüğü)
            float scale = 1.08F; // %8 Büyüt (Hale etkisi)
            poseStack.scale(scale, scale, scale);
            poseStack.translate(0.0D, 0.0D, 0.0D); // Pivot düzeltme

            this.getParentModel().copyPropertiesTo(this.getParentModel());

            // 4. TEXTURE ALMA (Basit ve Güvenli Yöntem)
            // RenderLayer zaten texture'a erişebilir, reflectiona gerek yok.
            ResourceLocation skin = this.getTextureLocation(entity);

            if (skin != null) {
                // Shaderlı Render Type'ı al
                VertexConsumer vertexConsumer = buffer.getBuffer(CloakRenderType.getAquaAura(skin));

                // 5. ÇİZİM
                // Full Bright (0xF000F0) ile çiziyoruz ki karanlıkta parlasın.
                this.getParentModel().renderToBuffer(poseStack, vertexConsumer, 0xF000F0,
                        OverlayTexture.NO_OVERLAY,
                        1.0F, 1.0F, 1.0F, 1.0F);
            }

            poseStack.popPose();
        }
    }

    // Render Type Yardımcısı
    private static class CloakRenderType extends RenderType {
        public CloakRenderType(String s, VertexFormat v, VertexFormat.Mode m, int i, boolean b, boolean b1, Runnable r, Runnable r1) {
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
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY) // Şeffaflık
                            .setCullState(NO_CULL)
                            .setWriteMaskState(COLOR_WRITE) // Derinlik yazma (Hayalet)
                            .createCompositeState(false)
            );
        }
    }
}