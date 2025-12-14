package net.eris.reverie.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.eris.reverie.ReverieMod;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ResonantGongModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(ReverieMod.MODID, "resonant_gong"), "main");

    public final ModelPart root;
    public final ModelPart gongBody;

    public ResonantGongModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root.getChild("root");
        this.gongBody = this.root.getChild("gong_body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // --- DÜZELTME BURADA ---
        // Önceki: 24.0F (Havada kalıyordu)
        // Yeni: 29.0F (5 Piksel aşağı indi -> 24 + 5 = 29)
        // Eğer hala tam oturmazsa bu sayıyla oynayarak (artırıp azaltarak) yerini ayarlayabilirsin.
        PartDefinition root = partdefinition.addOrReplaceChild("root",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 32.0F, 0.0F));

        PartDefinition gong_body = root.addOrReplaceChild("gong_body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-16.0F, 0.0F, -1.0F, 32.0F, 32.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -32.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setupAnimation(float swingAmount) {
        this.gongBody.xRot = swingAmount;
    }
}