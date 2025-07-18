
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.eris.reverie.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;

import net.eris.reverie.entity.SpikedLogEntity;
import net.eris.reverie.entity.DrunkardEntity;
import net.eris.reverie.entity.BrawlerEntity;
import net.eris.reverie.ReverieMod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ReverieModEntities {
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ReverieMod.MODID);
	public static final RegistryObject<EntityType<BrawlerEntity>> BRAWLER = register("brawler",
			EntityType.Builder.<BrawlerEntity>of(BrawlerEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(BrawlerEntity::new)

					.sized(1f, 2.6f));
	public static final RegistryObject<EntityType<DrunkardEntity>> DRUNKARD = register("drunkard",
			EntityType.Builder.<DrunkardEntity>of(DrunkardEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(DrunkardEntity::new)

					.sized(0.6f, 1.8f));
	public static final RegistryObject<EntityType<SpikedLogEntity>> SPIKED_LOG = register("spiked_log",
			EntityType.Builder.<SpikedLogEntity>of(SpikedLogEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(SpikedLogEntity::new)

					.sized(2.1f, 1.1f));

	// Start of user code block custom entities
	// End of user code block custom entities
	private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(registryname));
	}

	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			BrawlerEntity.init();
			DrunkardEntity.init();
			SpikedLogEntity.init();
		});
	}

	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(BRAWLER.get(), BrawlerEntity.createAttributes().build());
		event.put(DRUNKARD.get(), DrunkardEntity.createAttributes().build());
		event.put(SPIKED_LOG.get(), SpikedLogEntity.createAttributes().build());
	}
}
