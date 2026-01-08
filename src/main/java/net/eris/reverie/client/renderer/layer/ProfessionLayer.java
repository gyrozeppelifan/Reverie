package net.eris.reverie.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.eris.reverie.client.model.GeckModel;
import net.eris.reverie.entity.custom.FolkEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class ProfessionLayer<T extends FolkEntity, M extends GeckModel<T>> extends RenderLayer<T, M> {
    private final String raceFolder; // "geck", "talon", "varmint"

    public ProfessionLayer(RenderLayerParent<T, M> parent, String raceFolder) {
        super(parent);
        this.raceFolder = raceFolder;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        int professionId = entity.getProfessionId();
        if (professionId == 0) return; // İşsizse çizme

        String profName = getProfName(professionId);

        // DÜZELTME: ReverieMod.MOD_ID yerine direkt "reverie" yazdık.
        ResourceLocation texture = new ResourceLocation("reverie",
                "textures/entity/folk/" + raceFolder + "/professions/" + profName + ".png");

        // Translucent render tipi, kıyafetler için en iyisidir
        this.getParentModel().renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityTranslucent(texture)),
                light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    private String getProfName(int id) {
        return switch (id) {
            case 1 -> "barkeeper";
            case 2 -> "gunsmith";
            case 3 -> "banker";
            case 4 -> "stable_master";
            case 5 -> "bounty_clerk";
            case 6 -> "tailor";
            case 7 -> "undertaker";
            case 8 -> "prospector";
            case 9 -> "outfitter";
            default -> "unemployed";
        };
    }
}