package net.eris.reverie.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.eris.reverie.client.ReverieClientEvents;
import net.eris.reverie.client.model.ModelGoblinFlag;
import net.eris.reverie.entity.GoblinFlagEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class GoblinFlagRenderer extends EntityRenderer<GoblinFlagEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("reverie", "textures/entities/goblin_flag.png");
    private final ModelGoblinFlag<GoblinFlagEntity> model;

    public GoblinFlagRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ModelGoblinFlag<>(context.bakeLayer(ModelGoblinFlag.LAYER_LOCATION));
        this.shadowRadius = 0.4F;
    }

    @Override
    public ResourceLocation getTextureLocation(GoblinFlagEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(GoblinFlagEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        // 1. NORMAL RENDER (Katı Bayrak)
        poseStack.pushPose();
        setupRotations(entity, poseStack, entityYaw);
        float ageInTicks = entity.tickCount + partialTicks;
        model.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);

        VertexConsumer vcNormal = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.renderToBuffer(poseStack, vcNormal, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();

        // 2. RADYASYON RENDER
        // Eğer shader yüklüyse ve efekt aktifse
        if (ReverieClientEvents.goblinFlagGlowShader != null && entity.getGlowTicks() > 0) {

            // DEĞİŞİKLİK: Burayı da 100.0F'e bölüyoruz ki süre orantılı azalsın.
            float strength = (float)entity.getGlowTicks() / 120.0F;

            if (ReverieClientEvents.goblinFlagGlowShader.getUniform("GlowStrength") != null) {
                ReverieClientEvents.goblinFlagGlowShader.getUniform("GlowStrength").set(strength);
            }

            poseStack.pushPose();
            setupRotations(entity, poseStack, entityYaw);

            // SHELL TEKNİĞİ: Modeli büyüt
            // Attığın resimdeki gibi dışarı taşması için scale arttırıyoruz
            // --- DEĞİŞİKLİK BURADA ---
            // SHELL SCALE VE PİVOT DÜZELTME
            // scale'i 1.35'ten 1.15'e düşürdük, daha az dışarı taşsın.
            // translate değerini de azalttık ki pivot düzelsin.
            float scale = 1.05F;
            poseStack.scale(scale, scale, scale);
            // Translate değerini biraz düşürdük, büyüyen modelin ayakları yere bassın
            poseStack.translate(0.0D, 0.0D, 0.0D);
            // --- DEĞİŞİKLİK BİTTİ ---

// RenderType çağrısı
            VertexConsumer vcRadiation = bufferSource.getBuffer(ReverieRenderTypes.GOBLIN_RADIATION);

// Alpha 1.0, gerisini shader hallediyor
            model.renderToBuffer(poseStack, vcRadiation, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 0.8F);

            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    private void setupRotations(GoblinFlagEntity entity, PoseStack poseStack, float entityYaw) {
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0D, -1.62D, 0.0D);
    }

    // --- CUSTOM RENDER TYPES ---
    private static class ReverieRenderTypes extends RenderType {
        public ReverieRenderTypes(String s, VertexFormat v, VertexFormat.Mode m, int i, boolean b, boolean b1, Runnable r, Runnable r1) {
            super(s, v, m, i, b, b1, r, r1);
        }

        // SİHİRLİ DOKUNUŞ: ADDITIVE BLENDING (IŞIK MODU)
        // Bu ayar, rengi "boyamak" yerine "aydınlatmak" için kullanır.
        private static final TransparencyStateShard ADDITIVE_TRANSPARENCY = new TransparencyStateShard("additive_transparency", () -> {
            RenderSystem.enableBlend();
            // Kaynak: Alpha, Hedef: 1 (Yani arkadaki renge dokunma, benimkini üstüne ekle)
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        }, () -> {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        });

        public static final RenderType GOBLIN_RADIATION = create(
                "goblin_radiation",
                DefaultVertexFormat.POSITION_COLOR_TEX, // Alex's Caves shader formatı
                VertexFormat.Mode.QUADS,
                256,
                false,
                true, // Sortlama açık
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(() -> ReverieClientEvents.goblinFlagGlowShader))
                        .setTextureState(new RenderStateShard.TextureStateShard(GoblinFlagRenderer.TEXTURE, false, false))
                        .setTransparencyState(ADDITIVE_TRANSPARENCY) // <--- DEĞİŞTİ (Artık ışık gibi davranacak)
                        .setCullState(NO_CULL)         // Arkasını da gör
                        .setWriteMaskState(COLOR_WRITE) // Derinlik yok (Duvarların içinden de parlasın)
                        .createCompositeState(false)
        );
    }
}