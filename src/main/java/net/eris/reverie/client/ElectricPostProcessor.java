package net.eris.reverie.client;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
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
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        // Liste boşsa hiç işlem yapma, performansı koru
        if (StitchedRenderer.electricEntitiesOnScreen.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // 1. Zinciri Yükle
        if (electricChain == null) {
            try {
                electricChain = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), POST_CHAIN_LOCATION);
                electricChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            } catch (IOException | JsonSyntaxException e) {
                // Hata logunu azalttım
                return;
            }
        }

        if (electricChain.getTempTarget("final").width != mc.getWindow().getWidth()) {
            electricChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
        }

        // 2. Hazırlık: Maske Bufferını Ayarla
        RenderTarget target = electricChain.getTempTarget("reverie:entity_mask");
        if (target == null) return;

        target.clear(Minecraft.ON_OSX); // Rengi temizle
        // target.clear(Minecraft.ON_DEPTH); // Derinliği temizlemeyelim, duvar arkası efekt için (X-Ray)
        target.setClearColor(0F, 0F, 0F, 0F);
        target.bindWrite(false);

        // 3. Çizim Başlıyor
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose(); // Stack'i koru

        // Kamera pozisyonunu al (Interpolasyonlu)
        Vec3 cameraPos = event.getCamera().getPosition();

        // Immediate Buffer
        MultiBufferSource.BufferSource immediateBuffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        // ConcurrentModification hatasını önlemek için listeyi kopyalayalım
        List<StitchedEntity> entitiesToRender = new ArrayList<>(StitchedRenderer.electricEntitiesOnScreen);

        // Önemli: X-Ray efekti için depth testi kapatıyoruz
        RenderSystem.disableDepthTest();

        for (StitchedEntity entity : entitiesToRender) {
            EntityRenderer<? super StitchedEntity> renderer = mc.getEntityRenderDispatcher().getRenderer(entity);

            if (renderer instanceof StitchedRenderer stitchedRenderer) {
                // Pozisyon hesapla: Entity - Kamera
                double x = Mth.lerp(event.getPartialTick(), entity.xo, entity.getX()) - cameraPos.x;
                double y = Mth.lerp(event.getPartialTick(), entity.yo, entity.getY()) - cameraPos.y;
                double z = Mth.lerp(event.getPartialTick(), entity.zo, entity.getZ()) - cameraPos.z;

                poseStack.pushPose();
                poseStack.translate(x, y, z);

                // Rotasyonları uygula (EntityRenderDispatcher normalde bunu yapar, biz manuel yapıyoruz)
                float bodyRot = Mth.rotLerp(event.getPartialTick(), entity.yBodyRotO, entity.yBodyRot);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyRot));

                // Yeni metodumuzu çağırıyoruz!
                stitchedRenderer.renderModelDirectly(entity, event.getPartialTick(), poseStack, immediateBuffer, 15728640); // Full Light

                poseStack.popPose();
            }
        }

        immediateBuffer.endBatch(); // Çizimi bitir
        RenderSystem.enableDepthTest(); // Depth testi geri aç
        poseStack.popPose(); // Stack'i geri al

        // Listeyi temizle (Bir sonraki kare için)
        StitchedRenderer.electricEntitiesOnScreen.clear();

        // 4. Shader İşle
        mc.getMainRenderTarget().bindWrite(false);
        electricChain.process(event.getPartialTick());

        // 5. Ekrana Bas
        RenderTarget output = electricChain.getTempTarget("final");
        if (output != null) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest(); // Efekt blokların üstüne çizilsin
            output.blitToScreen(mc.getWindow().getWidth(), mc.getWindow().getHeight(), false);
            RenderSystem.enableDepthTest();
        }
    }
}