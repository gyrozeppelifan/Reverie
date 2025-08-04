package net.eris.reverie.handlers;

import net.eris.reverie.entity.BarrelGoblinEntity;
import net.eris.reverie.entity.GoblinEntity;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BarrelGoblinAttackHandler {
    private static final TagKey<EntityType<?>> GOBLINS_TAG =
            TagKey.create(
                    ForgeRegistries.ENTITY_TYPES.getRegistryKey(),
                    new ResourceLocation("reverie", "goblins")
            );

    @SubscribeEvent
    public static void onPlayerDealsDamage(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getSource().getEntity() instanceof Player)) return;

        Player player = (Player) event.getSource().getEntity();
        LivingEntity target = (LivingEntity) event.getEntity();

        // Hedef bir goblinse (tag içinde) hiç dokunma
        if (target.getType().is(GOBLINS_TAG)) return;

        // Şimdi sadece sahibin goblinlerine hedefi ata
        player.level().getEntitiesOfClass(
                        BarrelGoblinEntity.class,
                        player.getBoundingBox().inflate(16)
                ).stream()
                .filter(gob -> player.getUUID().equals(gob.getOwnerUUID()))
                .forEach(gob -> gob.setTarget(target));
    }
}
