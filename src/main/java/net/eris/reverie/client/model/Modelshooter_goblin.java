package net.eris.reverie.client.model;

import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.EntityModel;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

// Made with Blockbench 4.12.5
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports
public class Modelshooter_goblin<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("reverie", "modelshooter_goblin"), "main");
	public final ModelPart goblin;
	public final ModelPart leftLeg;
	public final ModelPart rightLeg;
	public final ModelPart body;
	public final ModelPart keg;
	public final ModelPart rightArm;
	public final ModelPart leftArm;
	public final ModelPart head;
	public final ModelPart left_ear;
	public final ModelPart right_ear;

	public Modelshooter_goblin(ModelPart root) {
		this.goblin = root.getChild("goblin");
		this.leftLeg = this.goblin.getChild("leftLeg");
		this.rightLeg = this.goblin.getChild("rightLeg");
		this.body = this.goblin.getChild("body");
		this.keg = this.body.getChild("keg");
		this.rightArm = this.body.getChild("rightArm");
		this.leftArm = this.body.getChild("leftArm");
		this.head = this.body.getChild("head");
		this.left_ear = this.head.getChild("left_ear");
		this.right_ear = this.head.getChild("right_ear");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition goblin = partdefinition.addOrReplaceChild("goblin", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
		PartDefinition leftLeg = goblin.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(26, 30).addBox(-1.0F, 0.0F, -2.0F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -8.0F, 2.0F));
		PartDefinition rightLeg = goblin.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(26, 30).mirror().addBox(-2.0F, 0.0F, -2.0F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.0F, -8.0F, 2.0F));
		PartDefinition body = goblin.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(0, 17).addBox(-4.0F, -9.0F, -2.0F, 8.0F, 11.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(0, 63).addBox(-4.0F, -9.0F, -2.0F, 8.0F, 11.0F, 5.0F, new CubeDeformation(0.5F)),
				PartPose.offset(0.0F, -10.0F, 1.0F));
		PartDefinition keg = body.addOrReplaceChild("keg", CubeListBuilder.create().texOffs(39, 41).addBox(-3.0F, -6.0F, -2.5F, 6.0F, 11.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 5.9F));
		PartDefinition rightArm = body.addOrReplaceChild("rightArm",
				CubeListBuilder.create().texOffs(26, 17).addBox(-3.0F, -2.0F, -1.0F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(40, 17).addBox(-3.0F, -2.0F, -1.0F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.4F)),
				PartPose.offset(-4.0F, -7.0F, 0.0F));
		PartDefinition leftArm = body.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(26, 17).mirror().addBox(0.0F, -2.0F, -1.0F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(4.0F, -7.0F, 0.0F));
		PartDefinition head = body.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -9.0F, -4.0F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(0, 46).addBox(-4.0F, -9.0F, -4.0F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.5F)).texOffs(32, 7)
						.addBox(-1.0F, -6.0F, -5.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 27).addBox(1.0F, -9.0F, 4.0F, 0.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -9.0F, -1.0F));
		PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(32, 1).mirror().addBox(0.0F, -2.0F, 0.0F, 7.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(4.0F, -5.0F, 0.0F));
		PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(32, 1).addBox(-7.0F, -2.0F, 0.0F, 7.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -5.0F, 0.0F));
		return LayerDefinition.create(meshdefinition, 64, 128);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		goblin.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.head.yRot = netHeadYaw / (180F / (float) Math.PI);
		this.head.xRot = headPitch / (180F / (float) Math.PI);
	}
}
