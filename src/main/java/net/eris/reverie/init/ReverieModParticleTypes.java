
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
	public static final RegistryObject<SimpleParticleType> WILD_FIRE = REGISTRY.register("wild_fire", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> GLASS_SHARDS = REGISTRY.register("glass_shards", () -> new SimpleParticleType(false));
	public static final RegistryObject<SimpleParticleType> BARREL_SHARD_1 = REGISTRY.register("barrel_shard_1", () -> new SimpleParticleType(false));
	public static final RegistryObject<SimpleParticleType> BARREL_SHARD_2 = REGISTRY.register("barrel_shard_2", () -> new SimpleParticleType(false));
	public static final RegistryObject<SimpleParticleType> BARREL_NAIL = REGISTRY.register("barrel_nail", () -> new SimpleParticleType(false));
	public static final RegistryObject<SimpleParticleType> BARREL_METAL_SHARD = REGISTRY.register("barrel_metal_shard", () -> new SimpleParticleType(false));
	public static final RegistryObject<SimpleParticleType> STITCHED_ZAP_PARTICLE = REGISTRY.register("stitched_zap_particle",
			() -> new SimpleParticleType(false));
	public static final RegistryObject<SimpleParticleType> SPIRIT_ORB = REGISTRY.register("spirit_orb",
			() -> new SimpleParticleType(true));

}
