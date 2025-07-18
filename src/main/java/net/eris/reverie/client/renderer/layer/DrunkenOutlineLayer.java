package net.eris.reverie.client.renderer.layer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;

// EKLENDİ: DRUNKEN_RAGE efekt kontrolü için gerekli import
import net.eris.reverie.init.ReverieModMobEffects;

public class DrunkenOutlineLayer<T extends LivingEntity, M extends EntityModel<T>>
    extends RenderLayer<T, M> {

    private static final ResourceLocation SWIRL_OVERLAY =
        new ResourceLocation("reverie", "textures/entities/foam_overlay.png");
    private static final ResourceLocation WHITE =
        new ResourceLocation("minecraft", "textures/misc/white.png");

    private static final float SCROLL_SPEED = 0.015f;
    private static final float SWIRL_ALPHA  = 0.25f;
    private static final float PINK_ALPHA   = 0.3f;

    public DrunkenOutlineLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack ms,
                       MultiBufferSource buffers,
                       int light,
                       T entity,
                       float limbSwing,
                       float limbSwingAmt,
                       float partialTicks,
                       float ageInTicks,
                       float netHeadYaw,
                       float headPitch) {

        // EKLENDİ: Sadece DRUNKEN_RAGE etkisi altındaysa devam et, yoksa çık
        if (!entity.hasEffect(ReverieModMobEffects.DRUNKEN_RAGE.get())) {
            return;
        }

        // 1) UV-scroll için zaman
        float t    = entity.tickCount + partialTicks;
        float uOff = t * SCROLL_SPEED;
        float vOff = t * SCROLL_SPEED;

        // --- Swirl pass: aynı model, UV kaydırmalı overlay ---
        VertexConsumer swirlVb = buffers.getBuffer(
            RenderType.energySwirl(SWIRL_OVERLAY, uOff, vOff)
        );
        this.getParentModel().renderToBuffer(
            ms, swirlVb, light,
            OverlayTexture.NO_OVERLAY,
            1f, 1f, 1f, SWIRL_ALPHA
        );

        // --- Pink tint pass: sabit beyaz + magenta, fullbright ---
        RenderSystem.disableCull();  // iç yüzeyleri de kapla
        VertexConsumer pinkVb = buffers.getBuffer(
            RenderType.entityTranslucent(WHITE)
        );
        int fullBright = 0xF000F0;
        this.getParentModel().renderToBuffer(
            ms, pinkVb, fullBright,
            OverlayTexture.NO_OVERLAY,
            1f, 0f, 1f, PINK_ALPHA
        );
        RenderSystem.enableCull();
    }
}
