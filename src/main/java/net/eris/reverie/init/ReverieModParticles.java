
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.eris.reverie.init;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.eris.reverie.client.particle.GlassShardsParticle;
import net.eris.reverie.client.particle.BoozeBubblesParticle;
import net.eris.reverie.client.particle.BloodParticle;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ReverieModParticles {
	@SubscribeEvent
	public static void registerParticles(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(ReverieModParticleTypes.BLOOD.get(), BloodParticle::provider);
		event.registerSpriteSet(ReverieModParticleTypes.BOOZE_BUBBLES.get(), BoozeBubblesParticle::provider);
		event.registerSpriteSet(ReverieModParticleTypes.GLASS_SHARDS.get(), GlassShardsParticle::provider);
	}
}
