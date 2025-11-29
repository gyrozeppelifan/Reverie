package net.eris.reverie.entity.stitched_abilities;

import net.eris.reverie.entity.StitchedEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;

public class AbilityLightning implements StitchedAbility {

    @Override
    public void start(StitchedEntity entity) {
        // Elektrik sesi
        entity.playSound(net.minecraft.sounds.SoundEvents.TRIDENT_THUNDER, 2.0f, 1.0f);
    }

    @Override
    public boolean tick(StitchedEntity entity) {
        // Yetenek başladıktan yarım saniye sonra (10. tick) yıldırımı çak
        if (entity.abilityTick == 10 && !entity.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) entity.level();
            LivingEntity target = entity.getTarget();

            if (target != null) {
                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);
                if (lightning != null) {
                    lightning.moveTo(target.position()); // Hedefin tepesine
                    serverLevel.addFreshEntity(lightning);
                }
            }
        }
        return entity.abilityTick < getDuration();
    }

    @Override
    public void stop(StitchedEntity entity) {}

    @Override
    public boolean canUse(StitchedEntity entity) {
        // Sadece bir hedefi varsa çalıştır
        return entity.getTarget() != null;
    }

    @Override
    public int getDuration() {
        return 30; // 1.5 saniye
    }
}