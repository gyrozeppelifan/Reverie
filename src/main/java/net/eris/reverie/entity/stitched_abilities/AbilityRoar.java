package net.eris.reverie.entity.stitched_abilities;

import net.eris.reverie.entity.StitchedEntity;
import net.eris.reverie.entity.projectile.MutagenBlobEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class AbilityRoar implements StitchedAbility {

    @Override
    public void start(StitchedEntity entity) {
        // 1. Ses ve Efekt
        // Kükreme sesi (Metalik ve korkutucu)
        entity.playSound(SoundEvents.RAVAGER_ROAR, 1.5f, 0.8f);

        // 2. Alan Etkisi (Debuff: Zayıflık ve Yavaşlatma)
        // 6 blok yarıçapındaki herkesi etkile
        AABB area = entity.getBoundingBox().inflate(6.0);
        List<LivingEntity> enemies = entity.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != entity && !(e instanceof net.minecraft.world.entity.player.Player));

        for (LivingEntity enemy : enemies) {
            // Eğer mob evcilleştirilmişse ve bu düşman sahibi veya sahibinin dostu ise pas geç
            if (entity.isOwnedBy(enemy)) continue;

            // DÜZELTME BURADA: Geri itme yerine Zayıflık ve Slowness
            enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1)); // 10 Saniye Zayıflık II
            enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2)); // 5 Saniye Slowness III
        }

        // 3. Mutajen Topunu Sahibine Fırlat (Buff için)
        if (!entity.level().isClientSide) {
            LivingEntity target = entity.getOwner(); // Hedef öncelikle SAHİBİMİZ

            // Eğer sahibi yoksa veya çok uzaktaysa, normal hedefine atsın (Zehirlesin)
            if (target == null || entity.distanceToSqr(target) > 400) {
                target = entity.getTarget();
            }

            if (target != null) {
                MutagenBlobEntity blob = new MutagenBlobEntity(entity.level(), entity);

                // Topun çıkış noktası: Ağız hizası
                double spawnY = entity.getEyeY() - 0.3;
                blob.setPos(entity.getX(), spawnY, entity.getZ());

                // Hedefe doğru nişan al
                double d0 = target.getX() - entity.getX();
                double d1 = target.getEyeY() - spawnY;
                double d2 = target.getZ() - entity.getZ();

                // Fırlat! (Hız: 1.5, Sapma: 1.0)
                // Bu top sana çarpınca MutagenBlobEntity içindeki kod çalışacak ve buff verecek.
                blob.shoot(d0, d1, d2, 1.5F, 1.0F);
                entity.level().addFreshEntity(blob);
            }
        }
    }

    @Override
    public boolean tick(StitchedEntity entity) {
        return entity.abilityTick < getDuration();
    }

    @Override
    public void stop(StitchedEntity entity) {
    }

    @Override
    public boolean canUse(StitchedEntity entity) {
        return true;
    }

    @Override
    public int getDuration() {
        return 20; // 1 saniye (Animasyon süresi kadar)
    }
}