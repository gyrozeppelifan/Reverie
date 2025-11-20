package net.eris.reverie.client.model;

// Animasyon dosyanı import etmeyi unutma!
import net.eris.reverie.client.model.animations.GoblinFlagAnimation;
import net.eris.reverie.entity.GoblinFlagEntity;
import net.minecraft.client.model.HierarchicalModel; // <-- DEĞİŞTİ (EntityModel yerine)
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class ModelGoblinFlag<T extends Entity> extends HierarchicalModel<T> { // <-- 1. DEĞİŞİKLİK

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation("reverie", "model_goblin_flag"), "main");

    public final ModelPart stem;
    public final ModelPart flag;

    public ModelGoblinFlag(ModelPart root) {
        this.stem = root.getChild("stem");
        this.flag = this.stem.getChild("flag");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition stem = partdefinition.addOrReplaceChild("stem",
                CubeListBuilder.create().texOffs(16, 27).addBox(-3.5F, -3.0F, -4.0F, 8.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-1.5F, -40.0F, -2.0F, 4.0F, 37.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 0).addBox(-8.5F, -43.0F, -3.0F, 18.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-0.5F, 24.0F, 0.0F));

        PartDefinition flag = stem.addOrReplaceChild("flag",
                CubeListBuilder.create().texOffs(16, 9).addBox(-9.0F, 0.0F, 0.0F, 18.0F, 18.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.5F, -40.0F, -3.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    // <-- 2. DEĞİŞİKLİK: HierarchicalModel için root belirtmek şart
    @Override
    public ModelPart root() {
        return this.stem;
    }

    // <-- 3. DEĞİŞİKLİK: Animasyonları bağladığımız yer
    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        if (entity instanceof GoblinFlagEntity flagEntity) {
            // Entity'deki AnimationState'leri kullanıyoruz
            this.animate(flagEntity.animationState1, GoblinFlagAnimation.waving, ageInTicks, 1.0F);
            this.animate(flagEntity.animationState2, GoblinFlagAnimation.finish, ageInTicks, 1.0F);
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        stem.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}