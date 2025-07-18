// Made with Blockbench 4.12.5
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

public class Modelspiked_log<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			new ResourceLocation("modid", "spiked_log"), "main");
	private final ModelPart log;

	public Modelspiked_log(ModelPart root) {
		this.log = root.getChild("log");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition log = partdefinition.addOrReplaceChild("log",
				CubeListBuilder.create().texOffs(0, 0)
						.addBox(-22.0F, -6.0F, -6.0F, 44.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)).texOffs(0, 24)
						.addBox(-21.0F, -5.0F, -5.0F, 42.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)).texOffs(42, 44)
						.addBox(15.0F, -9.0F, -2.0F, 3.0F, 18.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(42, 44)
						.addBox(-18.0F, -9.0F, -2.0F, 3.0F, 18.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(42, 44)
						.addBox(-1.0F, -9.0F, -2.0F, 3.0F, 18.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 44)
						.addBox(-18.0F, -2.0F, -9.0F, 3.0F, 3.0F, 18.0F, new CubeDeformation(0.0F)).texOffs(0, 44)
						.addBox(-1.0F, -2.0F, -9.0F, 3.0F, 3.0F, 18.0F, new CubeDeformation(0.0F)).texOffs(0, 44)
						.addBox(15.0F, -2.0F, -9.0F, 3.0F, 3.0F, 18.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 18.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		log.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
	}
}