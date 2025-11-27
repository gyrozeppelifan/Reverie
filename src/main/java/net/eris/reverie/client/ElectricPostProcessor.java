package net.eris.reverie.client;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.entity.StitchedEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ElectricPostProcessor {

    private static PostChain electricChain;
    // DÜZELTME: Artık redglow değil, kendi dosyamızı çağırıyoruz
    private static final ResourceLocation POST_CHAIN_LOCATION = new ResourceLocation(ReverieMod.MODID, "entity_outline_electric");

    @SubscribeEvent
    public static void renderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // 1. Zinciri (Chain) Yükle
        if (electricChain == null) {
            try {
                electricChain = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), POST_CHAIN_LOCATION);
                electricChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            } catch (IOException | JsonSyntaxException e) {
                // Sadece 10 saniyede bir hata yazsın (Spam engelleme)
                if (mc.player.tickCount % 200 == 0) {
                    ReverieMod.LOGGER.error("Shader Yüklenemedi:", e);
                }
                return;
            }
        }

        if (electricChain.getTempTarget("final").width != mc.getWindow().getWidth()) {
            electricChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
        }

        // 2. Entity Kontrolü
        boolean hasEntity = false;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof StitchedEntity stitched && stitched.getState() == 1) {
                hasEntity = true;
                break;
            }
        }
        if (!hasEntity) return;

        // 3. Entity Çizimi (Gizli Buffer)
        RenderTarget target = electricChain.getTempTarget("final");
        target.clear(Minecraft.ON_OSX);
        target.bindWrite(false);

        PoseStack poseStack = event.getPoseStack();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        for (Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof StitchedEntity stitched && stitched.getState() == 1) {
                double x = stitched.getX() - event.getCamera().getPosition().x;
                double y = stitched.getY() - event.getCamera().getPosition().y;
                double z = stitched.getZ() - event.getCamera().getPosition().z;

                poseStack.pushPose();
                poseStack.translate(x, y, z);
                mc.getEntityRenderDispatcher().render(stitched, 0, 0, 0, 0, event.getPartialTick(), poseStack, mc.renderBuffers().bufferSource(), 15728640);
                poseStack.popPose();
            }
        }
        mc.renderBuffers().bufferSource().endBatch();

        // 4. İşle
        mc.getMainRenderTarget().bindWrite(false);
        electricChain.process(event.getPartialTick());

        // 5. Ekrana Bas (Blit)
        RenderTarget output = electricChain.getTempTarget("final");
        if (output != null) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            output.blitToScreen(mc.getWindow().getWidth(), mc.getWindow().getHeight(), false);
            RenderSystem.enableDepthTest();
        }
    }
}