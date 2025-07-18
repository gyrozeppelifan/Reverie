
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.eris.reverie.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;

import net.eris.reverie.ReverieMod;

public class ReverieModSounds {
	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ReverieMod.MODID);
	public static final RegistryObject<SoundEvent> CHARGE = REGISTRY.register("charge", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "charge")));
	public static final RegistryObject<SoundEvent> DRUNKARD_DRINK = REGISTRY.register("drunkard_drink", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "drunkard_drink")));
	public static final RegistryObject<SoundEvent> BOTTLE_CRASH = REGISTRY.register("bottle_crash", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "bottle_crash")));
	public static final RegistryObject<SoundEvent> DRUNKARD_CHARGE = REGISTRY.register("drunkard_charge", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "drunkard_charge")));
	public static final RegistryObject<SoundEvent> DRUNKARD_HURT = REGISTRY.register("drunkard_hurt", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "drunkard_hurt")));
	public static final RegistryObject<SoundEvent> DRUNKARD_IDLE = REGISTRY.register("drunkard_idle", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "drunkard_idle")));
	public static final RegistryObject<SoundEvent> DRUNKARD_DEATH = REGISTRY.register("drunkard_death", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "drunkard_death")));
	public static final RegistryObject<SoundEvent> GLUE = REGISTRY.register("glue", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "glue")));
	public static final RegistryObject<SoundEvent> SPIKED_LOG_CRASH = REGISTRY.register("spiked_log_crash", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "spiked_log_crash")));
	public static final RegistryObject<SoundEvent> SPIKED_LOG_ROLL = REGISTRY.register("spiked_log_roll", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "spiked_log_roll")));
	public static final RegistryObject<SoundEvent> SPIKED_LOG_THROW = REGISTRY.register("spiked_log_throw", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "spiked_log_throw")));
}
