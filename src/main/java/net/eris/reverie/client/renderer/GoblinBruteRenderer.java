package net.eris.reverie.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.eris.reverie.client.model.ModelGoblinBrute;
import net.eris.reverie.client.model.animations.GoblinBruteAnimation;
import net.eris.reverie.entity.GoblinBruteEntity;

public class GoblinBruteRenderer extends MobRenderer<GoblinBruteEntity, ModelGoblinBrute<GoblinBruteEntity>> {
	private static final ResourceLocation TEXTURE_NORMAL = new ResourceLocation("reverie:textures/entities/goblin_brute_normal.png");
	private static final ResourceLocation TEXTURE_ANGRY  = new ResourceLocation("reverie:textures/entities/goblin_brute_angry.png");
	private static final ResourceLocation TEXTURE_CRYING = new ResourceLocation("reverie:textures/entities/goblin_brute_crying.png");
	private static final ResourceLocation TEXTURE_EYES   = new ResourceLocation("reverie:textures/entities/goblin_brute_angry_eyes.png");

	public GoblinBruteRenderer(EntityRendererProvider.Context context) {
		super(context,
				new AnimatedModel(context.bakeLayer(ModelGoblinBrute.LAYER_LOCATION)),
				0.5f);

		// --- ANGRY HALDE PARLAK GÖZ KATMANI ---
		this.addLayer(new RenderLayer<GoblinBruteEntity, ModelGoblinBrute<GoblinBruteEntity>>(this) {
			@Override
			public void render(PoseStack poseStack,
							   MultiBufferSource bufferSource,
							   int packedLight,
							   GoblinBruteEntity entity,
							   float limbSwing,
							   float limbSwingAmount,
							   float partialTicks,
							   float ageInTicks,
							   float netHeadYaw,
							   float headPitch) {
				// Sadece ANGRY durumunda çalışsın
				if (entity.getState() != GoblinBruteEntity.BruteState.ANGRY
						&& entity.getState() != GoblinBruteEntity.BruteState.ROARING
						&& entity.getState() != GoblinBruteEntity.BruteState.CHARGING) return;


				// fullbright glow-eyes efekti
				var vb = bufferSource.getBuffer(RenderType.eyes(TEXTURE_EYES));
				int overlay = OverlayTexture.NO_OVERLAY;

				// Model’i alıp renderToBuffer ile göz katmanını çiz
				ModelGoblinBrute<GoblinBruteEntity> model = this.getParentModel();
				model.renderToBuffer(poseStack, vb, packedLight, overlay,
						1f, 1f, 1f, 1f);
			}
		});
	}

	@Override
	public ResourceLocation getTextureLocation(GoblinBruteEntity entity) {
		return switch (entity.getState()) {
			case CRYING    -> TEXTURE_CRYING;
			case ROARING, CHARGING, ANGRY -> TEXTURE_ANGRY;
			default        -> TEXTURE_NORMAL;
		};
	}

	private static final class AnimatedModel extends ModelGoblinBrute<GoblinBruteEntity> {
		private final ModelPart root;
		private final net.minecraft.client.model.HierarchicalModel<GoblinBruteEntity> animator;

		public AnimatedModel(ModelPart root) {
			super(root);
			this.root = root;
			this.animator = new net.minecraft.client.model.HierarchicalModel<>() {
				@Override
				public ModelPart root() {
					return root;
				}

				@Override
				public void setupAnim(GoblinBruteEntity entity,
									  float limbSwing,
									  float limbSwingAmount,
									  float ageInTicks,
									  float netHeadYaw,
									  float headPitch) {
					// 1) Reset all poses
					root().getAllParts().forEach(ModelPart::resetPose);

					// 2) Idle animasyonunu hep çalıştır
					this.animate(entity.animationState0, GoblinBruteAnimation.idle, ageInTicks, 1f);

					// 3) Diğer durumların animasyonları
					switch (entity.getState()) {
						case IDLE, SEEK_PLAYER, SEEK_SHOOTER, CARRY_SHOOTER ->
								this.animateWalk(GoblinBruteAnimation.walk, limbSwing, limbSwingAmount, 1f, 1f);
						case CHARGING ->
								this.animateWalk(GoblinBruteAnimation.charge, limbSwing, limbSwingAmount, 1.3f, 1.2f);
						case ANGRY ->
								this.animateWalk(GoblinBruteAnimation.walk, limbSwing, limbSwingAmount, 1.3f, 1.2f);
						case CARRY_PLAYER -> {
							this.animateWalk(GoblinBruteAnimation.holdwalk, limbSwing, limbSwingAmount, 1f, 1f);
							this.animate(entity.animationState6, GoblinBruteAnimation.hold, ageInTicks, 1f);
						}
						default -> {}
					}

					if (entity.getState() == GoblinBruteEntity.BruteState.CRYING) {
						this.animate(entity.animationState2, GoblinBruteAnimation.cry, ageInTicks, 0.8f);
					} else if (entity.getState() == GoblinBruteEntity.BruteState.ROARING) {
						this.animate(entity.animationState3, GoblinBruteAnimation.angry, ageInTicks, 1.5f);
					}

					// 4) Attack overlay (en sona)
					if (entity.getAttackTimer() > 0) {
						this.animate(entity.animationState5, GoblinBruteAnimation.angryattack, ageInTicks, 1.8f);
					}
				}
			};
		}

		@Override
		public void setupAnim(GoblinBruteEntity entity,
							  float limbSwing,
							  float limbSwingAmount,
							  float ageInTicks,
							  float netHeadYaw,
							  float headPitch) {
			animator.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		}
	}
}
