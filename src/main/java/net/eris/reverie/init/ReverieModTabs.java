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

						// Hay Thatch & Bone & Copper
						List<Item> miscBlocks = List.of(
								ReverieModBlocks.COPPER_CONDUIT.get().asItem(),
								ReverieModBlocks.COPPER_JUNCTION.get().asItem(),
								ReverieModBlocks.HAY_THATCH.get().asItem(),
								ReverieModBlocks.HAY_THATCH_STAIRS.get().asItem(),
								ReverieModBlocks.HAY_THATCH_SLAB.get().asItem(),
								ReverieModBlocks.POINTY_BONE.get().asItem()
						);
						miscBlocks.forEach(item -> output.accept(new ItemStack(item)));

						// Treasure & Decoration (Olive related blocks removed)
						List<Item> treasureAndDecor = List.of(
								ReverieModBlocks.COIN_PILE.get().asItem(),
								ReverieModBlocks.GOLDEN_GRAVEL.get().asItem(),
								ReverieModBlocks.COINS.get().asItem(),
								ReverieModBlocks.WILD_TORCH.get().asItem()

						);
						treasureAndDecor.forEach(item -> output.accept(new ItemStack(item)));

						// Leather & Hides (Olive derived blocks removed)
						List<Item> hideBlocks = List.of(
								ReverieModBlocks.LEATHER_BLOCK.get().asItem(),
								ReverieModBlocks.LEATHER_PATCH_BLOCK.get().asItem(),
								ReverieModBlocks.LAYERED_PELT_BLOCK.get().asItem(),
								ReverieModBlocks.STITCHED_LEATHER_TILES.get().asItem(),
								ReverieModBlocks.PATCHED_LEATHER_TILES.get().asItem(),
								ReverieModBlocks.HANGING_HIDE.get().asItem()
						);
						hideBlocks.forEach(item -> output.accept(new ItemStack(item)));

						// Weapons & Tools (Olive Staff removed)
						output.accept(new ItemStack(ReverieModItems.SPIKED_LOG_ITEM.get()));
						output.accept(new ItemStack(ReverieModItems.BONE_SPEAR.get()));
						output.accept(new ItemStack(ReverieModItems.GOBLIN_SYMBOL_BANNER_PATTERN.get()));

						// Spawn Eggs
						List<Item> spawnEggs = List.of(
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