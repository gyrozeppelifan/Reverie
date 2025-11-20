
package net.eris.reverie.init;

import net.eris.reverie.potion.*;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.effect.MobEffect;

import net.eris.reverie.ReverieMod;

public class ReverieModMobEffects {
	public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ReverieMod.MODID);
	public static final RegistryObject<MobEffect> DRUNKEN_RAGE = REGISTRY.register("drunken_rage", () -> new DrunkenRageMobEffect());
	public static final RegistryObject<MobEffect> BLEEDING = REGISTRY.register("bleeding", () -> new BleedingMobEffect());
	public static final RegistryObject<MobEffect> DRUNK = REGISTRY.register("drunk", () -> new DrunkMobEffect());
	public static final RegistryObject<MobEffect> FOCUS = REGISTRY.register("focus", () -> new FocusMobEffect());
	public static final RegistryObject<MobEffect> POSSESSION = REGISTRY.register("possession", () -> new PossessionMobEffect());
	public static final RegistryObject<MobEffect> ANCIENT_CLOAK = REGISTRY.register("ancient_cloak", () -> new net.eris.reverie.effect.AncientCloakMobEffect());
}
