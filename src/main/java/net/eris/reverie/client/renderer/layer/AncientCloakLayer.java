package net.eris.reverie.client.renderer.layer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.eris.reverie.client.ReverieClientEvents;
import net.eris.reverie.init.ReverieModMobEffects;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class AncientCloakLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public AncientCloakLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        // Sadece efekt varsa çiz
        if (player.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get())) {

            // Oyuncu görünmezse layerı çizme
            if (player.isInvisible()) return;

            poseStack.pushPose();

            // --- SHELL TEKNİĞİ ---
            float scale = 1.10F;
            poseStack.scale(scale, scale, scale);
            poseStack.translate(0.0D, -0.15D, 0.0D);

            this.getParentModel().copyPropertiesTo(this.getParentModel());

            ResourceLocation skin = player.getSkinTextureLocation();

            // DÜZELTME: Yardımcı sınıf üzerinden RenderType alıyoruz
            VertexConsumer vertexConsumer = buffer.getBuffer(CloakRenderType.getAquaAura(skin));

            this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight,
                    net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                    1.0F, 1.0F, 1.0F, 0.6F);

            poseStack.popPose();
        }
    }

    // --- DÜZELTME BURADA: YARDIMCI SINIF ---
    // RenderType'ı extend ettiği için protected değişkenlere erişebilir.
    private static class CloakRenderType extends RenderType {

        // Zorunlu constructor
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
                            // ARTIK HATA VERMEZ:
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setCullState(NO_CULL)
                            .setWriteMaskState(COLOR_WRITE)
                            .createCompositeState(false)
            );
        }
    }
}