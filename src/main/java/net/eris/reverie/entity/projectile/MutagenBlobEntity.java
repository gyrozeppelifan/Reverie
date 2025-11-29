package net.eris.reverie.entity.projectile;

import net.eris.reverie.init.ReverieModEntities;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

public class MutagenBlobEntity extends ThrowableItemProjectile {

    public MutagenBlobEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public MutagenBlobEntity(Level level, LivingEntity shooter) {
        super(ReverieModEntities.MUTAGEN_BLOB.get(), shooter, level);
    }

    public MutagenBlobEntity(Level level, double x, double y, double z) {
        super(ReverieModEntities.MUTAGEN_BLOB.get(), x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        // Varsayılan olarak Slime Ball (Balçık Topu) görünsün.
        // İstersen bunu kendi modundaki özel bir item ile değiştirebiliriz.
        return Items.SLIME_BALL;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        Entity shooter = this.getOwner();

        if (target instanceof LivingEntity livingTarget) {
            // Eğer topu atan bizsek veya mob bizim dostumuzsa (sahibine çarpıyorsa)
            boolean isFriendlyFire = shooter != null && (target == shooter || (shooter instanceof net.minecraft.world.entity.TamableAnimal tameable && tameable.isOwnedBy((LivingEntity) target)));

            if (isFriendlyFire) {
                // DOST ETKİSİ (BUFF): Güç ve Yenilenme ver
                livingTarget.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1)); // 10 sn Güç II
                livingTarget.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1)); // 5 sn Yenilenme II
            } else {
                // DÜŞMAN ETKİSİ (DEBUFF): Hasar ve Zehir ver
                livingTarget.hurt(this.damageSources().thrown(this, this.getOwner()), 8.0F); // 4 Kalp hasar
                livingTarget.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1)); // 5 sn Zehir II
                livingTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1)); // 5 sn Zayıflık II
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            // Çarpınca etrafa yeşil partiküller saçsın ve yok olsun
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            // Parçalanma efekti (Slime partikülleri)
            for (int i = 0; i < 8; ++i) {
                this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItem()), this.getX(), this.getY(), this.getZ(),
                        ((double) this.random.nextFloat() - 0.5D) * 0.08D, ((double) this.random.nextFloat() - 0.5D) * 0.08D, ((double) this.random.nextFloat() - 0.5D) * 0.08D);
            }
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}