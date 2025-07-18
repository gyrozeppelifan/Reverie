
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.eris.reverie.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import net.eris.reverie.ReverieMod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ReverieModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ReverieMod.MODID);
	public static final RegistryObject<CreativeModeTab> REVERIE = REGISTRY.register("reverie",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.reverie.reverie")).icon(() -> new ItemStack(ReverieModItems.BOOZE_BOTTLE.get())).displayItems((parameters, tabData) -> {
				tabData.accept(ReverieModItems.BOOZE_BOTTLE.get());
				tabData.accept(ReverieModItems.BROKEN_BOOZE_BOTTLE.get());
				tabData.accept(ReverieModBlocks.HAY_THATCH.get().asItem());
				tabData.accept(ReverieModBlocks.HAY_THATCH_STAIRS.get().asItem());
				tabData.accept(ReverieModBlocks.HAY_THATCH_SLAB.get().asItem());
				tabData.accept(ReverieModBlocks.POINTY_BONE.get().asItem());
				tabData.accept(ReverieModBlocks.OLIVE_LEAVES.get().asItem());
				tabData.accept(ReverieModBlocks.OLIVE_WOOD.get().asItem());
				tabData.accept(ReverieModBlocks.OLIVE_BRANCHES.get().asItem());
				tabData.accept(ReverieModBlocks.OLIVE_TRAPDOOR.get().asItem());
				tabData.accept(ReverieModBlocks.OLIVE_DOOR.get().asItem());
				tabData.accept(ReverieModBlocks.OLIVE_LOG.get().asItem());
				tabData.accept(ReverieModBlocks.OLIVE_PLANKS.get().asItem());
				tabData.accept(ReverieModBlocks.OLIVE_SAPLING.get().asItem());
				tabData.accept(ReverieModItems.GREEN_OLIVE_BRANCH.get());
				tabData.accept(ReverieModItems.BLACK_OLIVE_BRANCH.get());
				tabData.accept(ReverieModBlocks.OLIVE_FENCE.get().asItem());
				tabData.accept(ReverieModBlocks.OLIVE_SLAB.get().asItem());
				tabData.accept(ReverieModBlocks.OLIVE_STAIRS.get().asItem());
				tabData.accept(ReverieModBlocks.OLIVE_PRESSURE_PLATE.get().asItem());
				tabData.accept(ReverieModBlocks.OLIVE_BUTTON.get().asItem());
				tabData.accept(ReverieModBlocks.OLIVE_FENCE_GATE.get().asItem());
				tabData.accept(ReverieModBlocks.OLIVE_PILE.get().asItem());
				tabData.accept(ReverieModBlocks.MASHED_OLIVE.get().asItem());
				tabData.accept(ReverieModItems.OLIVE_OIL_BOTTLE.get());
				tabData.accept(ReverieModItems.OLIVE_PASTE.get());
				tabData.accept(ReverieModBlocks.STRIPPED_OLIVE_LOG.get().asItem());
				tabData.accept(ReverieModBlocks.STRIPPED_OLIVE_WOOD.get().asItem());
				tabData.accept(ReverieModItems.OLIVE_BAGEL.get());
				tabData.accept(ReverieModItems.SPIKED_LOG_ITEM.get());
			}).build());

	@SubscribeEvent
	public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
		if (tabData.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
			tabData.accept(ReverieModItems.BRAWLER_SPAWN_EGG.get());
			tabData.accept(ReverieModItems.DRUNKARD_SPAWN_EGG.get());
		}
	}
}
