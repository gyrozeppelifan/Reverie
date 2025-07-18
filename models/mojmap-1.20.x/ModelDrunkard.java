// Made with Blockbench 4.12.5
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

public class ModelDrunkard<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			new ResourceLocation("modid", "drunkard"), "main");
	private final ModelPart Drunkard;
	private final ModelPart body;
	private final ModelPart bone;
	private final ModelPart right_arm;
	private final ModelPart bottle;
	private final ModelPart head;
	private final ModelPart nose;
	private final ModelPart left_leg;
	private final ModelPart right_leg;

	public ModelDrunkard(ModelPart root) {
		this.Drunkard = root.getChild("Drunkard");
		this.body = this.Drunkard.getChild("body");
		this.bone = this.body.getChild("bone");
		this.right_arm = this.body.getChild("right_arm");
		this.bottle = this.right_arm.getChild("bottle");
		this.head = this.body.getChild("head");
		this.nose = this.head.getChild("nose");
		this.left_leg = this.Drunkard.getChild("left_leg");
		this.right_leg = this.Drunkard.getChild("right_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Drunkard = partdefinition.addOrReplaceChild("Drunkard", CubeListBuilder.create(),
				PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition body = Drunkard.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(32, 20)
						.addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(9, 86)
						.addBox(-4.0F, -12.0F, -2.0F, 8.0F, 16.0F, 4.0F, new CubeDeformation(0.6F)),
				PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition bone = body.addOrReplaceChild("bone",
				CubeListBuilder.create().texOffs(32, 51).mirror()
						.addBox(0.0F, -1.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(4.0F, -11.0F, 0.0F));

		PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 51)
				.addBox(-4.0F, -1.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-4.0F, -11.0F, 0.0F));

		PartDefinition bottle = right_arm.addOrReplaceChild("bottle",
				CubeListBuilder.create().texOffs(0, 53)
						.addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(32, 36)
						.addBox(-3.0F, -9.0F, -3.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.0F, 9.0F, -3.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(48, 51)
						.addBox(-3.0F, -10.0F, 5.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-6.0F, -10.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)).texOffs(0, 20)
						.addBox(-5.0F, -9.0F, -4.0F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(1.0F, -12.0F, 0.0F));

		PartDefinition nose = head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(0, 74).addBox(-1.0F, 0.0F,
				-2.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -3.0F, -4.0F));

		PartDefinition left_leg = Drunkard.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 37).addBox(
				0.0F, -1.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, 0.0F));

		PartDefinition right_leg = Drunkard.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(16, 37)
				.addBox(-3.0F, -1.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-1.0F, 5.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		Drunkard.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
		this.head.yRot = netHeadYaw / (180F / (float) Math.PI);
		this.head.xRot = headPitch / (180F / (float) Math.PI);
	}
}