// src/main/java/net/eris/reverie/client/renderer/ElderOliveHeartBlockRenderer.java
package net.eris.reverie.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.state.BlockState;
import net.eris.reverie.block.entity.ElderOliveHeartBlockEntity;
import net.eris.reverie.init.ReverieModBlockEntities;

public class ElderOliveHeartBlockRenderer implements BlockEntityRenderer<ElderOliveHeartBlockEntity> {
    private static final float AMPLITUDE = 0.1f;

    public ElderOliveHeartBlockRenderer(BlockEntityRendererProvider.Context ctx) { }

    @Override
    public void render(ElderOliveHeartBlockEntity be, float pt, PoseStack ms,
                       MultiBufferSource buf, int light, int overlay) {
        BlockState state = be.getBlockState();

        // render the block fullbright if un–initialized, otherwise normal
        int usedLight = (be.getLevel() == null) ? 0xF000F0 : light;
        Minecraft.getInstance()
                .getBlockRenderer()
                .renderSingleBlock(state, ms, buf, usedLight, overlay);

        // grab the *current* interval (fast or normal)
        int interval = be.getCurrentPulseInterval();

        // drive animation off the client’s gameTime, not the server counter
        long gameTime = Minecraft.getInstance().level.getGameTime();
        float t = (gameTime % interval) + pt;
        float norm = t / interval;
        float scale = 1.0f + AMPLITUDE * (float)Math.cos(norm * Math.PI * 2);

        ms.pushPose();
        ms.translate(0.5D, 0.5D, 0.5D);
        ms.scale(scale, scale, scale);
        ms.translate(-0.5D, -0.5D, -0.5D);

        // render the scaled model
        Minecraft.getInstance()
                .getBlockRenderer()
                .renderSingleBlock(state, ms, buf, light, overlay);
        ms.popPose();
    }

    public static void register() {
        BlockEntityRenderers.register(
                ReverieModBlockEntities.ELDER_OLIVE_HEART.get(),
                ElderOliveHeartBlockRenderer::new
        );
    }
}
