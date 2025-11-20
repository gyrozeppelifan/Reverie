package net.eris.reverie.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.eris.reverie.entity.PossessionPuppetEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class PossessionPuppetRenderer extends EntityRenderer<PossessionPuppetEntity> {
    public PossessionPuppetRenderer(EntityRendererProvider.Context ctx) { super(ctx); }
    @Override public void render(PossessionPuppetEntity e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int light) {
        // deliberately empty
    }
    @Override public ResourceLocation getTextureLocation(PossessionPuppetEntity e) {
        return new ResourceLocation("minecraft","textures/misc/white.png"); // kullanılmıyor
    }
}
