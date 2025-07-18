
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.eris.reverie.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Block;

import net.eris.reverie.block.StrippedOliveWoodBlock;
import net.eris.reverie.block.StrippedOliveLogBlock;
import net.eris.reverie.block.PointyBoneBlock;
import net.eris.reverie.block.OliveWoodBlock;
import net.eris.reverie.block.OliveTrapdoorBlock;
import net.eris.reverie.block.OliveStairsBlock;
import net.eris.reverie.block.OliveSlabBlock;
import net.eris.reverie.block.OliveSaplingBlock;
import net.eris.reverie.block.OlivePressurePlateBlock;
import net.eris.reverie.block.OlivePlanksBlock;
import net.eris.reverie.block.OlivePileBlock;
import net.eris.reverie.block.OliveLogBlock;
import net.eris.reverie.block.OliveLeavesBlock;
import net.eris.reverie.block.OliveFenceGateBlock;
import net.eris.reverie.block.OliveFenceBlock;
import net.eris.reverie.block.OliveDoorBlock;
import net.eris.reverie.block.OliveButtonBlock;
import net.eris.reverie.block.OliveBranchesBlock;
import net.eris.reverie.block.MashedOliveBlock;
import net.eris.reverie.block.HayThatchStairsBlock;
import net.eris.reverie.block.HayThatchSlabBlock;
import net.eris.reverie.block.HayThatchBlock;
import net.eris.reverie.ReverieMod;

public class ReverieModBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, ReverieMod.MODID);
	public static final RegistryObject<Block> HAY_THATCH = REGISTRY.register("hay_thatch", () -> new HayThatchBlock());
	public static final RegistryObject<Block> HAY_THATCH_STAIRS = REGISTRY.register("hay_thatch_stairs", () -> new HayThatchStairsBlock());
	public static final RegistryObject<Block> HAY_THATCH_SLAB = REGISTRY.register("hay_thatch_slab", () -> new HayThatchSlabBlock());
	public static final RegistryObject<Block> POINTY_BONE = REGISTRY.register("pointy_bone", () -> new PointyBoneBlock());
	public static final RegistryObject<Block> OLIVE_LEAVES = REGISTRY.register("olive_leaves", () -> new OliveLeavesBlock());
	public static final RegistryObject<Block> OLIVE_WOOD = REGISTRY.register("olive_wood", () -> new OliveWoodBlock());
	public static final RegistryObject<Block> OLIVE_BRANCHES = REGISTRY.register("olive_branches", () -> new OliveBranchesBlock());
	public static final RegistryObject<Block> OLIVE_TRAPDOOR = REGISTRY.register("olive_trapdoor", () -> new OliveTrapdoorBlock());
	public static final RegistryObject<Block> OLIVE_DOOR = REGISTRY.register("olive_door", () -> new OliveDoorBlock());
	public static final RegistryObject<Block> OLIVE_LOG = REGISTRY.register("olive_log", () -> new OliveLogBlock());
	public static final RegistryObject<Block> OLIVE_PLANKS = REGISTRY.register("olive_planks", () -> new OlivePlanksBlock());
	public static final RegistryObject<Block> OLIVE_SAPLING = REGISTRY.register("olive_sapling", () -> new OliveSaplingBlock());
	public static final RegistryObject<Block> OLIVE_FENCE = REGISTRY.register("olive_fence", () -> new OliveFenceBlock());
	public static final RegistryObject<Block> OLIVE_SLAB = REGISTRY.register("olive_slab", () -> new OliveSlabBlock());
	public static final RegistryObject<Block> OLIVE_STAIRS = REGISTRY.register("olive_stairs", () -> new OliveStairsBlock());
	public static final RegistryObject<Block> OLIVE_PRESSURE_PLATE = REGISTRY.register("olive_pressure_plate", () -> new OlivePressurePlateBlock());
	public static final RegistryObject<Block> OLIVE_BUTTON = REGISTRY.register("olive_button", () -> new OliveButtonBlock());
	public static final RegistryObject<Block> OLIVE_FENCE_GATE = REGISTRY.register("olive_fence_gate", () -> new OliveFenceGateBlock());
	public static final RegistryObject<Block> OLIVE_PILE = REGISTRY.register("olive_pile", () -> new OlivePileBlock());
	public static final RegistryObject<Block> MASHED_OLIVE = REGISTRY.register("mashed_olive", () -> new MashedOliveBlock());
	public static final RegistryObject<Block> STRIPPED_OLIVE_LOG = REGISTRY.register("stripped_olive_log", () -> new StrippedOliveLogBlock());
	public static final RegistryObject<Block> STRIPPED_OLIVE_WOOD = REGISTRY.register("stripped_olive_wood", () -> new StrippedOliveWoodBlock());
	// Start of user code block custom blocks
	// End of user code block custom blocks
}
