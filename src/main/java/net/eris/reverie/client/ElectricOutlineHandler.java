package net.eris.reverie.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting; // <-- EKSİK OLAN IMPORT
import net.eris.reverie.ReverieMod;
import net.eris.reverie.entity.StitchedEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ElectricOutlineHandler {

    private static ShaderInstance electricShader;
    private static RenderTarget outlineBuffer;

    // Bu metodu ReverieModClient.java içinden çağırıyorsun, o yüzden burada public static kalmalı.
    public static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(
                    event.getResourceProvider(),
                    new ResourceLocation(ReverieMod.MODID, "electric_outline"),
                    DefaultVertexFormat.POSITION
            ), shader -> electricShader = shader);
        } catch (IOException e) {
            ReverieMod.LOGGER.error("Failed to load electric outline shader", e);
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // Tüm dünya renderlandıktan sonra (Translucent dahil) araya giriyoruz
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || electricShader == null) return;

        // 1. Buffer Kontrolü
        if (outlineBuffer == null) {
            outlineBuffer = new TextureTarget(mc.getWindow().getWidth(), mc.getWindow().getHeight(), true, Minecraft.ON_OSX);
            outlineBuffer.setClearColor(0f, 0f, 0f, 0f);
        }
        if (outlineBuffer.width != mc.getWindow().getWidth() || outlineBuffer.height != mc.getWindow().getHeight()) {
            outlineBuffer.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight(), Minecraft.ON_OSX);
        }

        // 2. Özel Buffer'a Sadece Stitched Entitylerini Çiz
        outlineBuffer.clear(Minecraft.ON_OSX);
        outlineBuffer.bindWrite(false);

        PoseStack poseStack = event.getPoseStack();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof StitchedEntity stitched && stitched.getState() == 1) { // Sadece Çarpılma Anı
                // Kameraya göre pozisyonu ayarla
                double x = stitched.getX() - event.getCamera().getPosition().x;
                double y = stitched.getY() - event.getCamera().getPosition().y;
                double z = stitched.getZ() - event.getCamera().getPosition().z;

                poseStack.pushPose();
                poseStack.translate(x, y, z);

                // Entity Render Dispatcher ile çiz
                mc.getEntityRenderDispatcher().render(
                        stitched,
                        0, 0, 0,
                        0, event.getPartialTick(),
                        poseStack,
                        mc.renderBuffers().bufferSource(),
                        15728640 // Full Bright
                );

                poseStack.popPose();
            }
        }

        // Render Bufferlarını zorla çizdir (Flush)
        mc.renderBuffers().bufferSource().endBatch();

        // 3. Shader'ı Uygula ve Ana Ekrana Bas
        mc.getMainRenderTarget().bindWrite(false);

        electricShader.setSampler("DiffuseSampler", outlineBuffer.getColorTextureId());
        electricShader.safeGetUniform("OneTexel").set(1f / (float)outlineBuffer.width, 1f / (float)outlineBuffer.height);
        electricShader.safeGetUniform("Time").set(event.getRenderTick() / 20f); // Zamanı gönder

        electricShader.apply();

        // Ekranı kaplayan bir kare (Quad) çiz
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        Matrix4f matrix = new Matrix4f().setOrtho(0, mc.getWindow().getWidth(), mc.getWindow().getHeight(), 0, 1000, 3000);

        // --- DÜZELTME 1: VertexFormat.Mode.QUADS yerine VertexSorting.ORTHOGRAPHIC_Z ---
        RenderSystem.setProjectionMatrix(matrix, VertexSorting.ORTHOGRAPHIC_Z);

        // Tam ekran quad
        float w = mc.getWindow().getWidth();
        float h = mc.getWindow().getHeight();
        bufferbuilder.vertex(0, h, 0).endVertex();
        bufferbuilder.vertex(w, h, 0).endVertex();
        bufferbuilder.vertex(w, 0, 0).endVertex();
        bufferbuilder.vertex(0, 0, 0).endVertex();

        tesselator.end();
        electricShader.clear();

        // --- DÜZELTME 2: resetProjectionMatrix yerine setProjectionMatrix ile geri yükleme ---
        // Normal dünya render'ı için 'DISTANCE_TO_ORIGIN' sıralaması kullanılır.
        RenderSystem.setProjectionMatrix(event.getProjectionMatrix(), VertexSorting.DISTANCE_TO_ORIGIN);
    }
}