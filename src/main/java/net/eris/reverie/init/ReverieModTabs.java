
package net.eris.reverie.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.init.ReverieModItems;
import net.eris.reverie.init.ReverieModBlocks;

import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ReverieModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY =
			DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ReverieMod.MODID);

	public static final RegistryObject<CreativeModeTab> REVERIE = REGISTRY.register("reverie",
			() -> CreativeModeTab.builder()
					.title(Component.translatable("item_group.reverie.reverie"))
					.icon(() -> new ItemStack(ReverieModItems.BOOZE_BOTTLE.get()))
					.displayItems((parameters, output) -> {
						// Booze Items
						output.accept(new ItemStack(ReverieModItems.BOOZE_BOTTLE.get()));
						output.accept(new ItemStack(ReverieModItems.BROKEN_BOOZE_BOTTLE.get()));
						output.accept(new ItemStack(ReverieModItems.GOBLIN_FLAG.get()));
						output.accept(new ItemStack(ReverieModItems.ANCIENT_CROSSBOW.get()));



						// Hay Thatch & Bone
						List<Item> hayItems = List.of(
								ReverieModBlocks.COPPER_CONDUIT.get().asItem(),
								ReverieModBlocks.COPPER_JUNCTION.get().asItem(),
								ReverieModBlocks.HAY_THATCH.get().asItem(),
								ReverieModBlocks.HAY_THATCH_STAIRS.get().asItem(),
								ReverieModBlocks.HAY_THATCH_SLAB.get().asItem(),
								ReverieModBlocks.POINTY_BONE.get().asItem()
						);
						hayItems.forEach(item -> output.accept(new ItemStack(item)));

						// Olive Blocks & Sapling
						List<Item> oliveBlocks = List.of(
								ReverieModBlocks.OLIVE_LEAVES.get().asItem(),
								ReverieModBlocks.OLIVE_LOG.get().asItem(),
								ReverieModBlocks.OLIVE_WOOD.get().asItem(),
								ReverieModBlocks.STRIPPED_OLIVE_LOG.get().asItem(),
								ReverieModBlocks.STRIPPED_OLIVE_WOOD.get().asItem(),
								ReverieModBlocks.OLIVE_TRAPDOOR.get().asItem(),
								ReverieModBlocks.OLIVE_DOOR.get().asItem(),
								ReverieModBlocks.OLIVE_PLANKS.get().asItem(),
								ReverieModBlocks.OLIVE_SAPLING.get().asItem(),
								ReverieModBlocks.ELDER_OLIVE_LEAVES.get().asItem(),
								ReverieModBlocks.ELDER_OLIVE_LOG.get().asItem(),
								ReverieModBlocks.SHINY_ELDER_OLIVE_LOG.get().asItem(),
								ReverieModBlocks.ELDER_OLIVE_BLOCK.get().asItem(),
								ReverieModBlocks.GREEN_OLIVE_BLOCK.get().asItem(),
								ReverieModBlocks.BLACK_OLIVE_BLOCK.get().asItem(),
								ReverieModBlocks.CARVED_ELDER_OLIVE_BLOCK.get().asItem(),
								ReverieModBlocks.CARVED_GREEN_OLIVE_BLOCK.get().asItem(),
								ReverieModBlocks.CARVED_BLACK_OLIVE_BLOCK.get().asItem(),
								ReverieModBlocks.COIN_PILE.get().asItem(),
								ReverieModBlocks.GOLDEN_GRAVEL.get().asItem()
						);
						oliveBlocks.forEach(item -> output.accept(new ItemStack(item)));

						// Olive Items & Derived Blocks
						output.accept(new ItemStack(ReverieModItems.GREEN_OLIVE_BRANCH.get()));
						output.accept(new ItemStack(ReverieModItems.BLACK_OLIVE_BRANCH.get()));
						List<Item> oliveDerived = List.of(
								ReverieModBlocks.OLIVE_FENCE.get().asItem(),
								ReverieModBlocks.OLIVE_SLAB.get().asItem(),
								ReverieModBlocks.OLIVE_STAIRS.get().asItem(),
								ReverieModBlocks.OLIVE_PRESSURE_PLATE.get().asItem(),
								ReverieModBlocks.OLIVE_BUTTON.get().asItem(),
								ReverieModBlocks.OLIVE_FENCE_GATE.get().asItem(),
								ReverieModBlocks.OLIVE_PILE.get().asItem(),
								ReverieModBlocks.MASHED_OLIVE.get().asItem(),
								ReverieModBlocks.LEATHER_BLOCK.get().asItem(),
								ReverieModBlocks.LEATHER_PATCH_BLOCK.get().asItem(),
								ReverieModBlocks.RABBIT_HIDE_BLOCK.get().asItem(),
								ReverieModBlocks.LAYERED_PELT_BLOCK.get().asItem(),
								ReverieModBlocks.STITCHED_LEATHER_TILES.get().asItem(),
								ReverieModBlocks.PATCHED_LEATHER_TILES.get().asItem(),
								ReverieModBlocks.HANGING_HIDE.get().asItem()

						);
						oliveDerived.forEach(item -> output.accept(new ItemStack(item)));
						output.accept(new ItemStack(ReverieModItems.OLIVE_OIL_BOTTLE.get()));
						output.accept(new ItemStack(ReverieModItems.OLIVE_PASTE.get()));
						output.accept(new ItemStack(ReverieModItems.OLIVE_BAGEL.get()));
						output.accept(new ItemStack(ReverieModItems.SPIKED_LOG_ITEM.get()));
						output.accept(new ItemStack(ReverieModItems.BONE_SPEAR.get()));
						output.accept(new ItemStack(ReverieModItems.ELDER_OLIVE_STAFF.get()));
						output.accept(new ItemStack(ReverieModItems.GOBLIN_SYMBOL_BANNER_PATTERN.get()));

						// Spawn Eggs
						List<Item> spawnEggs = List.of(
								ReverieModItems.BRAWLER_SPAWN_EGG.get(),
								ReverieModItems.DRUNKARD_SPAWN_EGG.get(),
								ReverieModItems.GOBLIN_SPAWN_EGG.get(),
								ReverieModItems.SHOOTER_GOBLIN_SPAWN_EGG.get(),
								ReverieModItems.GOBLIN_BRUTE_SPAWN_EGG.get()
						);
						spawnEggs.forEach(egg -> output.accept(new ItemStack(egg)));
					})
					.build()
	);

	@SubscribeEvent
	public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
			event.accept(ReverieModItems.BRAWLER_SPAWN_EGG.get());
			event.accept(ReverieModItems.DRUNKARD_SPAWN_EGG.get());
		}
	}
}
