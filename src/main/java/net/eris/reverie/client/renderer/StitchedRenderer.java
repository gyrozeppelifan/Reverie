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
import net.minecraft.world.item.Items;

public class StitchedRenderer extends MobRenderer<StitchedEntity, Stitched<StitchedEntity>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(ReverieMod.MODID, "textures/entities/stitched.png");
    private static final ResourceLocation TEXTURE_SKELETON = new ResourceLocation(ReverieMod.MODID, "textures/entities/stitched_skeleton.png");

    public StitchedRenderer(EntityRendererProvider.Context context) {
        super(context, new AnimatedModel(context.bakeLayer(Stitched.LAYER_LOCATION)), 0.5f);

        // --- OUTLINE LAYER ---
        this.addLayer(new RenderLayer<StitchedEntity, Stitched<StitchedEntity>>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, StitchedEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                if (entity.getState() == 1) {
                    if (ReverieClientEvents.stitchedFlashShader != null) {
                        ReverieClientEvents.stitchedFlashShader.safeGetUniform("FlashColor").set(0.0F, 1.0F, 1.0F, 1.0F);
                    }
                    RenderType flashType = ReverieRenderTypes.getStitchedFlash(getTextureLocation(entity));
                    VertexConsumer vertexConsumer = buffer.getBuffer(flashType);
                    this.getParentModel().setupVisibility(entity.getHeadItem().is(Items.LIGHTNING_ROD));
                    this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
                }
            }
        });
    }

    @Override
    public void render(StitchedEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Stitched<StitchedEntity> model = this.getModel();
        model.setupVisibility(entity.getHeadItem().is(Items.LIGHTNING_ROD));
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected int getBlockLightLevel(StitchedEntity entity, BlockPos pos) {
        if (entity.getState() == 1) return 15;
        return super.getBlockLightLevel(entity, pos);
    }

    @Override
    public ResourceLocation getTextureLocation(StitchedEntity entity) {
        if (entity.getState() == 1 && entity.tickCount % 2 == 0) {
            return TEXTURE_SKELETON; // İstersen burada yanıp sönme efekti de yapabilirsin
        }
        return TEXTURE;
    }

    // --- YENİ ANİMASYON YÖNETİCİSİ ---
    private static final class AnimatedModel extends Stitched<StitchedEntity> {
        private final ModelPart root;
        private final HierarchicalModel<StitchedEntity> animator = new HierarchicalModel<StitchedEntity>() {
            @Override public ModelPart root() { return root; }
            @Override public void setupAnim(StitchedEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
                this.root().getAllParts().forEach(ModelPart::resetPose);

                // 1. IDLE (Ayakta Durma)
                this.animate(entity.idleState, StitchedAnimation.idle, ageInTicks, 1f);

                // 2. PASSIVE (Yerde Yatış)
                // Eğer 'sitroaring' varsa passive yerine onu oynatabilirsin
                this.animate(entity.passiveState, StitchedAnimation.idle, ageInTicks, 1f); // Şimdilik idle verdik, yatış animasyonu varsa değiştir.

                // 3. ELECTRICITY (Çarpılma)
                this.animate(entity.electrocutedState, StitchedAnimation.electricity, ageInTicks, 1.4f);

                // 4. STANDUP (Ayağa Kalkma)
                this.animate(entity.standupState, StitchedAnimation.standup, ageInTicks, 0.7f);

                // 5. WALKING (Yürüme)
                // Burada normal yürüyüş veya kolsuz yürüyüş seçimi yapılabilir
                this.animate(entity.walkState, StitchedAnimation.walking, ageInTicks, 1.0f);
                this.animate(entity.walkNoArmsState, StitchedAnimation.walkingwithoutarms, ageInTicks, 1.0f);

                // 6. AKSİYONLAR
                this.animate(entity.attackState, StitchedAnimation.attackingbase, ageInTicks, 1.0f);
                this.animate(entity.roaringState, StitchedAnimation.roaring, ageInTicks, 1.0f);
                this.animate(entity.sitRoaringState, StitchedAnimation.sitroaring, ageInTicks, 1.0f);
            }
        };

        public AnimatedModel(ModelPart root) {
            super(root);
            this.root = root;
        }

        @Override
        public void setupAnim(StitchedEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            animator.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }
}