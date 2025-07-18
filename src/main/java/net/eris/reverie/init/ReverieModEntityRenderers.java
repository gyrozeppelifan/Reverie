
package net.eris.reverie.init;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.eris.reverie.client.renderer.SpikedLogRenderer;
import net.eris.reverie.client.renderer.DrunkardRenderer;
import net.eris.reverie.client.renderer.BrawlerRenderer;
import net.eris.reverie.client.renderer.GoblinRenderer;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ReverieModEntityRenderers {
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ReverieModEntities.BRAWLER.get(), BrawlerRenderer::new);
		event.registerEntityRenderer(ReverieModEntities.DRUNKARD.get(), DrunkardRenderer::new);
		event.registerEntityRenderer(ReverieModEntities.SPIKED_LOG.get(), SpikedLogRenderer::new);
		event.registerEntityRenderer(ReverieModEntities.GOBLIN.get(), GoblinRenderer::new);

	}
}
