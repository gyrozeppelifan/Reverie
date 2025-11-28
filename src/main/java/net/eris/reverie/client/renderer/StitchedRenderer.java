package net.eris.reverie.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.ReverieClientEvents;
import net.eris.reverie.client.model.Stitched;
import net.eris.reverie.client.model.animations.StitchedAnimation;
import net.eris.reverie.entity.StitchedEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class StitchedRenderer extends MobRenderer<StitchedEntity, Stitched<StitchedEntity>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(ReverieMod.MODID, "textures/entities/stitched.png");
    private static final ResourceLocation TEXTURE_SKELETON = new ResourceLocation(ReverieMod.MODID, "textures/entities/stitched_skeleton.png");

    public StitchedRenderer(EntityRendererProvider.Context context) {
        super(context, new AnimatedModel(context.bakeLayer(Stitched.LAYER_LOCATION)), 0.5f);

        // --- OUTLINE LAYER (Inverted Hull & Flash) ---
        this.addLayer(new RenderLayer<StitchedEntity, Stitched<StitchedEntity>>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, StitchedEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                if (entity.getState() == 1) {
                    if (ReverieClientEvents.stitchedFlashShader != null) {
                        ReverieClientEvents.stitchedFlashShader.safeGetUniform("FlashColor").set(0.0F, 1.0F, 1.0F, 1.0F);
                    }

                    RenderType flashType = ReverieRenderTypes.getStitchedFlash(getTextureLocation(entity));
                    VertexConsumer vertexConsumer = buffer.getBuffer(flashType);

                    this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
                }
            }
        });
    }

    // --- YENİ: KARANLIKTA PARLAMA ---
    @Override
    protected int getBlockLightLevel(StitchedEntity entity, BlockPos pos) {
        // Eğer çarpılıyorsa (State 1) her zaman maksimum ışıkta (15) renderla.
        // Bu sayede gece veya karanlık mağarada bile parlar.
        if (entity.getState() == 1) {
            return 15;
        }
        return super.getBlockLightLevel(entity, pos);
    }

    @Override
    public void render(StitchedEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(StitchedEntity entity) {
        if (entity.getState() == 1) {
            if (entity.tickCount % 2 == 0) {
                return TEXTURE_SKELETON;
            }
        }
        return TEXTURE;
    }

    private static final class AnimatedModel extends Stitched<StitchedEntity> {
        private final ModelPart root;
        private final HierarchicalModel<StitchedEntity> animator = new HierarchicalModel<StitchedEntity>() {
            @Override public ModelPart root() { return root; }
            @Override public void setupAnim(StitchedEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
                this.root().getAllParts().forEach(ModelPart::resetPose);

                if (entity.getState() == 0 || entity.getState() == 4) {
                    this.animate(entity.passiveState, StitchedAnimation.passive, ageInTicks, 1f);
                } else {
                    this.animate(entity.passiveState, StitchedAnimation.passive, ageInTicks, 1f);
                }

                this.animate(entity.electrocutedState, StitchedAnimation.electrocuted, ageInTicks, 1.4f);
                this.animate(entity.standupState, StitchedAnimation.standup, ageInTicks, 0.7f);
            }
        };
        public AnimatedModel(ModelPart root) { super(root); this.root = root; }
        @Override public void setupAnim(StitchedEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            animator.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }
}