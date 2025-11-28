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

    // --- DEBUG MODU ---
    // Bunu 'true' yaparsan direkt çizilen maskeyi görürsün (Beyaz/Renkli entity).
    // Eğer bunu görünce entity varsa, shader kodunda sorun var demektir.
    // Entity yoksa, çizim kodunda sorun var demektir.
    private static final boolean DEBUG_SHOW_MASK = true;

    @SubscribeEvent
    public static void renderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (StitchedRenderer.electricEntitiesOnScreen.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // 1. Zinciri Yükle
        if (electricChain == null) {
            try {
                electricChain = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), POST_CHAIN_LOCATION);
                electricChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            } catch (IOException | JsonSyntaxException e) {
                return;
            }
        }

        if (electricChain.getTempTarget("final").width != mc.getWindow().getWidth()) {
            electricChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
        }

        // 2. Hazırlık: Maske Bufferını Ayarla
        RenderTarget target = electricChain.getTempTarget("reverie:entity_mask");
        if (target == null) return;

        // Derinlik testini temizlemiyoruz, sadece rengi temizliyoruz (şeffaf yapıyoruz)
        target.clear(Minecraft.ON_OSX);
        target.setClearColor(0F, 0F, 0F, 0F);
        target.bindWrite(false);

        // 3. Çizim Başlıyor
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        Vec3 cameraPos = event.getCamera().getPosition();
        MultiBufferSource.BufferSource immediateBuffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        List<StitchedEntity> entitiesToRender = new ArrayList<>(StitchedRenderer.electricEntitiesOnScreen);

        // Duvar arkasından görmek için Depth Test KAPALI
        RenderSystem.disableDepthTest();

        for (StitchedEntity entity : entitiesToRender) {
            EntityRenderer<? super StitchedEntity> renderer = mc.getEntityRenderDispatcher().getRenderer(entity);
            if (renderer instanceof StitchedRenderer stitchedRenderer) {
                double x = Mth.lerp(event.getPartialTick(), entity.xo, entity.getX()) - cameraPos.x;
                double y = Mth.lerp(event.getPartialTick(), entity.yo, entity.getY()) - cameraPos.y;
                double z = Mth.lerp(event.getPartialTick(), entity.zo, entity.getZ()) - cameraPos.z;

                poseStack.pushPose();
                poseStack.translate(x, y, z);

                float bodyRot = Mth.rotLerp(event.getPartialTick(), entity.yBodyRotO, entity.yBodyRot);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyRot));

                stitchedRenderer.renderModelDirectly(entity, event.getPartialTick(), poseStack, immediateBuffer, 15728640);

                poseStack.popPose();
            }
        }

        immediateBuffer.endBatch();
        RenderSystem.enableDepthTest();
        poseStack.popPose();
        StitchedRenderer.electricEntitiesOnScreen.clear();

        // 4. İşle
        mc.getMainRenderTarget().bindWrite(false);
        electricChain.process(event.getPartialTick());

        // 5. Ekrana Bas (DEBUG KONTROLÜ)
        // Eğer DEBUG_SHOW_MASK true ise, direkt maskeyi basar. Entity'i düz olarak görmen lazım.
        // False ise shaderlı sonucu (final) basar.
        RenderTarget output = DEBUG_SHOW_MASK ? electricChain.getTempTarget("reverie:entity_mask") : electricChain.getTempTarget("final");

        if (output != null) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            output.blitToScreen(mc.getWindow().getWidth(), mc.getWindow().getHeight(), false);
            RenderSystem.enableDepthTest();
        }
    }
}