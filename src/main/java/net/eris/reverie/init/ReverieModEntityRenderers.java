
package net.eris.reverie.init;

import net.eris.reverie.client.renderer.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ReverieModEntityRenderers {
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ReverieModEntities.BRAWLER.get(), BrawlerRenderer::new);
		event.registerEntityRenderer(ReverieModEntities.DRUNKARD.get(), DrunkardRenderer::new);
		event.registerEntityRenderer(ReverieModEntities.SPIKED_LOG.get(), SpikedLogRenderer::new);
		event.registerEntityRenderer(ReverieModEntities.GOBLIN.get(), GoblinRenderer::new);
		event.registerEntityRenderer(ReverieModEntities.SHOOTER_GOBLIN.get(), ShooterGoblinRenderer::new);
		event.registerEntityRenderer(ReverieModEntities.BONE_SPEAR_PROJECTILE.get(), BoneSpearProjectileRenderer::new);
		event.registerEntityRenderer(ReverieModEntities.BARREL_GOBLIN.get(), GoblinRenderer::new);
		event.registerEntityRenderer(ReverieModEntities.GOBLIN_BARREL.get(), GoblinBarrelRenderer::new);
		event.registerEntityRenderer(ReverieModEntities.GOBLET.get(), GobletRenderer::new);
		event.registerEntityRenderer(ReverieModEntities.GOBLIN_BRUTE.get(), GoblinBruteRenderer::new);
		event.registerEntityRenderer(ReverieModEntities.POSSESSION_PUPPET.get(), PossessionPuppetRenderer::new);
		event.registerEntityRenderer(ReverieModEntities.GOBLIN_FLAG.get(), GoblinFlagRenderer::new);
		event.registerEntityRenderer(ReverieModEntities.MUTAGEN_BLOB.get(), MutagenBlobRenderer::new);
		event.registerEntityRenderer(ReverieModEntities.MAGIC_ARROW.get(), MagicArrowRenderer::new);
		event.registerEntityRenderer(ReverieModEntities.STITCHED.get(), StitchedRenderer::new);
		event.registerEntityRenderer(ReverieModEntities.HOG.get(), HogRenderer::new);
		event.registerEntityRenderer(ReverieModEntities.GECK.get(), GeckRenderer::new);

	}
}
