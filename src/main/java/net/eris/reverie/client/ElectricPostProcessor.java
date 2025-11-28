package net.eris.reverie.client;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Axis;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.renderer.StitchedRenderer;
import net.eris.reverie.entity.StitchedEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ElectricPostProcessor {

    private static PostChain electricChain;
    private static final ResourceLocation POST_CHAIN_LOCATION = new ResourceLocation(ReverieMod.MODID, "shaders/post/entity_outline_electric.json");

    @SubscribeEvent
    public static void renderLevelStage(RenderLevelStageEvent event) {
        // Particles (Parçacıklar) çizildikten sonra çalış ki onları ezmeyelim
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        if (StitchedRenderer.electricEntitiesOnScreen.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // 1. Zinciri Yükle
        if (electricChain == null) {
            try {
                electricChain = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), POST_CHAIN_LOCATION);
                electricChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            } catch (IOException | JsonSyntaxException e) {
                ReverieMod.LOGGER.error("Shader Yükleme Hatası", e);
                return;
            }
        }

        if (electricChain.getTempTarget("final").width != mc.getWindow().getWidth()) {
            electricChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
        }

        // 2. TÜM BUFFERLARI TEMİZLE (Kritik Hamle!)
        // Eğer bunları temizlemezsek önceki kareden kalan siyahlıklar ekranı kaplar ve particleları siler.
        clearTarget(electricChain.getTempTarget("reverie:entity_mask"));
        clearTarget(electricChain.getTempTarget("swap"));
        clearTarget(electricChain.getTempTarget("final"));

        // 3. Entity Çizimi (Maske Oluşturma)
        RenderTarget maskTarget = electricChain.getTempTarget("reverie:entity_mask");
        if (maskTarget == null) return;

        maskTarget.bindWrite(false);

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        Vec3 cameraPos = event.getCamera().getPosition();
        MultiBufferSource.BufferSource immediateBuffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        List<StitchedEntity> entities = new ArrayList<>(StitchedRenderer.electricEntitiesOnScreen);

        // Entity'nin blokların arkasından da görünmesi için Depth Test kapalı
        RenderSystem.disableDepthTest();

        for (StitchedEntity entity : entities) {
            EntityRenderer<? super StitchedEntity> renderer = mc.getEntityRenderDispatcher().getRenderer(entity);
            if (renderer instanceof StitchedRenderer stitchedRenderer) {
                double x = Mth.lerp(event.getPartialTick(), entity.xo, entity.getX()) - cameraPos.x;
                double y = Mth.lerp(event.getPartialTick(), entity.yo, entity.getY()) - cameraPos.y;
                double z = Mth.lerp(event.getPartialTick(), entity.zo, entity.getZ()) - cameraPos.z;

                poseStack.pushPose();
                poseStack.translate(x, y, z);
                float bodyRot = Mth.lerp(event.getPartialTick(), entity.yBodyRotO, entity.yBodyRot);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyRot));

                stitchedRenderer.renderModelDirectly(entity, event.getPartialTick(), poseStack, immediateBuffer, 15728640);

                poseStack.popPose();
            }
        }

        immediateBuffer.endBatch();
        RenderSystem.enableDepthTest();
        poseStack.popPose();

        StitchedRenderer.electricEntitiesOnScreen.clear();

        // 4. İşle (Outline + Blur)
        mc.getMainRenderTarget().bindWrite(false);
        electricChain.process(event.getPartialTick());

        // 5. Sonucu Ekrana Bas (ADDITIVE BLENDING)
        // Burası çok önemli: Siyah pikselleri şeffaf, renklileri parlak yapar.
        // Böylece arkadaki particle'lar silinmez!
        RenderTarget output = electricChain.getTempTarget("final");
        if (output != null) {
            RenderSystem.enableBlend();

            // ADDITIVE MOD: (SourceAlpha, 1) -> Renkleri üst üste ekler
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO
            );

            RenderSystem.disableDepthTest(); // En öne çizilsin
            output.blitToScreen(mc.getWindow().getWidth(), mc.getWindow().getHeight(), false);
            RenderSystem.enableDepthTest();

            // Normal Blend'e dön
            RenderSystem.defaultBlendFunc();
        }
    }

    // Yardımcı Metod: Buffer Temizleyici
    private static void clearTarget(RenderTarget target) {
        if (target != null) {
            target.clear(Minecraft.ON_OSX); // Rengi sil
            target.setClearColor(0F, 0F, 0F, 0F); // Şeffaf yap
        }
    }
}