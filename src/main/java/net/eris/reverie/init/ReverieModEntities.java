
package net.eris.reverie.init;

import io.netty.util.Attribute;
import net.eris.reverie.client.renderer.GoblinBarrelRenderer;
import net.eris.reverie.entity.*;
import net.eris.reverie.entity.projectile.MagicArrow;
import net.eris.reverie.util.GoblinReputation;
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
					.sized(2.1f, 2.1f));
	public static final RegistryObject<EntityType<GobletEntity>> GOBLET = register("goblet",
			EntityType.Builder.<GobletEntity>of(GobletEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(GobletEntity::new)
					.sized(0.6f, 0.8f));
	public static final RegistryObject<EntityType<GoblinBruteEntity>> GOBLIN_BRUTE = register("goblin_brute",
			EntityType.Builder.<GoblinBruteEntity>of(GoblinBruteEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(GoblinBruteEntity::new)
					.sized(1.2f, 3.1f));
	public static final RegistryObject<EntityType<StitchedEntity>> STITCHED = register("stitched",
			EntityType.Builder.<StitchedEntity>of(StitchedEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(StitchedEntity::new)
					.sized(1.2f, 3.1f));
	public static final RegistryObject<EntityType<GoblinFlagEntity>> GOBLIN_FLAG = register("goblin_flag",
			EntityType.Builder.<GoblinFlagEntity>of(GoblinFlagEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(GoblinFlagEntity::new)
					.sized(1.2f, 3.1f));
	public static final RegistryObject<EntityType<MagicArrow>> MAGIC_ARROW = REGISTRY.register("magic_arrow",
			() -> EntityType.Builder.<MagicArrow>of(MagicArrow::new, net.minecraft.world.entity.MobCategory.MISC)
					.sized(0.5F, 0.5F)
					.clientTrackingRange(4)
					.updateInterval(20)
					.build("magic_arrow"));
	public static final RegistryObject<EntityType<PossessionPuppetEntity>> POSSESSION_PUPPET = register("possession_puppet",
			EntityType.Builder.<PossessionPuppetEntity>of(PossessionPuppetEntity::new, MobCategory.MISC)
					.setShouldReceiveVelocityUpdates(false) // gereksiz trafik yok
					.setTrackingRange(32)
					.setUpdateInterval(3)
					.setCustomClientFactory(PossessionPuppetEntity::new)
					.sized(0.6f, 1.8f)          // oyuncu ölçüsü → doğru path
					.noSummon()                 // yumurta vb. ile çağrılmasın


	);




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
			GobletEntity.init();
			GoblinBruteEntity.init();
			GoblinFlagEntity.init();
			PossessionPuppetEntity.init();

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
		event.put(GOBLET.get(), GobletEntity.createAttributes().build());
		event.put(GOBLIN_BRUTE.get(),  GoblinBruteEntity.createAttributes().build());
		event.put(STITCHED.get(),  GoblinBruteEntity.createAttributes().build());
		event.put(POSSESSION_PUPPET.get(), PossessionPuppetEntity.createAttributes().build());
	}
}
