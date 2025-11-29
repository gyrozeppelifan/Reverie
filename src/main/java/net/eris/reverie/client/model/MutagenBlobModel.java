package net.eris.reverie.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.eris.reverie.ReverieMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class MutagenBlobModel<T extends Entity> extends EntityModel<T> {
    // Layer lokasyonunu belirledik. Renderer'da bunu kullanacağız.
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(ReverieMod.MODID, "mutagen_blob"), "main");

    private final ModelPart bb_main;

    public MutagenBlobModel(ModelPart root) {
        // Blockbench'ten gelen isimlendirme "bb_main" idi, onu burada yakalıyoruz.
        this.bb_main = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // SENİN VERDİĞİN KOD BURADA ENTEGRE EDİLDİ:
        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F)
        );

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Top havada uçarken kendi etrafında yavaşça dönsün, daha havalı durur.
        // İstemezsen bu satırları silebilirsin.
        this.bb_main.yRot = ageInTicks * 0.2F;
        this.bb_main.xRot = ageInTicks * 0.1F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}