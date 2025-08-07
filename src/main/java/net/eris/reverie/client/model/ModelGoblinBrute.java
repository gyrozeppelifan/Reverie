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
public class ModelGoblinBrute<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("reverie", "model_goblin_brute"), "main");
	public final ModelPart goblin_brute;
	public final ModelPart body;
	public final ModelPart holdinglocation;
	public final ModelPart left_arm;
	public final ModelPart right_arm;
	public final ModelPart head;
	public final ModelPart angry_face;
	public final ModelPart sad_head;
	public final ModelPart left_leg;
	public final ModelPart right_leg;

	public ModelGoblinBrute(ModelPart root) {
		this.goblin_brute = root.getChild("goblin_brute");
		this.body = this.goblin_brute.getChild("body");
		this.holdinglocation = this.body.getChild("holdinglocation");
		this.left_arm = this.body.getChild("left_arm");
		this.right_arm = this.body.getChild("right_arm");
		this.head = this.body.getChild("head");
		this.angry_face = this.head.getChild("angry_face");
		this.sad_head = this.head.getChild("sad_head");
		this.left_leg = this.goblin_brute.getChild("left_leg");
		this.right_leg = this.goblin_brute.getChild("right_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition goblin_brute = partdefinition.addOrReplaceChild("goblin_brute", CubeListBuilder.create(), PartPose.offset(0.0F, -3.0F, 0.0F));
		PartDefinition body = goblin_brute.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(0, 0).addBox(-16.0F, -18.0F, -12.0F, 32.0F, 32.0F, 24.0F, new CubeDeformation(0.0F)).texOffs(0, 56).addBox(-16.0F, -18.0F, -12.0F, 32.0F, 32.0F, 24.0F, new CubeDeformation(0.5F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition holdinglocation = body.addOrReplaceChild("holdinglocation", CubeListBuilder.create().texOffs(113, 146).addBox(-6.0F, -5.0F, -4.0F, 11.0F, 9.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(1.0F, 0.0F, -22.0F));
		PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 112).addBox(-1.0F, -5.0F, -6.0F, 9.0F, 25.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(17.0F, -11.0F, -3.0F));
		PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 112).mirror().addBox(-8.0F, -4.0F, -6.0F, 9.0F, 25.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(-17.0F, -11.0F, -3.0F));
		PartDefinition head = body.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(112, 71).addBox(-3.0F, -3.0F, -11.0F, 6.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(112, 24).addBox(-6.0F, -6.0F, -9.0F, 12.0F, 11.0F, 11.0F, new CubeDeformation(0.0F)).texOffs(112, 67)
						.addBox(-14.0F, -4.0F, -4.0F, 11.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(112, 67).mirror().addBox(3.0F, -4.0F, -4.0F, 11.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(0.0F, -17.0F, -10.0F));
		PartDefinition angry_face = head.addOrReplaceChild("angry_face", CubeListBuilder.create().texOffs(40, 112).addBox(-8.0F, -50.0F, -20.0F, 12.0F, 11.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 44.0F, 11.0F));
		PartDefinition sad_head = head.addOrReplaceChild("sad_head", CubeListBuilder.create().texOffs(112, 0).addBox(-8.0F, -50.0F, -20.0F, 12.0F, 13.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 44.0F, 11.0F));
		PartDefinition left_leg = goblin_brute.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(112, 46).addBox(-4.0F, -1.0F, -4.0F, 8.0F, 13.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(9.0F, 15.0F, 0.0F));
		PartDefinition right_leg = goblin_brute.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(112, 46).mirror().addBox(-4.0F, -1.0F, -4.0F, 8.0F, 13.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(-9.0F, 15.0F, 0.0F));
		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		goblin_brute.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.head.yRot = netHeadYaw / (180F / (float) Math.PI);
		this.head.xRot = headPitch / (180F / (float) Math.PI);
	}
}
