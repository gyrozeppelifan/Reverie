package net.eris.reverie.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.eris.reverie.util.GoblinReputation;
import net.minecraft.core.particles.ParticleTypes;

public class GoblinRepEvent {

    @SubscribeEvent
    public static void onGoblinHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (GoblinReputation.isGoblin(entity)) { // Tüm goblin türleri için çalışır!
            if (event.getSource().getEntity() instanceof Player player) {
                GoblinReputation.add(player, -10);
                if (!entity.level().isClientSide) {
                    ((ServerLevel) entity.level()).sendParticles(
                            ParticleTypes.ANGRY_VILLAGER,
                            entity.getX(),
                            entity.getY() + entity.getBbHeight(),
                            entity.getZ(),
                            1, 0.0, 0.0, 0.0, 0.0
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public static void onGoblinKilled(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (GoblinReputation.isGoblin(entity)) {
            if (event.getSource().getEntity() instanceof Player player) {
                GoblinReputation.add(player, -20);
            }
        }
    }
}
