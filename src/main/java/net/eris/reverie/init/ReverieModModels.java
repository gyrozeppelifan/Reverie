
package net.eris.reverie.init;

import net.eris.reverie.client.model.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class ReverieModModels {
	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(ModelDrunkard.LAYER_LOCATION, ModelDrunkard::createBodyLayer);
		event.registerLayerDefinition(Modelspiked_log.LAYER_LOCATION, Modelspiked_log::createBodyLayer);
		event.registerLayerDefinition(ModelBrawler.LAYER_LOCATION, ModelBrawler::createBodyLayer);
		event.registerLayerDefinition(Modelgoblin.LAYER_LOCATION, Modelgoblin::createBodyLayer);
		event.registerLayerDefinition(Modelshooter_goblin.LAYER_LOCATION, Modelshooter_goblin::createBodyLayer);
		event.registerLayerDefinition(ModelBoneSpearProjectile.LAYER_LOCATION, ModelBoneSpearProjectile::createBodyLayer);
		event.registerLayerDefinition(Modelgoblin_barrel.LAYER_LOCATION, Modelgoblin_barrel::createBodyLayer);
		event.registerLayerDefinition(ModelGoblet.LAYER_LOCATION, ModelGoblet::createBodyLayer);
		event.registerLayerDefinition(ModelGoblinBrute.LAYER_LOCATION, ModelGoblinBrute::createBodyLayer);
		event.registerLayerDefinition(ModelGoblinFlag.LAYER_LOCATION, ModelGoblinFlag::createBodyLayer);
		event.registerLayerDefinition(Stitched.LAYER_LOCATION, Stitched::createBodyLayer);
		event.registerLayerDefinition(MutagenBlobModel.LAYER_LOCATION, MutagenBlobModel::createBodyLayer);
		event.registerLayerDefinition(HogModel.LAYER_LOCATION, HogModel::createBodyLayer);

	}
}
