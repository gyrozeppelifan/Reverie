package net.eris.reverie.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;

import net.eris.reverie.entity.ShooterGoblinEntity;
import net.eris.reverie.client.model.animations.ShooterGoblinAnimation;
import net.eris.reverie.client.model.Modelshooter_goblin;

import com.mojang.blaze3d.vertex.PoseStack;

public class ShooterGoblinRenderer extends MobRenderer<ShooterGoblinEntity, ShooterGoblinRenderer.AnimatedModel> {
    public ShooterGoblinRenderer(EntityRendererProvider.Context context) {
        super(context, new AnimatedModel(context.bakeLayer(Modelshooter_goblin.LAYER_LOCATION)), 0.5f);

// Mischief Layer - sadece hedefi varsa overlay/üst katman
        this.addLayer(new RenderLayer<ShooterGoblinEntity, AnimatedModel>(this) {
            final ResourceLocation LAYER_TEXTURE = new ResourceLocation("reverie:textures/entities/shooter_goblin_mischief.png");
            @Override
            public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, ShooterGoblinEntity entity,
                               float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                               float netHeadYaw, float headPitch) {
                // sadece hedefi varsa buradan ayarlanacak
                if (!entity.hasTargetClient()) return;
                AnimatedModel model = this.getParentModel();
                model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(LAYER_TEXTURE)), light,
                        LivingEntityRenderer.getOverlayCoords(entity, 0), 1, 1, 1, 1);
            }
        });

// Eyes Layer (sadece hedefi varsa göz efekti additive)
        this.addLayer(new RenderLayer<ShooterGoblinEntity, AnimatedModel>(this) {
            final ResourceLocation LAYER_TEXTURE = new ResourceLocation("reverie:textures/entities/shooter_goblin_eyes.png");
            @Override
            public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, ShooterGoblinEntity entity,
                               float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                               float netHeadYaw, float headPitch) {
                // SADECE HEDEFİ VARSA OYNAT!
                if (!entity.hasTargetClient()) return;
                AnimatedModel model = this.getParentModel();
                model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.eyes(LAYER_TEXTURE)), light,
                        OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
            }
        });


        // Elinde item renderı (aynen GoblinRenderer’daki gibi)
        this.addLayer(new ItemInHandLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(ShooterGoblinEntity entity) {
        return new ResourceLocation("reverie:textures/entities/shooter_goblin.png");
    }

    // Ana model + animasyon altyapısı
    public static final class AnimatedModel extends Modelshooter_goblin<ShooterGoblinEntity> {
        private final ModelPart root;
        private final HierarchicalModel animator = new HierarchicalModel<ShooterGoblinEntity>() {
            @Override
            public ModelPart root() {
                return root;
            }
            @Override
            public void setupAnim(ShooterGoblinEntity entity, float limbSwing, float limbSwingAmount,
                                  float ageInTicks, float netHeadYaw, float headPitch) {
                this.root().getAllParts().forEach(ModelPart::resetPose);
                this.animate(entity.animationState1, ShooterGoblinAnimation.idle, ageInTicks, 0.7f);
                this.animateWalk(ShooterGoblinAnimation.walk, limbSwing, limbSwingAmount, 1f, 1f);
                this.animate(entity.animationState2, ShooterGoblinAnimation.throwing, ageInTicks, 1f);
                this.animate(entity.animationState3, ShooterGoblinAnimation.hold, ageInTicks, 1f);
            }
        };
        public AnimatedModel(ModelPart root) {
            super(root);
            this.root = root;
        }
        @Override
        public void setupAnim(ShooterGoblinEntity entity, float limbSwing, float limbSwingAmount,
                              float ageInTicks, float netHeadYaw, float headPitch) {
            animator.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }

    // Elde item custom render layer (GoblinRenderer’daki pattern)
    private static class ItemInHandLayer extends RenderLayer<ShooterGoblinEntity, AnimatedModel> {
        public ItemInHandLayer(RenderLayerParent<ShooterGoblinEntity, AnimatedModel> renderer) {
            super(renderer);
        }
        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                           ShooterGoblinEntity shooterGoblin, float limbSwing, float limbSwingAmount,
                           float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            ItemStack itemstack = shooterGoblin.getMainHandItem();
            if (itemstack.isEmpty()) return;

            poseStack.pushPose();
            poseStack.pushPose();

            AnimatedModel model = this.getParentModel();
            model.goblin.translateAndRotate(poseStack);
            model.body.translateAndRotate(poseStack);
            model.rightArm.translateAndRotate(poseStack);

            // Pozisyon tweak: Buradan spear/mızrak/ok/item eline tam oturacak şekilde ayar çekebilirsin
            poseStack.translate(-0.08F, 0.50F, -0.05F);
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-97F));
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180F));
            poseStack.scale(0.90F, 0.90F, 0.90F);

            ItemInHandRenderer renderer = net.minecraft.client.Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer();
            renderer.renderItem(shooterGoblin, itemstack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, poseStack, bufferSource, packedLight);

            poseStack.popPose();
            poseStack.popPose();
        }
    }
}
