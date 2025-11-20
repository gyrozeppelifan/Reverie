package net.eris.reverie.client.renderer.layer;

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
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Method;

// GENERIC SINIF: Her türlü LivingEntity ve Model için çalışır
public class AncientCloakLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private final LivingEntityRenderer<T, M> renderer;

    public AncientCloakLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
        this.renderer = (LivingEntityRenderer<T, M>) renderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        if (entity.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get())) {

            // --- DÜZELTME: İPTAL KONTROLÜNÜ KALDIRDIK ---
            // "if (entity.isInvisible()) return;" satırını sildik.
            // Çünkü zırhı gizlemek için entity'i zaten kodla görünmez yapıyoruz.
            // Eğer o satır kalırsa shader da iptal oluyor.

            // Shader Zamanı (Multiplayer Fix)
            if (ReverieClientEvents.ancientCloakShader != null) {
                ReverieClientEvents.ancientCloakShader.getUniform("GameTime").set(ageInTicks / 20.0F);
            }

            poseStack.pushPose();

            // Shell (Büyütme)
            float scale = 1.10F;
            poseStack.scale(scale, scale, scale);
            poseStack.translate(0.0D, -0.15D, 0.0D);

            this.getParentModel().copyPropertiesTo(this.getParentModel());

            // REFLECTION ILE TEXTURE ALMA
            ResourceLocation skin = getEntityTexture(entity);

            if (skin != null) {
                VertexConsumer vertexConsumer = buffer.getBuffer(CloakRenderType.getAquaAura(skin));

                this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight,
                        OverlayTexture.NO_OVERLAY,
                        1.0F, 1.0F, 1.0F, 0.6F);
            }

            poseStack.popPose();
        }
    }

    private ResourceLocation getEntityTexture(T entity) {
        try {
            Method method = EntityRenderer.class.getDeclaredMethod("getTextureLocation", net.minecraft.world.entity.Entity.class);
            method.setAccessible(true);
            return (ResourceLocation) method.invoke(renderer, entity);
        } catch (Exception e) {
            return null;
        }
    }

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
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setCullState(NO_CULL)
                            .setWriteMaskState(COLOR_WRITE)
                            .createCompositeState(false)
            );
        }
    }
}