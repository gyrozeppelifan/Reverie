package net.eris.reverie.client.model;// Made with Blockbench 5.0.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class Stitched<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "stitched"), "main");
	private final ModelPart creature;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart mouth;
	private final ModelPart right_arm;
	private final ModelPart left_arm;
	private final ModelPart right_leg;
	private final ModelPart left_leg;

	public Stitched(ModelPart root) {
		this.creature = root.getChild("creature");
		this.body = this.creature.getChild("body");
		this.head = this.body.getChild("head");
		this.mouth = this.head.getChild("mouth");
		this.right_arm = this.body.getChild("right_arm");
		this.left_arm = this.body.getChild("left_arm");
		this.right_leg = this.creature.getChild("right_leg");
		this.left_leg = this.creature.getChild("left_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition creature = partdefinition.addOrReplaceChild("creature", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, -5.0F));

		PartDefinition body = creature.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -26.0F, -9.0F, 24.0F, 22.0F, 18.0F, new CubeDeformation(0.0F))
		.texOffs(0, 40).addBox(-10.0F, -4.0F, -8.0F, 20.0F, 4.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 9.0F, 6.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(80, 40).addBox(-5.0F, -13.0F, -4.0F, 10.0F, 13.0F, 10.0F, new CubeDeformation(0.4F))
		.texOffs(96, 34).addBox(5.0F, -7.0F, 1.0F, 5.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(96, 34).mirror().addBox(-10.0F, -7.0F, 1.0F, 5.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(80, 63).addBox(-5.0F, -13.0F, -4.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(84, 34).addBox(-2.0F, -5.0F, -6.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -26.0F, -8.0F));

		PartDefinition mouth = head.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(80, 83).addBox(-5.0F, 0.0F, -10.0F, 10.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(84, 0).addBox(-5.0F, -0.775F, -9.85F, 10.0F, 3.0F, 10.0F, new CubeDeformation(0.4F))
		.texOffs(28, 96).addBox(5.0F, 1.0F, -6.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(28, 96).mirror().addBox(-8.0F, 1.0F, -6.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -3.0F, 6.0F));

		PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 60).addBox(-9.0F, -5.0F, -4.0F, 10.0F, 26.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(-13.0F, -21.0F, -3.0F));

		PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 60).addBox(0.0F, -4.0F, -5.0F, 10.0F, 26.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(12.0F, -22.0F, -2.0F));

		PartDefinition right_leg = creature.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(84, 13).addBox(-4.0F, -1.0F, -4.0F, 7.0F, 14.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, 10.0F, 6.0F));

		PartDefinition left_leg = creature.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 96).addBox(-4.0F, -1.0F, -4.0F, 7.0F, 14.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, 10.0F, 6.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		creature.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}