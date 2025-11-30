package net.eris.reverie.entity.stitched_abilities;

import net.eris.reverie.entity.StitchedEntity;
import net.eris.reverie.init.ReverieModMobEffects;
import net.eris.reverie.init.ReverieModParticleTypes;
import net.eris.reverie.init.ReverieModSounds; // BUNU EKLEMEYİ UNUTMA!
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AbilityLightning implements StitchedAbility {

    @Override
    public void start(StitchedEntity entity) {
        // DEĞİŞİKLİK BURADA:
        // Creeper sesi yerine kendi özel sesimiz çalıyor.
        // 2.0f ses şiddeti, 1.0f pitch (normal hız)
        entity.playSound(ReverieModSounds.STITCHED_ZAP.get(), 2.0f, 1.0f);

        entity.setState(6); // Görsel mod: Tesla/Overdrive
    }

    // ... (Geri kalan tick, spawnElectricBeam, isFriendly, stop, canUse, getDuration kodları AYNEN KALSIN) ...
    @Override
    public boolean tick(StitchedEntity entity) {
        // 1. Merkez Efekti (Stitched'in kendisi)
        if (entity.level() instanceof ServerLevel serverLevel) {
            double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * 2.0;
            double y = entity.getY() + entity.getRandom().nextDouble() * 2.0;
            double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * 2.0;
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 1, 0, 0, 0, 0);
        }

        // 2. Çarpma Döngüsü
        if (entity.abilityTick % 5 == 0) {
            AABB area = entity.getBoundingBox().inflate(8.0);
            List<LivingEntity> enemies = entity.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != entity);

            int targetCount = 0;
            for (LivingEntity enemy : enemies) {
                if (isFriendly(entity, enemy)) continue;
                if (targetCount >= 3) break;

                enemy.addEffect(new MobEffectInstance(ReverieModMobEffects.ZAPPED.get(), 60, 0));
                spawnElectricBeam(entity, enemy);

                targetCount++;
            }

            if (targetCount > 0) {
                // Bu çarpma sesi (Impact) olarak kalabilir, gayet tok bir ses.
                entity.playSound(SoundEvents.LIGHTNING_BOLT_IMPACT, 0.5f, 1.5f + entity.getRandom().nextFloat() * 0.5f);
            }
        }

        return entity.abilityTick < getDuration();
    }

    private void spawnElectricBeam(StitchedEntity start, LivingEntity end) {
        if (!(start.level() instanceof ServerLevel serverLevel)) return;

        // Başlangıç: Stitched'in Göğsü
        Vec3 startPos = start.position().add(0, start.getEyeHeight() * 0.6, 0);
        // Bitiş: Düşmanın Göğsü
        Vec3 endPos = end.position().add(0, end.getEyeHeight() * 0.6, 0);

        // Vektör Hesabı (Bitiş - Başlangıç)
        Vec3 delta = endPos.subtract(startPos);

        // TEK BİR PARTİKÜL GÖNDERİYORUZ
        // Bu partikül X, Y, Z konumunda doğacak.
        // Hız (Velocity) değerlerini (delta.x, delta.y, delta.z) hedef vektör olarak kullanacak.
        // StitchedZapParticle class'ında bunu "Hedef Nokta" olarak kodladık.

        // Not: sendParticles metodu hızı (speed) de alır, burası önemli.
        // Parametreler: ParticleType, X, Y, Z, Count, DeltaX, DeltaY, DeltaZ, Speed
        // Speed'i 1 yapıyoruz ki bizim delta değerlerimizi bozmasın.
        serverLevel.sendParticles(ReverieModParticleTypes.STITCHED_ZAP_PARTICLE.get(),
                startPos.x, startPos.y, startPos.z,
                1, // Sayı
                delta.x, delta.y, delta.z, // Hız/Yön olarak hedef vektörü gönderiyoruz
                1.0); // Hız çarpanı
    }

    private boolean isFriendly(StitchedEntity entity, LivingEntity target) {
        if (entity.isOwnedBy(target)) return true;
        if (entity.getOwner() != null && entity.getOwner() == target) return true;
        if (target instanceof StitchedEntity) return true;
        if (target instanceof TamableAnimal otherPet && otherPet.getOwnerUUID() != null && otherPet.getOwnerUUID().equals(entity.getOwnerUUID())) {
            return true;
        }
        return false;
    }

    @Override
    public void stop(StitchedEntity entity) {
        entity.setState(3);
    }

    @Override
    public boolean canUse(StitchedEntity entity) {
        return true;
    }

    @Override
    public int getDuration() {
        return 100;
    }
}