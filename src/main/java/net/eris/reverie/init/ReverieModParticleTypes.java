
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.eris.reverie.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;

import net.eris.reverie.ReverieMod;

public class ReverieModParticleTypes {
	public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ReverieMod.MODID);
	public static final RegistryObject<SimpleParticleType> BLOOD = REGISTRY.register("blood", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> BOOZE_BUBBLES = REGISTRY.register("booze_bubbles", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> GLASS_SHARDS = REGISTRY.register("glass_shards", () -> new SimpleParticleType(false));
}
