package net.eris.reverie.entity.stitched_abilities;

import net.eris.reverie.entity.StitchedEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class AbilityRoar implements StitchedAbility {

    @Override
    public void start(StitchedEntity entity) {
        // 1. Ses ve Animasyon
        // Buraya kükreme animasyonunu tetikleyecek kodu ekleyeceğiz ileride
        // entity.triggerAnim("controller", "roar");
        entity.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.5f, 0.5f); // Geçici ses (Metalik kükreme)

        // 2. Alan Etkisi (Düşmanları İtme)
        AABB area = entity.getBoundingBox().inflate(5.0); // 5 blok etraf
        List<LivingEntity> enemies = entity.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != entity && !(e instanceof net.minecraft.world.entity.player.Player));

        for (LivingEntity enemy : enemies) {
            // Geri itme
            double dx = enemy.getX() - entity.getX();
            double dz = enemy.getZ() - entity.getZ();
            enemy.knockback(1.5, -dx, -dz);
            // Yavaşlatma
            enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
        }

        // 3. Mutajen Topunu Fırlat (Buff)
        // TODO: Buraya 'MutagenBlobEntity' fırlatma kodunu ekleyeceğiz.
        // Şimdilik oyuncuya chatten mesaj atalım çalıştığını görmek için.
        if (entity.level().isClientSide) return;
        System.out.println("Stitched: MUAAGH! (Mutajen fırlatıldı)");
    }

    @Override
    public boolean tick(StitchedEntity entity) {
        // Bu yetenek anlık bir etki, o yüzden tick'te bir şey yapmasına gerek yok.
        // Ama animasyon bitene kadar entity'yi kilitlemek istersek burayı kullanırız.
        return entity.abilityTick < getDuration();
    }

    @Override
    public void stop(StitchedEntity entity) {
        // Temizlik yok
    }

    @Override
    public boolean canUse(StitchedEntity entity) {
        // Şimdilik her zaman kullanılabilir (Cooldown'ı Entity classında yöneteceğiz)
        return true;
    }

    @Override
    public int getDuration() {
        return 20; // 1 Saniye sürsün (Animasyon süresi kadar)
    }
}