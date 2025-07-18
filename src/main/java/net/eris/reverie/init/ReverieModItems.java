
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.eris.reverie.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.common.ForgeSpawnEggItem;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.BlockItem;

import net.eris.reverie.item.SpikedLogItemItem;
import net.eris.reverie.item.OlivePasteItem;
import net.eris.reverie.item.OliveOilBottleItem;
import net.eris.reverie.item.OliveBagelItem;
import net.eris.reverie.item.GreenOliveBranchItem;
import net.eris.reverie.item.BrokenBoozeBottleItem;
import net.eris.reverie.item.BoozeBottleItem;
import net.eris.reverie.item.BlackOliveBranchItem;
import net.eris.reverie.ReverieMod;

public class ReverieModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ReverieMod.MODID);
	public static final RegistryObject<Item> BRAWLER_SPAWN_EGG = REGISTRY.register("brawler_spawn_egg", () -> new ForgeSpawnEggItem(ReverieModEntities.BRAWLER, -1, -1, new Item.Properties()));
	public static final RegistryObject<Item> DRUNKARD_SPAWN_EGG = REGISTRY.register("drunkard_spawn_egg", () -> new ForgeSpawnEggItem(ReverieModEntities.DRUNKARD, -6710887, -26368, new Item.Properties()));
	public static final RegistryObject<Item> BOOZE_BOTTLE = REGISTRY.register("booze_bottle", () -> new BoozeBottleItem());
	public static final RegistryObject<Item> BROKEN_BOOZE_BOTTLE = REGISTRY.register("broken_booze_bottle", () -> new BrokenBoozeBottleItem());
	public static final RegistryObject<Item> HAY_THATCH = block(ReverieModBlocks.HAY_THATCH);
	public static final RegistryObject<Item> HAY_THATCH_STAIRS = block(ReverieModBlocks.HAY_THATCH_STAIRS);
	public static final RegistryObject<Item> HAY_THATCH_SLAB = block(ReverieModBlocks.HAY_THATCH_SLAB);
	public static final RegistryObject<Item> POINTY_BONE = block(ReverieModBlocks.POINTY_BONE);
	public static final RegistryObject<Item> OLIVE_LEAVES = block(ReverieModBlocks.OLIVE_LEAVES);
	public static final RegistryObject<Item> OLIVE_WOOD = block(ReverieModBlocks.OLIVE_WOOD);
	public static final RegistryObject<Item> OLIVE_BRANCHES = block(ReverieModBlocks.OLIVE_BRANCHES);
	public static final RegistryObject<Item> OLIVE_TRAPDOOR = block(ReverieModBlocks.OLIVE_TRAPDOOR);
	public static final RegistryObject<Item> OLIVE_DOOR = doubleBlock(ReverieModBlocks.OLIVE_DOOR);
	public static final RegistryObject<Item> OLIVE_LOG = block(ReverieModBlocks.OLIVE_LOG);
	public static final RegistryObject<Item> OLIVE_PLANKS = block(ReverieModBlocks.OLIVE_PLANKS);
	public static final RegistryObject<Item> OLIVE_SAPLING = block(ReverieModBlocks.OLIVE_SAPLING);
	public static final RegistryObject<Item> GREEN_OLIVE_BRANCH = REGISTRY.register("green_olive_branch", () -> new GreenOliveBranchItem());
	public static final RegistryObject<Item> BLACK_OLIVE_BRANCH = REGISTRY.register("black_olive_branch", () -> new BlackOliveBranchItem());
	public static final RegistryObject<Item> OLIVE_FENCE = block(ReverieModBlocks.OLIVE_FENCE);
	public static final RegistryObject<Item> OLIVE_SLAB = block(ReverieModBlocks.OLIVE_SLAB);
	public static final RegistryObject<Item> OLIVE_STAIRS = block(ReverieModBlocks.OLIVE_STAIRS);
	public static final RegistryObject<Item> OLIVE_PRESSURE_PLATE = block(ReverieModBlocks.OLIVE_PRESSURE_PLATE);
	public static final RegistryObject<Item> OLIVE_BUTTON = block(ReverieModBlocks.OLIVE_BUTTON);
	public static final RegistryObject<Item> OLIVE_FENCE_GATE = block(ReverieModBlocks.OLIVE_FENCE_GATE);
	public static final RegistryObject<Item> OLIVE_PILE = block(ReverieModBlocks.OLIVE_PILE);
	public static final RegistryObject<Item> MASHED_OLIVE = block(ReverieModBlocks.MASHED_OLIVE);
	public static final RegistryObject<Item> OLIVE_OIL_BOTTLE = REGISTRY.register("olive_oil_bottle", () -> new OliveOilBottleItem());
	public static final RegistryObject<Item> OLIVE_PASTE = REGISTRY.register("olive_paste", () -> new OlivePasteItem());
	public static final RegistryObject<Item> STRIPPED_OLIVE_LOG = block(ReverieModBlocks.STRIPPED_OLIVE_LOG);
	public static final RegistryObject<Item> STRIPPED_OLIVE_WOOD = block(ReverieModBlocks.STRIPPED_OLIVE_WOOD);
	public static final RegistryObject<Item> OLIVE_BAGEL = REGISTRY.register("olive_bagel", () -> new OliveBagelItem());
	public static final RegistryObject<Item> SPIKED_LOG_ITEM = REGISTRY.register("spiked_log_item", () -> new SpikedLogItemItem());

	// Start of user code block custom items
	// End of user code block custom items
	private static RegistryObject<Item> block(RegistryObject<Block> block) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
	}

	private static RegistryObject<Item> doubleBlock(RegistryObject<Block> block) {
		return REGISTRY.register(block.getId().getPath(), () -> new DoubleHighBlockItem(block.get(), new Item.Properties()));
	}
}
