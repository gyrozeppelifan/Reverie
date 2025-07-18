
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.eris.reverie.init;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.eris.reverie.client.model.Modelspiked_log;
import net.eris.reverie.client.model.ModelDrunkard;
import net.eris.reverie.client.model.ModelBrawler;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class ReverieModModels {
	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(ModelDrunkard.LAYER_LOCATION, ModelDrunkard::createBodyLayer);
		event.registerLayerDefinition(Modelspiked_log.LAYER_LOCATION, Modelspiked_log::createBodyLayer);
		event.registerLayerDefinition(ModelBrawler.LAYER_LOCATION, ModelBrawler::createBodyLayer);
	}
}
