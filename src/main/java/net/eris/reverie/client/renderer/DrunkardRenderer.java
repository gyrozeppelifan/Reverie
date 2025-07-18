
package net.eris.reverie.client.renderer;

import net.minecraft.world.level.Level;
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

import net.eris.reverie.procedures.DrunkardWalkRemovalProcedure;
import net.eris.reverie.procedures.DrunkardDisplayConditionProcedure;
import net.eris.reverie.procedures.DrunkardBrokenBottleDisplayProcedure;
import net.eris.reverie.procedures.DrunkardBottleDisplayProcedure;
import net.eris.reverie.procedures.DrunkardBaseSkinProcedure;
import net.eris.reverie.procedures.CyclopsBrokenBottleConditionProcedure;
import net.eris.reverie.procedures.CyclopsBottleConditionProcedure;
import net.eris.reverie.entity.DrunkardEntity;
import net.eris.reverie.client.model.animations.DrunkardWalkAnimation;
import net.eris.reverie.client.model.animations.DrunkardDrinkAnimation;
import net.eris.reverie.client.model.animations.DrunkardChargeAnimation;
import net.eris.reverie.client.model.animations.DrunkardAttackAnimation;
import net.eris.reverie.client.model.ModelDrunkard;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

public class DrunkardRenderer extends MobRenderer<DrunkardEntity, ModelDrunkard<DrunkardEntity>> {
	public DrunkardRenderer(EntityRendererProvider.Context context) {
		super(context, new AnimatedModel(context.bakeLayer(ModelDrunkard.LAYER_LOCATION)), 0.5f);
		this.addLayer(new RenderLayer<DrunkardEntity, ModelDrunkard<DrunkardEntity>>(this) {
			final ResourceLocation LAYER_TEXTURE = new ResourceLocation("reverie:textures/entities/drunkard_bottle.png");

			@Override
			public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, DrunkardEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
				Level world = entity.level();
				double x = entity.getX();
				double y = entity.getY();
				double z = entity.getZ();
				if (DrunkardBottleDisplayProcedure.execute(entity)) {
					VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(LAYER_TEXTURE));
					this.getParentModel().renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
				}
			}
		});
		this.addLayer(new RenderLayer<DrunkardEntity, ModelDrunkard<DrunkardEntity>>(this) {
			final ResourceLocation LAYER_TEXTURE = new ResourceLocation("reverie:textures/entities/drunkard_broken_bottle.png");

			@Override
			public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, DrunkardEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
				Level world = entity.level();
				double x = entity.getX();
				double y = entity.getY();
				double z = entity.getZ();
				if (DrunkardBrokenBottleDisplayProcedure.execute(entity)) {
					VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(LAYER_TEXTURE));
					this.getParentModel().renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
				}
			}
		});
		this.addLayer(new RenderLayer<DrunkardEntity, ModelDrunkard<DrunkardEntity>>(this) {
			final ResourceLocation LAYER_TEXTURE = new ResourceLocation("reverie:textures/entities/cyclops.png");

			@Override
			public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, DrunkardEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
				Level world = entity.level();
				double x = entity.getX();
				double y = entity.getY();
				double z = entity.getZ();
				if (DrunkardDisplayConditionProcedure.execute(entity)) {
					VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(LAYER_TEXTURE));
					this.getParentModel().renderToBuffer(poseStack, vertexConsumer, light, LivingEntityRenderer.getOverlayCoords(entity, 0), 1, 1, 1, 1);
				}
			}
		});
		this.addLayer(new RenderLayer<DrunkardEntity, ModelDrunkard<DrunkardEntity>>(this) {
			final ResourceLocation LAYER_TEXTURE = new ResourceLocation("reverie:textures/entities/drunkard.png");

			@Override
			public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, DrunkardEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
				Level world = entity.level();
				double x = entity.getX();
				double y = entity.getY();
				double z = entity.getZ();
				if (DrunkardBaseSkinProcedure.execute(entity)) {
					VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(LAYER_TEXTURE));
					this.getParentModel().renderToBuffer(poseStack, vertexConsumer, light, LivingEntityRenderer.getOverlayCoords(entity, 0), 1, 1, 1, 1);
				}
			}
		});
		this.addLayer(new RenderLayer<DrunkardEntity, ModelDrunkard<DrunkardEntity>>(this) {
			final ResourceLocation LAYER_TEXTURE = new ResourceLocation("reverie:textures/entities/cyclops_bottle.png");

			@Override
			public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, DrunkardEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
				Level world = entity.level();
				double x = entity.getX();
				double y = entity.getY();
				double z = entity.getZ();
				if (CyclopsBottleConditionProcedure.execute(entity)) {
					VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(LAYER_TEXTURE));
					this.getParentModel().renderToBuffer(poseStack, vertexConsumer, light, LivingEntityRenderer.getOverlayCoords(entity, 0), 1, 1, 1, 1);
				}
			}
		});
		this.addLayer(new RenderLayer<DrunkardEntity, ModelDrunkard<DrunkardEntity>>(this) {
			final ResourceLocation LAYER_TEXTURE = new ResourceLocation("reverie:textures/entities/cyclops_broken_bottle.png");

			@Override
			public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, DrunkardEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
				Level world = entity.level();
				double x = entity.getX();
				double y = entity.getY();
				double z = entity.getZ();
				if (CyclopsBrokenBottleConditionProcedure.execute(entity)) {
					VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(LAYER_TEXTURE));
					this.getParentModel().renderToBuffer(poseStack, vertexConsumer, light, LivingEntityRenderer.getOverlayCoords(entity, 0), 1, 1, 1, 1);
				}
			}
		});
	}

	@Override
	public ResourceLocation getTextureLocation(DrunkardEntity entity) {
		return new ResourceLocation("reverie:textures/entities/blank.png");
	}

	private static final class AnimatedModel extends ModelDrunkard<DrunkardEntity> {
		private final ModelPart root;
		private final HierarchicalModel animator = new HierarchicalModel<DrunkardEntity>() {
			@Override
			public ModelPart root() {
				return root;
			}

			@Override
			public void setupAnim(DrunkardEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
				this.root().getAllParts().forEach(ModelPart::resetPose);
				this.animate(entity.animationState0, DrunkardChargeAnimation.charge_anim, ageInTicks, 2f);
				this.animate(entity.animationState1, DrunkardDrinkAnimation.drink_anim, ageInTicks, 1f);
				if (DrunkardWalkRemovalProcedure.execute(entity))
					this.animateWalk(DrunkardWalkAnimation.walk, limbSwing, limbSwingAmount, 4f, 3.4f);
				this.animate(entity.animationState3, DrunkardAttackAnimation.attack, ageInTicks, 1.2f);
			}
		};

		public AnimatedModel(ModelPart root) {
			super(root);
			this.root = root;
		}

		@Override
		public void setupAnim(DrunkardEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
			animator.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		}
	}
}
