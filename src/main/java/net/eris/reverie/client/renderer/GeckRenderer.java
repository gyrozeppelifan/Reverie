package net.eris.reverie.client.renderer;

import net.eris.reverie.client.model.GeckModel;
import net.eris.reverie.client.renderer.layer.ProfessionLayer;
import net.eris.reverie.entity.custom.GeckEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GeckRenderer extends MobRenderer<GeckEntity, GeckModel<GeckEntity>> {
    public GeckRenderer(EntityRendererProvider.Context context) {
        super(context, new GeckModel<>(context.bakeLayer(GeckModel.LAYER_LOCATION)), 0.5f);
        this.addLayer(new ProfessionLayer<>(this, "geck"));
    }

    @Override
    public ResourceLocation getTextureLocation(GeckEntity entity) {
        String variant = switch (entity.getVariant()) {
            case 1 -> "lizard"; case 2 -> "snake"; default -> "chameleon";
        };
        return new ResourceLocation("reverie", "textures/entity/folk/geck/geck_" + variant + ".png");
    }
}