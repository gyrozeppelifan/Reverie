
package net.eris.reverie.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import net.eris.reverie.entity.BrawlerEntity;
import net.eris.reverie.client.model.ModelBrawler;

public class BrawlerRenderer extends MobRenderer<BrawlerEntity, ModelBrawler<BrawlerEntity>> {
	public BrawlerRenderer(EntityRendererProvider.Context context) {
		super(context, new ModelBrawler<BrawlerEntity>(context.bakeLayer(ModelBrawler.LAYER_LOCATION)), 0.8f);
	}

	@Override
	public ResourceLocation getTextureLocation(BrawlerEntity entity) {
		return new ResourceLocation("reverie:textures/entities/brawler.png");
	}
}
