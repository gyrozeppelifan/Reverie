package net.eris.reverie.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.entity.HogEntity;
import net.minecraft.client.model.HierarchicalModel; // ÖNEMLİ: EntityModel değil!
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class HogModel extends HierarchicalModel<HogEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(ReverieMod.MODID, "hog"), "main");

    // Kemikler PUBLIC olmalı ki Renderer erişsin
    public final ModelPart root;
    public final ModelPart mountplace;
    public final ModelPart wingleft;
    public final ModelPart wingright;
    public final ModelPart bodyarmor;
    public final ModelPart head;
    public final ModelPart helmet;
    public final ModelPart leg1left;
    public final ModelPart leg2left;
    public final ModelPart leg1right;
    public final ModelPart leg2right;

    public HogModel(ModelPart root) {
        this.root = root.getChild("root");
        this.mountplace = this.root.getChild("mountplace");
        this.wingleft = this.root.getChild("wingleft");
        this.wingright = this.root.getChild("wingright");
        this.bodyarmor = this.root.getChild("bodyarmor");
        this.leg1left = this.root.getChild("leg1left");
        this.leg2left = this.root.getChild("leg2left");
        this.leg1right = this.root.getChild("leg1right");
        this.leg2right = this.root.getChild("leg2right");
        this.head = this.root.getChild("head");
        this.helmet = this.head.getChild("helmet");

        // Varsayılan Gizlilik
        setWingsVisible(false);
        setArmorVisible(false);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // --- BLOCKBENCH KODUN ---
        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -11.0F, -7.0F, 12.0F, 11.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(0, 68).addBox(0.0F, -9.0F, 11.0F, 0.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 17.0F, -2.0F));

        root.addOrReplaceChild("mountplace", CubeListBuilder.create().texOffs(60, 49).addBox(2.0F, -11.0F, 4.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -2.0F, -6.0F));
        root.addOrReplaceChild("wingleft", CubeListBuilder.create().texOffs(0, 58).addBox(0.0F, -1.0F, -4.0F, 15.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, -9.0F, 0.0F));
        root.addOrReplaceChild("wingright", CubeListBuilder.create().texOffs(0, 58).mirror().addBox(-15.0F, -1.0F, -4.0F, 15.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-6.0F, -9.0F, 0.0F));
        root.addOrReplaceChild("bodyarmor", CubeListBuilder.create().texOffs(0, 29).addBox(-2.0F, -11.0F, -1.0F, 12.0F, 11.0F, 18.0F, new CubeDeformation(0.4F)), PartPose.offset(-4.0F, 0.0F, -6.0F));

        root.addOrReplaceChild("leg1left", CubeListBuilder.create().texOffs(60, 31).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(4.0F, 0.0F, -4.0F));
        root.addOrReplaceChild("leg2left", CubeListBuilder.create().texOffs(60, 31).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(4.0F, 0.0F, 7.9F));
        root.addOrReplaceChild("leg1right", CubeListBuilder.create().texOffs(60, 31).addBox(-2.0F, 0.0F, -1.9F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 0.0F, -4.1F));
        root.addOrReplaceChild("leg2right", CubeListBuilder.create().texOffs(60, 31).addBox(-2.0F, 0.0F, -1.9F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 0.0F, 7.9F));

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(46, 58).addBox(-5.0F, -7.0F, -7.0F, 10.0F, 11.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(60, 19).mirror().addBox(5.0F, -11.0F, -7.0F, 0.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(60, 19).addBox(-5.0F, -11.0F, -7.0F, 0.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(60, 42).addBox(-3.0F, 0.0F, -10.0F, 6.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(20, 68).addBox(4.0F, 0.0F, -10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(10, 68).addBox(3.0F, 2.0F, -10.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(10, 68).mirror().addBox(-6.0F, 2.0F, -10.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(28, 68).addBox(-6.0F, 0.0F, -10.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, -8.0F));

        head.addOrReplaceChild("helmet", CubeListBuilder.create().texOffs(60, 0).addBox(-4.0F, 0.0F, -7.0F, 10.0F, 11.0F, 8.0F, new CubeDeformation(0.4F)), PartPose.offset(-1.0F, -7.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(HogEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // BURASI BOŞ KALACAK. Render'da override edilecek.
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setWingsVisible(boolean visible) {
        this.wingleft.visible = visible;
        this.wingright.visible = visible;
    }

    public void setArmorVisible(boolean visible) {
        this.bodyarmor.visible = visible;
        this.helmet.visible = visible;
    }
}