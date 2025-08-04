
package net.eris.reverie.init;

import net.eris.reverie.client.particle.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ReverieModParticles {
	@SubscribeEvent
	public static void registerParticles(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(ReverieModParticleTypes.BLOOD.get(), BloodParticle::provider);
		event.registerSpriteSet(ReverieModParticleTypes.BOOZE_BUBBLES.get(), BoozeBubblesParticle::provider);
		event.registerSpriteSet(ReverieModParticleTypes.GLASS_SHARDS.get(), GlassShardsParticle::provider);
		event.registerSpriteSet(ReverieModParticleTypes.BARREL_SHARD_1.get(), BarrelShard1Particle::provider);
		event.registerSpriteSet(ReverieModParticleTypes.BARREL_SHARD_2.get(), BarrelShard2Particle::provider);
		event.registerSpriteSet(ReverieModParticleTypes.BARREL_NAIL.get(), BarrelNailParticle::provider);
		event.registerSpriteSet(ReverieModParticleTypes.BARREL_METAL_SHARD.get(), BarrelMetalShardParticle::provider);
	}
}
