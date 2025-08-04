
package net.eris.reverie.init;

import io.netty.util.Attribute;
import net.eris.reverie.client.renderer.GoblinBarrelRenderer;
import net.eris.reverie.entity.*;
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
	public static final RegistryObject<EntityType<GoblinEntity>> GOBLIN = register("goblin",
			EntityType.Builder.<GoblinEntity>of(GoblinEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(GoblinEntity::new)
					.sized(0.6f, 1.8f));
	public static final RegistryObject<EntityType<ShooterGoblinEntity>> SHOOTER_GOBLIN = register("shooter_goblin",
			EntityType.Builder.<ShooterGoblinEntity>of(ShooterGoblinEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(ShooterGoblinEntity::new)
					.sized(0.6f, 1.8f));
	public static final RegistryObject<EntityType<BoneSpearProjectileEntity>> BONE_SPEAR_PROJECTILE = register("bone_spear",
			EntityType.Builder.<BoneSpearProjectileEntity>of(BoneSpearProjectileEntity::new, MobCategory.MISC).setCustomClientFactory(BoneSpearProjectileEntity::new).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));
	public static final RegistryObject<EntityType<BarrelGoblinEntity>> BARREL_GOBLIN = register("barrel_goblin",
			EntityType.Builder.<BarrelGoblinEntity>of(BarrelGoblinEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(BarrelGoblinEntity::new)
					.sized(0.6f, 1.8f));
	public static final RegistryObject<EntityType<GoblinBarrelEntity>> GOBLIN_BARREL = register("goblin_barrel",
			EntityType.Builder.<GoblinBarrelEntity>of(GoblinBarrelEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(GoblinBarrelEntity::new)
					.sized(2.1f, 1.1f));


	private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(registryname));
	}

	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			BrawlerEntity.init();
			DrunkardEntity.init();
			SpikedLogEntity.init();
			GoblinEntity.init();
			ShooterGoblinEntity.init();
			GoblinBarrelEntity.init();
		});
	}

	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(BRAWLER.get(), BrawlerEntity.createAttributes().build());
		event.put(DRUNKARD.get(), DrunkardEntity.createAttributes().build());
		event.put(SPIKED_LOG.get(), SpikedLogEntity.createAttributes().build());
		event.put(GOBLIN.get(), GoblinEntity.createAttributes().build());
		event.put(SHOOTER_GOBLIN.get(), ShooterGoblinEntity.createAttributes().build());
		event.put(BARREL_GOBLIN.get(), BarrelGoblinEntity.createAttributes().build());
		event.put(GOBLIN_BARREL.get(), GoblinBarrelEntity.createAttributes().build());
	}
}
