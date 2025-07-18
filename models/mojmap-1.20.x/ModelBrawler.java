// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

public class ModelBrawler<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			new ResourceLocation("modid", "brawler"), "main");
	private final ModelPart Brawler;
	private final ModelPart Body;
	private final ModelPart head;
	private final ModelPart left_ear;
	private final ModelPart right_ear;
	private final ModelPart hair;
	private final ModelPart left_arm_up;
	private final ModelPart upleftforearm;
	private final ModelPart left_arm_botttom;
	private final ModelPart bottomleftforearm;
	private final ModelPart right_arm_top;
	private final ModelPart uprightforearm;
	private final ModelPart rightt_arm_bottom;
	private final ModelPart bottomrightforearm;
	private final ModelPart right_leg;
	private final ModelPart left_leg;

	public ModelBrawler(ModelPart root) {
		this.Brawler = root.getChild("Brawler");
		this.Body = this.Brawler.getChild("Body");
		this.head = this.Body.getChild("head");
		this.left_ear = this.head.getChild("left_ear");
		this.right_ear = this.head.getChild("right_ear");
		this.hair = this.head.getChild("hair");
		this.left_arm_up = this.Body.getChild("left_arm_up");
		this.upleftforearm = this.left_arm_up.getChild("upleftforearm");
		this.left_arm_botttom = this.Body.getChild("left_arm_botttom");
		this.bottomleftforearm = this.left_arm_botttom.getChild("bottomleftforearm");
		this.right_arm_top = this.Body.getChild("right_arm_top");
		this.uprightforearm = this.right_arm_top.getChild("uprightforearm");
		this.rightt_arm_bottom = this.Body.getChild("rightt_arm_bottom");
		this.bottomrightforearm = this.rightt_arm_bottom.getChild("bottomrightforearm");
		this.right_leg = this.Brawler.getChild("right_leg");
		this.left_leg = this.Brawler.getChild("left_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Brawler = partdefinition.addOrReplaceChild("Brawler", CubeListBuilder.create(),
				PartPose.offset(0.0F, 2.0F, -1.0F));

		PartDefinition Body = Brawler.addOrReplaceChild("Body",
				CubeListBuilder.create().texOffs(0, 0)
						.addBox(-7.0F, -19.0F, -4.0F, 14.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(44, 0)
						.addBox(-5.0F, 0.0F, -4.0F, 10.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(0, 20)
						.addBox(-7.0F, -19.0F, -4.0F, 14.0F, 12.0F, 8.0F, new CubeDeformation(0.6F)).texOffs(44, 18)
						.addBox(-5.0F, -7.0F, -4.0F, 10.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(11, 108)
						.addBox(-5.0F, -7.0F, -4.0F, 10.0F, 7.0F, 8.0F, new CubeDeformation(0.5F)),
				PartPose.offset(0.0F, 5.0F, 1.0F));

		PartDefinition head = Body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(48, 33).addBox(-4.0F,
				-10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -19.0F, -1.0F));

		PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(79, 114).addBox(
				0.0F, -3.0F, 0.0F, 6.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -5.0F, 0.0F));

		PartDefinition right_ear = head.addOrReplaceChild("right_ear",
				CubeListBuilder.create().texOffs(79, 114).mirror()
						.addBox(-6.0F, -3.0F, 0.0F, 6.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(-4.0F, -5.0F, 0.0F));

		PartDefinition hair = head.addOrReplaceChild("hair", CubeListBuilder.create().texOffs(91, 102).addBox(-5.0F,
				0.0F, 3.0F, 10.0F, 0.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -10.0F, -3.0F));

		PartDefinition left_arm_up = Body.addOrReplaceChild("left_arm_up",
				CubeListBuilder.create().texOffs(54, 83).mirror()
						.addBox(0.0F, -3.0F, -3.0F, 8.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(7.0F, -16.0F, 0.0F));

		PartDefinition upleftforearm = left_arm_up.addOrReplaceChild("upleftforearm", CubeListBuilder.create()
				.texOffs(72, 61).addBox(0.0F, -2.0F, -3.0F, 9.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(8.0F, -1.0F, 0.0F));

		PartDefinition left_arm_botttom = Body.addOrReplaceChild("left_arm_botttom",
				CubeListBuilder.create().texOffs(54, 83).mirror()
						.addBox(0.0F, -3.0F, -3.0F, 8.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(7.0F, -9.0F, 0.0F));

		PartDefinition bottomleftforearm = left_arm_botttom.addOrReplaceChild("bottomleftforearm", CubeListBuilder
				.create().texOffs(72, 61).addBox(0.0F, -2.0F, -3.0F, 9.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(8.0F, -1.0F, 0.0F));

		PartDefinition right_arm_top = Body.addOrReplaceChild("right_arm_top", CubeListBuilder.create().texOffs(54, 83)
				.addBox(-8.0F, -2.0F, -3.0F, 8.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-7.0F, -17.0F, 0.0F));

		PartDefinition uprightforearm = right_arm_top.addOrReplaceChild("uprightforearm",
				CubeListBuilder.create().texOffs(72, 61).mirror()
						.addBox(-9.0F, -3.0F, -3.0F, 9.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(-8.0F, 1.0F, 0.0F));

		PartDefinition rightt_arm_bottom = Body.addOrReplaceChild("rightt_arm_bottom", CubeListBuilder.create()
				.texOffs(54, 83).addBox(-8.0F, -2.0F, -3.0F, 8.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-7.0F, -10.0F, 0.0F));

		PartDefinition bottomrightforearm = rightt_arm_bottom.addOrReplaceChild("bottomrightforearm",
				CubeListBuilder.create().texOffs(72, 61).mirror()
						.addBox(-9.0F, -3.0F, -3.0F, 9.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(-8.0F, 1.0F, 0.0F));

		PartDefinition right_leg = Brawler.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(48, 51)
				.addBox(-3.0F, -1.0F, -3.0F, 6.0F, 17.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-4.0F, 6.0F, 1.0F));

		PartDefinition left_leg = Brawler.addOrReplaceChild("left_leg",
				CubeListBuilder.create().texOffs(48, 51).mirror()
						.addBox(-3.0F, -1.0F, -3.0F, 6.0F, 17.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(4.0F, 6.0F, 1.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		Brawler.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}