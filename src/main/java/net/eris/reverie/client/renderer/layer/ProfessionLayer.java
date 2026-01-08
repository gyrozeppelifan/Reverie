package net.eris.reverie.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.eris.reverie.entity.custom.FolkEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class ProfessionLayer<T extends FolkEntity, M extends HierarchicalModel<T>> extends RenderLayer<T, M> {
    private final String raceFolder; // "geck", "talon" veya "varmint"

    public ProfessionLayer(RenderLayerParent<T, M> parent, String raceFolder) {
        super(parent);
        this.raceFolder = raceFolder;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, T entity,
                       float limb, float amount, float ticks, float age, float yaw, float pitch) {
        if (entity.getProfessionId() == 0) return;

        String prof = getProfName(entity.getProfessionId());
        // Örn: reverie:textures/entity/folk/geck/professions/banker.png
        ResourceLocation texture = new ResourceLocation("reverie", "textures/entity/folk/" + raceFolder + "/professions/" + prof + ".png");

        this.getParentModel().renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityTranslucent(texture)),
                light, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
    }

    private String getProfName(int id) {
        return switch (id) {
            case 1 -> "barkeeper"; case 2 -> "gunsmith"; case 3 -> "banker";
            case 4 -> "stable_master"; case 5 -> "bounty_clerk"; case 6 -> "tailor";
            default -> "unemployed";
        };
    }
}