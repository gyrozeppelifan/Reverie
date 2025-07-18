
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.eris.reverie.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.effect.MobEffect;

import net.eris.reverie.potion.FocusMobEffect;
import net.eris.reverie.potion.DrunkenRageMobEffect;
import net.eris.reverie.potion.DrunkMobEffect;
import net.eris.reverie.potion.BleedingMobEffect;
import net.eris.reverie.ReverieMod;

public class ReverieModMobEffects {
	public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ReverieMod.MODID);
	public static final RegistryObject<MobEffect> DRUNKEN_RAGE = REGISTRY.register("drunken_rage", () -> new DrunkenRageMobEffect());
	public static final RegistryObject<MobEffect> BLEEDING = REGISTRY.register("bleeding", () -> new BleedingMobEffect());
	public static final RegistryObject<MobEffect> DRUNK = REGISTRY.register("drunk", () -> new DrunkMobEffect());
	public static final RegistryObject<MobEffect> FOCUS = REGISTRY.register("focus", () -> new FocusMobEffect());
}
