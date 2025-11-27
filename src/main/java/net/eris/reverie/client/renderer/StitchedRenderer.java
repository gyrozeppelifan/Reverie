package net.eris.reverie.client.renderer;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.model.Stitched; // Model dosyanız
import net.eris.reverie.client.model.animations.StitchedAnimation; // Animasyon dosyanız
import net.eris.reverie.entity.StitchedEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class StitchedRenderer extends MobRenderer<StitchedEntity, Stitched<StitchedEntity>> {

    // Normal Doku
    private static final ResourceLocation TEXTURE = new ResourceLocation(ReverieMod.MODID, "textures/entities/stitched.png");
    // İskelet/Elektrikli Doku (Senin yüklediğin kemikli hali)
    private static final ResourceLocation TEXTURE_SKELETON = new ResourceLocation(ReverieMod.MODID, "textures/entities/stitched_skeleton.png");

    public StitchedRenderer(EntityRendererProvider.Context context) {
        super(context, new Stitched<>(context.bakeLayer(Stitched.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(StitchedEntity entity) {
        // Eğer çarpılıyorsa (State 1) veya ayağa kalkıyorsa (State 2) titreme efekti yap
        if (entity.getState() == 1 || entity.getState() == 2) {
            // Her 2 tick'te bir doku değiştir (Hızlı Flicker Efekti)
            if (entity.tickCount % 2 == 0) {
                return TEXTURE_SKELETON;
            }
        }
        return TEXTURE;
    }

    @Override
    protected void setupRotations(StitchedEntity entity, com.mojang.blaze3d.vertex.PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);
    }
}