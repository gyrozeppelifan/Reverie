package net.eris.reverie.client;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.renderer.StitchedRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ElectricPostProcessor {

    private static PostChain electricChain;
    private static final ResourceLocation POST_CHAIN_LOCATION = new ResourceLocation(ReverieMod.MODID, "shaders/post/entity_outline_electric.json");

    @SubscribeEvent
    public static void renderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // 1. Zinciri Yükle
        if (electricChain == null) {
            try {
                electricChain = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), POST_CHAIN_LOCATION);
                electricChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
                ReverieMod.LOGGER.info("Elektrik Shader Zinciri YÜKLENDİ!");
            } catch (IOException | JsonSyntaxException e) {
                if (mc.player.tickCount % 200 == 0) {
                    ReverieMod.LOGGER.error("Shader Hatası:", e);
                }
                return;
            }
        }

        if (electricChain.getTempTarget("final").width != mc.getWindow().getWidth()) {
            electricChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
        }

        // 2. Hazırlık: Gizli Tuvali Temizle
        RenderTarget target = electricChain.getTempTarget("final");
        target.clear(Minecraft.ON_OSX); // Eski çizimleri sil
        target.setClearColor(0F, 0F, 0F, 0F); // Şeffaf yap
        target.bindWrite(false); // Tuvali aktif et

        // 3. Çizim: Entity'leri Tuvale Bas
        // DİKKAT: Depth Test'i kapatıyoruz ki duvar arkasındaki entityler de tuvale çizilsin (X-Ray için şart)
        RenderSystem.disableDepthTest();

        PoseStack poseStack = event.getPoseStack();

        // --- KRİTİK DÜZELTME: IMMEDIATE BUFFER ---
        // Standart buffer yerine bunu kullanıyoruz ki tuvale anında yazılsın.
        MultiBufferSource.BufferSource immediateBuffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        // Renderer'a çiz emrini veriyoruz
        StitchedRenderer.renderElectricBatch(poseStack, immediateBuffer, event.getPartialTick(), event.getCamera());

        // Çizimi bitir ve tuvale işle
        immediateBuffer.endBatch();

        // Ayarları geri al
        RenderSystem.enableDepthTest();

        // 4. İşle (Zinciri Çalıştır)
        mc.getMainRenderTarget().bindWrite(false); // Ana ekrana dön
        electricChain.process(event.getPartialTick()); // Shader efektini uygula

        // 5. Sonucu Ekrana Bas (Blit)
        RenderTarget output = electricChain.getTempTarget("final");
        if (output != null) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            // Burada da Depth Test kapalı olmalı ki efekt blokların üzerine (önüne) çizilsin
            RenderSystem.disableDepthTest();

            output.blitToScreen(mc.getWindow().getWidth(), mc.getWindow().getHeight(), false);

            RenderSystem.enableDepthTest();
        }
    }
}