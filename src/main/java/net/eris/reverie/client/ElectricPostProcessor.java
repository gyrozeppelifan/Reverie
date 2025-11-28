package net.eris.reverie.client;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.renderer.StitchedRenderer;
import net.eris.reverie.entity.StitchedEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import com.mojang.blaze3d.platform.GlStateManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ElectricPostProcessor {

    private static PostChain electricChain;
    private static final ResourceLocation POST_CHAIN_LOCATION = new ResourceLocation(ReverieMod.MODID, "shaders/post/entity_outline_electric.json");

    @SubscribeEvent
    public static void renderLevelStage(RenderLevelStageEvent event) {
        // AFTER_WEATHER daha güvenli bir stage, her şey çizildikten sonra çalışır.
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;

        // Liste boşsa işlemciyi yorma
        if (StitchedRenderer.electricEntitiesOnScreen.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // 1. Zinciri Yükle veya Yenile
        if (electricChain == null) {
            try {
                electricChain = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), POST_CHAIN_LOCATION);
                electricChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            } catch (IOException | JsonSyntaxException e) {
                ReverieMod.LOGGER.error("Shader Zinciri Yüklenemedi: ", e);
                return;
            }
        }

        if (electricChain.getTempTarget("final").width != mc.getWindow().getWidth()) {
            electricChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
        }

        // 2. Maske Bufferını Temizle
        RenderTarget maskTarget = electricChain.getTempTarget("reverie:entity_mask");
        if (maskTarget == null) return;

        maskTarget.clear(Minecraft.ON_OSX); // Sadece rengi temizle, derinliği koru
        maskTarget.bindWrite(false);

        // 3. Entityleri Maske Bufferına Çiz
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        // Kamera pozisyonunu sıfırla ki entityleri doğru yere koyabilelim
        Vec3 cameraPos = event.getCamera().getPosition();

        // Buffer Kaynağı
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // Listeyi kopyala (Hata önlemi)
        List<StitchedEntity> entities = new ArrayList<>(StitchedRenderer.electricEntitiesOnScreen);

        // X-Ray Efekti için Depth Testi Kapatıyoruz (Duvar arkası)
        RenderSystem.disableDepthTest();

        for (StitchedEntity entity : entities) {
            EntityRenderer<? super StitchedEntity> renderer = mc.getEntityRenderDispatcher().getRenderer(entity);
            if (renderer instanceof StitchedRenderer stitchedRenderer) {
                // Pozisyonu ayarla
                double x = Mth.lerp(event.getPartialTick(), entity.xo, entity.getX()) - cameraPos.x;
                double y = Mth.lerp(event.getPartialTick(), entity.yo, entity.getY()) - cameraPos.y;
                double z = Mth.lerp(event.getPartialTick(), entity.zo, entity.getZ()) - cameraPos.z;

                poseStack.pushPose();
                poseStack.translate(x, y, z);

                // Gövde dönüşü
                float bodyRot = Mth.rotLerp(event.getPartialTick(), entity.yBodyRotO, entity.yBodyRot);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyRot));

                // Özel Render Metodunu Çağır
                // Shader için düz beyaz/kırmızı renk kullanmak yerine texture kullanıyoruz ama shader onu zaten ezecek.
                stitchedRenderer.renderModelDirectly(entity, event.getPartialTick(), poseStack, bufferSource, 15728640);

                poseStack.popPose();
            }
        }

        // Çizimi GPU'ya gönder
        bufferSource.endBatch();

        RenderSystem.enableDepthTest();
        poseStack.popPose();

        // Listeyi temizle
        StitchedRenderer.electricEntitiesOnScreen.clear();

        // 4. Shader Zincirini Çalıştır
        mc.getMainRenderTarget().bindWrite(false);
        electricChain.process(event.getPartialTick());

        // 5. Sonucu Ekrana Bas
        RenderTarget output = electricChain.getTempTarget("final");
        if (output != null) {
            // Blending ayarları (Siyah kısımları şeffaf yapmak için)
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );
            RenderSystem.disableDepthTest(); // Arayüzün/Blokların önüne çiz

            output.blitToScreen(mc.getWindow().getWidth(), mc.getWindow().getHeight(), false);

            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
        }
    }
}