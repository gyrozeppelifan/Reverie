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
public class ModelGoblet<T extends Entity> extends EntityModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in
    // the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("reverie", "model_goblet"), "main");
    public final ModelPart body;
    public final ModelPart head;
    public final ModelPart leftarm;
    public final ModelPart rightarm;
    public final ModelPart rightleg;
    public final ModelPart leftleg;

    public ModelGoblet(ModelPart root) {
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.leftarm = this.body.getChild("leftarm");
        this.rightarm = this.body.getChild("rightarm");
        this.rightleg = root.getChild("rightleg");
        this.leftleg = root.getChild("leftleg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 13).addBox(-2.0F, -7.0F, -1.0F, 6.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 23.0F, -1.0F));
        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(20, 20).addBox(-3.0F, -14.0F, 0.0F, 6.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(20, 13).addBox(5.0F, -7.0F, 0.0F, 6.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
                        .addBox(-5.0F, -7.0F, -3.0F, 10.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(8, 27).addBox(-1.0F, -4.0F, -4.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(20, 13).mirror()
                        .addBox(-11.0F, -7.0F, 0.0F, 6.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(1.0F, -7.0F, 1.0F));
        PartDefinition leftarm = body.addOrReplaceChild("leftarm", CubeListBuilder.create().texOffs(0, 22).mirror().addBox(0.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(4.0F, -7.0F, 1.0F));
        PartDefinition rightarm = body.addOrReplaceChild("rightarm", CubeListBuilder.create().texOffs(0, 22).addBox(-2.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -7.0F, 1.0F));
        PartDefinition rightleg = partdefinition.addOrReplaceChild("rightleg", CubeListBuilder.create().texOffs(8, 22).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 21.0F, 0.0F));
        PartDefinition leftleg = partdefinition.addOrReplaceChild("leftleg", CubeListBuilder.create().texOffs(8, 22).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(2.0F, 21.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        rightleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leftleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw / (180F / (float) Math.PI);
        this.head.xRot = headPitch / (180F / (float) Math.PI);
    }
}
