package net.eris.reverie.client.renderer;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import com.mojang.blaze3d.vertex.PoseStack;

import net.eris.reverie.entity.GoblinEntity;
import net.eris.reverie.client.model.animations.goblinAnimation;
import net.eris.reverie.client.model.Modelgoblin;

public class GoblinRenderer extends MobRenderer<GoblinEntity, GoblinRenderer.AnimatedModel> {
    public GoblinRenderer(EntityRendererProvider.Context context) {
        super(context, new AnimatedModel(context.bakeLayer(Modelgoblin.LAYER_LOCATION)), 0.5f);

        // SADECE TARGET YOKSA: Normal Goblin Render
        this.addLayer(new RenderLayer<GoblinEntity, AnimatedModel>(this) {
            final ResourceLocation LAYER_TEXTURE = new ResourceLocation("reverie:textures/entities/goblin.png");

            @Override
            public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, GoblinEntity entity,
                               float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                               float netHeadYaw, float headPitch) {
                if (entity.hasTargetClient()) return;
                AnimatedModel model = this.getParentModel();
                model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(LAYER_TEXTURE)), light,
    LivingEntityRenderer.getOverlayCoords(entity, 0), 1, 1, 1, 1);
            }
        });

        // SADECE TARGET VARSA: Mischief Texture (üst katman)
        this.addLayer(new RenderLayer<GoblinEntity, AnimatedModel>(this) {
            final ResourceLocation LAYER_TEXTURE = new ResourceLocation("reverie:textures/entities/goblin_mischief.png");

            @Override
            public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, GoblinEntity entity,
                               float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                               float netHeadYaw, float headPitch) {
                if (!entity.hasTargetClient()) return;
                AnimatedModel model = this.getParentModel();
                model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(LAYER_TEXTURE)), light,
    LivingEntityRenderer.getOverlayCoords(entity, 0), 1, 1, 1, 1);
            }
        });

        // SADECE TARGET VARSA: Gözler (overlay, additive)
        this.addLayer(new RenderLayer<GoblinEntity, AnimatedModel>(this) {
            final ResourceLocation LAYER_TEXTURE = new ResourceLocation("reverie:textures/entities/goblin_eyes.png");

            @Override
            public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, GoblinEntity entity,
                               float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                               float netHeadYaw, float headPitch) {
                if (!entity.hasTargetClient()) return;
                AnimatedModel model = this.getParentModel();
                model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.eyes(LAYER_TEXTURE)), light,
                    OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
            }
        });

        // ELDE ITEM LAYERI (her durumda elindeki itemi çizer!)
        this.addLayer(new ItemInHandLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(GoblinEntity entity) {
        return new ResourceLocation("reverie:textures/entities/goblin.png");
    }

    // Animasyonlu ana model
    public static final class AnimatedModel extends Modelgoblin<GoblinEntity> {
        private final ModelPart root;
        private final net.minecraft.client.model.HierarchicalModel animator = new net.minecraft.client.model.HierarchicalModel<GoblinEntity>() {
            @Override
            public ModelPart root() {
                return root;
            }
            @Override
            public void setupAnim(GoblinEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
                this.root().getAllParts().forEach(ModelPart::resetPose);
                this.animateWalk(goblinAnimation.walk, limbSwing, limbSwingAmount, 1f, 1f);
                this.animate(entity.animationState1, goblinAnimation.idle,    ageInTicks, 0.7f);
                this.animate(entity.animationState2, goblinAnimation.attack,  ageInTicks, 0f);
                this.animate(entity.animationState3, goblinAnimation.attack2, ageInTicks, 1f);
            }
        };

        public AnimatedModel(ModelPart root) {
            super(root);
            this.root = root;
        }

        @Override
        public void setupAnim(GoblinEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            animator.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }

    // El itemini custom olarak renderlayan layer (her zaman en üstte olmalı!)
    private static class ItemInHandLayer extends RenderLayer<GoblinEntity, AnimatedModel> {
        public ItemInHandLayer(RenderLayerParent<GoblinEntity, AnimatedModel> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                           GoblinEntity goblinEntity, float limbSwing, float limbSwingAmount,
                           float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            ItemStack itemstack = goblinEntity.getMainHandItem();
            if (itemstack.isEmpty()) return;

            // POZİSYON TWEAK: Buradan ayar çekebilirsin!
            poseStack.pushPose();
            poseStack.pushPose();

            AnimatedModel model = this.getParentModel();
            model.goblin.translateAndRotate(poseStack);
            model.body.translateAndRotate(poseStack);
            model.rightArm.translateAndRotate(poseStack);

            // --- ŞU DEĞERLERİ DENE ---

            // X, Y, Z, yukarı/aşağı/ileri/geri/sağ/sol (kendi modelin için ayarlayabilirsin)
            poseStack.translate(-0.08F, 0.50F, -0.05F);
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-97F));
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180F));
            poseStack.scale(0.90F, 0.90F, 0.90F);

            ItemInHandRenderer renderer = net.minecraft.client.Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer();
            renderer.renderItem(goblinEntity, itemstack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, poseStack, bufferSource, packedLight);

            poseStack.popPose();
            poseStack.popPose();
            
        }
    }
}
