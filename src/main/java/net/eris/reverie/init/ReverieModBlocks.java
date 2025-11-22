
package net.eris.reverie.init;

import net.eris.reverie.block.*;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Block;

import net.eris.reverie.ReverieMod;

public class ReverieModBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, ReverieMod.MODID);
	public static final RegistryObject<Block> GOLDEN_GRAVEL = REGISTRY.register("coin_pile_block",
			() -> new GoldenGravelBlock());
	public static final RegistryObject<Block> COIN_PILE = REGISTRY.register("coin_pile",
			() -> new CoinPileBlock());
	public static final RegistryObject<Block> HAY_THATCH = REGISTRY.register("hay_thatch", () -> new HayThatchBlock());
	public static final RegistryObject<Block> COINS = REGISTRY.register("coins", () -> new CoinsBlock());
	public static final RegistryObject<Block> WILD_TORCH = REGISTRY.register("wild_torch", () -> new WildTorchBlock());
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
	public static final RegistryObject<Block> COPPER_CONDUIT = REGISTRY.register("copper_conduit", () -> new CopperConduitBlock());
	public static final RegistryObject<Block> COPPER_JUNCTION = REGISTRY.register("copper_junction", () -> new CopperJunctionBlock());
	public static final RegistryObject<Block> ELDER_OLIVE_LEAVES = REGISTRY.register("elder_olive_leaves", () -> new ElderOliveLeavesBlock());
	public static final RegistryObject<Block> ELDER_OLIVE_LOG = REGISTRY.register("elder_olive_log", () -> new ElderOliveLogBlock());
	public static final RegistryObject<Block> SHINY_ELDER_OLIVE_LOG = REGISTRY.register("shiny_elder_olive_log", () -> new ElderOliveLogBlock());
	public static final RegistryObject<Block> ELDER_OLIVE_HEART = REGISTRY.register("elder_olive_heart", () -> new ElderOliveHeartBlock());
	public static final RegistryObject<Block> ELDER_OLIVE_LEFT_EYE = REGISTRY.register("elder_olive_left_eye", () -> new ElderOliveLeftEyeBlock());
	public static final RegistryObject<Block> ELDER_OLIVE_RIGHT_EYE = REGISTRY.register("elder_olive_right_eye", () -> new ElderOliveRightEyeBlock());
	public static final RegistryObject<Block> ELDER_OLIVE_MOUTH = REGISTRY.register("elder_olive_mouth", () -> new ElderOliveMouthBlock());
	public static final RegistryObject<Block> ELDER_OLIVE_NOSE_UP = REGISTRY.register("elder_olive_nose_up", () -> new ElderOliveNoseUpBlock());
	public static final RegistryObject<Block> ELDER_OLIVE_NOSE_DOWN = REGISTRY.register("elder_olive_nose_down", () -> new ElderOliveNoseDownBlock());
	public static final RegistryObject<Block> DEAD_LOG = REGISTRY.register("dead_log", () -> new DeadLogBlock());
	public static final RegistryObject<Block> ELDER_OLIVE_BLOCK = REGISTRY.register("elder_olive_block", () -> new ElderOliveBlock());
	public static final RegistryObject<Block> GREEN_OLIVE_BLOCK = REGISTRY.register("green_olive_block", () -> new GreenOliveBlock());
	public static final RegistryObject<Block> BLACK_OLIVE_BLOCK = REGISTRY.register("black_olive_block", () -> new BlackOliveBlock());
	public static final RegistryObject<Block> CARVED_ELDER_OLIVE_BLOCK = REGISTRY.register("carved_elder_olive_block", () -> new CarvedElderOliveBlock());
	public static final RegistryObject<Block> CARVED_BLACK_OLIVE_BLOCK = REGISTRY.register("carved_black_olive_block", () -> new CarvedBlackOliveBlock());
	public static final RegistryObject<Block> CARVED_GREEN_OLIVE_BLOCK = REGISTRY.register("carved_green_olive_block", () -> new CarvedGreenOliveBlock());
	public static final RegistryObject<Block> HANGING_HIDE = REGISTRY.register("hanging_hide", () -> new HangingHideBlock());
	public static final RegistryObject<Block> LEATHER_BLOCK = REGISTRY.register("leather_block", () -> new LeatherBlock());
	public static final RegistryObject<Block> LEATHER_PATCH_BLOCK = REGISTRY.register("leather_patch_block", () -> new LeatherPatchBlock());
	public static final RegistryObject<Block> RABBIT_HIDE_BLOCK = REGISTRY.register("rabbit_hide_patch_block", () -> new RabbitHideBlock());
	public static final RegistryObject<Block> LAYERED_PELT_BLOCK = REGISTRY.register("layered_pelt_block", () -> new LayeredPeltBlock());
	public static final RegistryObject<Block> STITCHED_LEATHER_TILES = REGISTRY.register("stitched_leather_tiles", () -> new StitchedLeatherTilesBlock());
	public static final RegistryObject<Block> PATCHED_LEATHER_TILES = REGISTRY.register("patched_leather_tiles", () -> new PatchedLeatherTilesBlock());
}
