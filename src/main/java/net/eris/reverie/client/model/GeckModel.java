package net.eris.reverie.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.eris.reverie.client.model.animations.GeckAnimation;
import net.eris.reverie.entity.custom.FolkEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

// Vanilla animasyonlar için EntityModel yerine HierarchicalModel kullanıyoruz.
public class GeckModel<T extends FolkEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("reverie", "geck"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart tail;
    // Diğer wear/hat parçaları model hiyerarşisinde tanımlı olduğu için doğrudan root üzerinden yönetilir.

    public GeckModel(ModelPart root) {
        this.root = root.getChild("geck");
        this.body = this.root.getChild("body");
        this.head = this.body.getChild("head");
        this.tail = this.body.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Senin Blockbench'ten attığın o detaylı Mesh yapısı:
        PartDefinition geck = partdefinition.addOrReplaceChild("geck", CubeListBuilder.create(), PartPose.offset(-2.0F, 11.0F, 0.0F));

        PartDefinition body = geck.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 49).addBox(-1.0F, -9.0F, 0.0F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(40, 66).addBox(1.0F, -11.0F, 1.0F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -3.0F, -2.0F));

        body.addOrReplaceChild("body_wear", CubeListBuilder.create().texOffs(36, 15).addBox(-1.0F, -9.0F, 0.0F, 8.0F, 18.0F, 6.0F, new CubeDeformation(0.2F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(28, 52).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 1.0F, 6.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 15).addBox(-5.0F, -9.0F, -4.0F, 10.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(64, 31).addBox(2.0F, -7.0F, -5.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(64, 31).addBox(-6.0F, -7.0F, -5.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -11.0F, 2.0F));

        head.addOrReplaceChild("head_wear", CubeListBuilder.create().texOffs(0, 32).addBox(-1.0F, -9.0F, -1.0F, 10.0F, 9.0F, 8.0F, new CubeDeformation(0.4F)), PartPose.offset(-4.0F, 0.0F, -3.0F));

        head.addOrReplaceChild("eye_wear", CubeListBuilder.create().texOffs(0, 83).addBox(-1.0F, -3.0F, -1.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.1F))
                .texOffs(0, 83).addBox(-9.0F, -3.0F, -1.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offset(3.0F, -4.0F, -4.0F));

        head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -11.0F, -6.0F, 16.0F, 4.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(36, 39).addBox(-5.0F, -12.0F, -4.0F, 10.0F, 5.0F, 8.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, -1.4F, 0.0F));

        PartDefinition left_leg = body.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(64, 16).mirror().addBox(-1.0F, 0.0F, -1.0F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(5.0F, 3.0F, 2.0F));
        left_leg.addOrReplaceChild("left_leg_wear", CubeListBuilder.create().texOffs(28, 66).mirror().addBox(-1.0F, 0.0F, -1.0F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(58, 52).addBox(0.0F, -1.0F, -1.0F, 3.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, -8.0F, 2.0F));
        left_arm.addOrReplaceChild("left_arm_wear", CubeListBuilder.create().texOffs(64, 0).addBox(4.0F, -9.0F, 0.0F, 3.0F, 13.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offset(-4.0F, 8.0F, -1.0F));

        PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(58, 52).mirror().addBox(-3.0F, -1.0F, -1.0F, 3.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.0F, -8.0F, 2.0F));
        right_arm.addOrReplaceChild("right_arm_wear", CubeListBuilder.create().texOffs(64, 0).mirror().addBox(4.0F, -9.0F, 0.0F, 3.0F, 13.0F, 3.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offset(-7.0F, 8.0F, -1.0F));

        PartDefinition right_leg = body.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(64, 16).addBox(-2.0F, 0.0F, -1.0F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 3.0F, 2.0F));
        right_leg.addOrReplaceChild("right_leg_wear", CubeListBuilder.create().texOffs(28, 66).addBox(-2.0F, 0.0F, -1.0F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose); // Her kareden önce pozu sıfırlıyoruz.

        // Kafa Hareketleri
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);

        // --- ANİMASYON TETİKLEYİCİLERİ ---
        // Boşta durma ve yürüme (Ana döngü)
        this.animate(entity.idleAnimationState, GeckAnimation.idle, ageInTicks);
        this.animateWalk(GeckAnimation.walk, limbSwing, limbSwingAmount, 1.0f, 1.0f);

        // Özel Durumlar (Panik ve Meslek Çalışması)
        this.animate(entity.panicAnimationState, GeckAnimation.panic, ageInTicks);

        // Meslek spesifik animasyonlar (Mesela Barmen)
        if (entity.getProfessionId() == 1) { // BARKEEPER
            this.animate(entity.idleAnimationState, GeckAnimation.workBarkeeper, ageInTicks);
        }
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}