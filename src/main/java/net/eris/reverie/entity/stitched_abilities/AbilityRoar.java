package net.eris.reverie.entity.stitched_abilities;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.network.ScreenShakeS2CPacket;
import net.eris.reverie.entity.StitchedEntity;
import net.eris.reverie.entity.projectile.MutagenBlobEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

public class AbilityRoar implements StitchedAbility {

    @Override
    public void start(StitchedEntity entity) {
        // 1. SES VE EKRAN TİTREMESİ
        entity.playSound(SoundEvents.RAVAGER_ROAR, 1.5f, 0.8f);

        if (!entity.level().isClientSide) {
            try {
                ReverieMod.PACKET_HANDLER.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                        new ScreenShakeS2CPacket(40, 2.0f));
            } catch (Exception e) {
                System.out.println("ScreenShake paketi gönderilemedi: " + e.getMessage());
            }
        }

        // 2. ALAN ETKİSİ
        AABB area = entity.getBoundingBox().inflate(6.0);
        List<LivingEntity> enemies = entity.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != entity);

        for (LivingEntity enemy : enemies) {
            if (isFriendly(entity, enemy)) continue;

            enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
            enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));

            double dx = enemy.getX() - entity.getX();
            double dz = enemy.getZ() - entity.getZ();
            enemy.knockback(1.5D, -dx, -dz);
        }

        // 3. TOZ DUMAN PARTİKÜLLERİ (Start anında çıkanlar)
        if (entity.level() instanceof ServerLevel serverLevel) {
            double radius = 4.0;
            for (int i = 0; i < 360; i += 15) {
                double x = entity.getX() + radius * Math.cos(Math.toRadians(i));
                double z = entity.getZ() + radius * Math.sin(Math.toRadians(i));

                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, entity.getY() + 0.1, z, 1, 0.1, 0.1, 0.1, 0.05);

                if (i % 45 == 0) {
                    serverLevel.sendParticles(ParticleTypes.POOF, x, entity.getY(), z, 1, 0, 0, 0, 0);
                }
            }
        }

        // 4. MUTAJEN TOPUNU FIRLAT
        if (!entity.level().isClientSide) {
            LivingEntity target = entity.getOwner();
            if (target == null || entity.distanceToSqr(target) > 400) {
                target = entity.getTarget();
            }

            if (target != null) {
                MutagenBlobEntity blob = new MutagenBlobEntity(entity.level(), entity);

                // Topun çıkışını da ağız hizasına ayarlayalım
                Vec3 mouthPos = getMouthPosition(entity);
                blob.setPos(mouthPos.x, mouthPos.y, mouthPos.z);

                double d0 = target.getX() - entity.getX();
                double d1 = target.getEyeY() - mouthPos.y;
                double d2 = target.getZ() - entity.getZ();

                blob.shoot(d0, d1, d2, 0.5F, 1.0F);
                entity.level().addFreshEntity(blob);
            }
        }
    }

    @Override
    public boolean tick(StitchedEntity entity) {

        // --- YENİ: AĞIZDAN SAÇILAN SALYALAR (SNEEZE) ---
        // Her 3 tickte bir (çok yoğun olmasın diye) tükürük saçsın
        if (entity.abilityTick % 3 == 0 && entity.level() instanceof ServerLevel serverLevel) {

            // Ağız pozisyonunu ve bakış yönünü hesapla
            Vec3 mouthPos = getMouthPosition(entity);
            Vec3 lookDir = entity.getViewVector(1.0F); // Baktığı yön (vektör)

            // Partikülü fırlat
            // Konum: Ağız
            // Hız: Bakış yönüne doğru (lookDir.x, y, z) + biraz rastgelelik (0.1)
            serverLevel.sendParticles(ParticleTypes.SNEEZE,
                    mouthPos.x, mouthPos.y, mouthPos.z,
                    3, // Parçacık sayısı
                    lookDir.x * 0.2, lookDir.y * 0.2, lookDir.z * 0.2, // Yayılma alanı (Hız vektörü gibi çalışır SNEEZE'de)
                    0.1); // Hız çarpanı
        }

        return entity.abilityTick < getDuration();
    }

    // Yardımcı Metot: Ağzın tam koordinatını bulur
    private Vec3 getMouthPosition(StitchedEntity entity) {
        // Göz hizası
        Vec3 eyePos = entity.getEyePosition(1.0F);
        // Baktığı yön
        Vec3 lookDir = entity.getViewVector(1.0F);

        // Ağız genelde gözlerin 0.5 blok önünde ve 0.2 blok altındadır.
        // Bu vektör matematiğiyle tam orayı buluyoruz.
        return eyePos.add(lookDir.scale(0.6)).add(0, -0.3, 0);
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
    }

    @Override
    public boolean canUse(StitchedEntity entity) {
        return true;
    }

    @Override
    public int getDuration() {
        return 80;
    }
}