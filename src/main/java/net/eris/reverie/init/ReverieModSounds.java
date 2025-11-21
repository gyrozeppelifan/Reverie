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
	public static final RegistryObject<SoundEvent> GOBLIN_BARREL_GROUND_HIT = REGISTRY.register("goblin_barrel_ground_hit", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "goblin_barrel_ground_hit")));
	public static final RegistryObject<SoundEvent> GOBLIN_BARREL_BREAK = REGISTRY.register("goblin_barrel_break", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "goblin_barrel_break")));
	public static final RegistryObject<SoundEvent> STRUGGLE = REGISTRY.register("struggle", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "struggle")));
	public static final RegistryObject<SoundEvent> BARREL_GOBLIN_SPAWN = REGISTRY.register("barrel_goblin_spawn", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "barrel_goblin_spawn")));
	public static final RegistryObject<SoundEvent> GOBLIN_STAFF_DECLINE = REGISTRY.register("goblin_staff_decline", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "goblin_staff_decline")));
	public static final RegistryObject<SoundEvent> GOBLIN_BARREL_SUMMON = REGISTRY.register("goblin_barrel_summon", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "goblin_barrel_summon")));
	public static final RegistryObject<SoundEvent> LUCKY_CRIT = REGISTRY.register("lucky_crit", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "lucky_crit")));
	public static final RegistryObject<SoundEvent> GOBLIN_BRUTE_ROAR = REGISTRY.register("goblin_brute_roar", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "goblin_brute_roar")));
	public static final RegistryObject<SoundEvent> SHOOTER_GOBLIN_DEATH = REGISTRY.register("shooter_goblin_death", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "shooter_goblin_death")));
	public static final RegistryObject<SoundEvent> SHOOTER_BRUTE_TEAMUP = REGISTRY.register("shooter_brute_teamup", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "shooter_brute_teamup")));
	public static final RegistryObject<SoundEvent> ANCIENT_CLOAK = REGISTRY.register("ancient_cloak", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "ancient_cloak")));
	public static final RegistryObject<SoundEvent> ANCIENT_CROSSBOW_SHOOT_NORMAL = REGISTRY.register("ancient_crossbow_shoot_normal", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "ancient_crossbow_shoot_normal")));
	public static final RegistryObject<SoundEvent> ANCIENT_CROSSBOW_SHOOT_FAST = REGISTRY.register("ancient_crossbow_shoot_fast", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "ancient_crossbow_shoot_fast")));
	public static final RegistryObject<SoundEvent> ANCIENT_CROSSBOW_SHOOT_RAPID = REGISTRY.register("ancient_crossbow_shoot_rapid", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "ancient_crossbow_shoot_rapid")));
	public static final RegistryObject<SoundEvent> ANCIENT_CROSSBOW_LOAD = REGISTRY.register("ancient_crossbow_load", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "ancient_crossbow_load")));
	public static final RegistryObject<SoundEvent> ANCIENT_CROSSBOW_CLOAK_SHOOT = REGISTRY.register("ancient_crossbow_cloak_shoot", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "ancient_crossbow_cloak_shoot")));
	public static final RegistryObject<SoundEvent> KISSING = REGISTRY.register("kissing", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("reverie", "kissing")));
}



