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

        // Ekran boyutu değiştiyse zinciri güncelle
        if (electricChain.getTempTarget("final").width != mc.getWindow().getWidth()) {
            electricChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
        }

        // 2. Hazırlık: Gizli Tuvali (entity_mask) Temizle ve Bağla
        // DİKKAT: Artık "final" değil, JSON'da tanımladığımız "reverie:entity_mask" hedefi!
        RenderTarget target = electricChain.getTempTarget("reverie:entity_mask");
        if (target == null) {
            ReverieMod.LOGGER.error("Shader hedefi 'reverie:entity_mask' bulunamadı! JSON dosyasını kontrol et.");
            return;
        }

        target.clear(Minecraft.ON_OSX); // Renk ve Derinlik temizle
        target.setClearColor(0F, 0F, 0F, 0F); // Arka planı şeffaf yap
        target.bindWrite(false); // Yazma moduna geç

        // 3. Çizim: Entity'leri Tuvale Bas
        PoseStack poseStack = event.getPoseStack();

        // Immediate Buffer kullanıyoruz
        MultiBufferSource.BufferSource immediateBuffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        // Renderer'a çiz emrini veriyoruz
        StitchedRenderer.renderElectricBatch(poseStack, immediateBuffer, event.getPartialTick(), event.getCamera());

        // Çizimi bitir (GPU'ya gönder)
        immediateBuffer.endBatch();

        // 4. İşle (Zinciri Çalıştır)
        mc.getMainRenderTarget().bindWrite(false); // Ana ekrana (Main Target) geri dön
        electricChain.process(event.getPartialTick()); // Zinciri çalıştır, maskeden alıp final'a işleyecek

        // 5. Sonucu Ekrana Bas (Blit)
        // Zincirin son çıktısı "final" veya "swap" target'ında birikmiş olmalı (JSON'a göre en son "final"a blit yapılıyor)
        RenderTarget output = electricChain.getTempTarget("final");
        if (output != null) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest(); // Blokların üzerine çizilmesi için depth test KAPALI

            output.blitToScreen(mc.getWindow().getWidth(), mc.getWindow().getHeight(), false);

            RenderSystem.enableDepthTest(); // Normale döndür
        }
    }
}