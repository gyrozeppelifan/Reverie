package net.eris.reverie.client.model; // Paket ismini kendi paketine göre ayarla

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.eris.reverie.ReverieMod; // Mod ID için

// Class ismini 'mamma'dan 'Stitched'e çevirdim
public class Stitched<T extends Entity> extends EntityModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(ReverieMod.MODID, "stitched"), "main");
	private final ModelPart creature;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart basehead;
	private final ModelPart lightning;
	private final ModelPart mouth;
	private final ModelPart right_arm;
	private final ModelPart left_arm;
	private final ModelPart right_leg;
	private final ModelPart left_leg;

	public Stitched(ModelPart root) {
		this.creature = root.getChild("creature");
		this.body = this.creature.getChild("body");
		this.head = this.body.getChild("head");
		this.basehead = this.head.getChild("basehead");
		this.lightning = this.head.getChild("lightning");
		this.mouth = this.head.getChild("mouth");
		this.right_arm = this.body.getChild("right_arm");
		this.left_arm = this.body.getChild("left_arm");
		this.right_leg = this.creature.getChild("right_leg");
		this.left_leg = this.creature.getChild("left_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition creature = partdefinition.addOrReplaceChild("creature", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition body = creature.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-9.0F, -21.0F, -6.0F, 18.0F, 12.0F, 11.0F, new CubeDeformation(0.0F))
				.texOffs(0, 23).addBox(-9.0F, -9.0F, -6.0F, 18.0F, 8.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -13.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -21.0F, -2.0F));

		// Basehead (Normal Kafa)
		PartDefinition basehead = head.addOrReplaceChild("basehead", CubeListBuilder.create().texOffs(58, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-1.0F, -3.0F, -6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		// Lightning (Paratoner Kafa)
		PartDefinition lightning = head.addOrReplaceChild("lightning", CubeListBuilder.create().texOffs(58, 38).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(32, 6).addBox(-1.0F, -15.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 42).addBox(-1.0F, -3.0F, -6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition mouth = head.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(82, 56).addBox(-2.0F, -1.0F, -6.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 60).addBox(-9.0F, -5.0F, -4.0F, 10.0F, 26.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(-13.0F, -21.0F, -3.0F));

		PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 60).addBox(0.0F, -4.0F, -5.0F, 10.0F, 26.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(12.0F, -22.0F, -2.0F));

		PartDefinition right_leg = creature.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(84, 13).addBox(-4.0F, -1.0F, -4.0F, 7.0F, 14.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, -13.0F, 0.0F));

		PartDefinition left_leg = creature.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(84, 34).addBox(-3.0F, -1.0F, -4.0F, 7.0F, 14.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, -13.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// Burası Renderer'dan yönetiliyor (AnimatedModel class'ında), burayı boş bırakabiliriz veya temel rotasyonları ekleyebiliriz.
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		creature.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	// --- GÖRÜNÜRLÜK YÖNETİMİ ---
	public void setupVisibility(boolean hasLightningRod) {
		// Lightning Rod varsa basehead gizlensin, lightning görünsün
		this.basehead.visible = !hasLightningRod;
		this.lightning.visible = hasLightningRod;
	}
}