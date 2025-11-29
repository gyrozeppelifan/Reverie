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
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

public class MutagenBlobEntity extends ThrowableItemProjectile {

    private boolean landed = false;
    private int groundTick = 0;

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
        return Items.SLIME_BALL;
    }

    // --- 1. DÜŞMANA VURUNCA (Hasar Var, Zehir Yok) ---
    @Override
    protected void onHitEntity(EntityHitResult result) {
        // Eğer yerde bekliyorsa kimseye vurmasın (sadece temas ile alınsın)
        if (this.landed) return;

        super.onHitEntity(result);
        Entity target = result.getEntity();
        Entity shooter = this.getOwner();

        if (target instanceof LivingEntity livingTarget) {
            if (isFriendly(livingTarget, shooter)) {
                applyBuffs(livingTarget); // Dost ise güçlendir
                this.discard();
            } else {
                // Düşman ise SADECE hasar vur (Zehir kaldırıldı)
                livingTarget.hurt(this.damageSources().thrown(this, this.getOwner()), 6.0F);
                this.discard();
            }
        }
    }

    // --- 2. YERE ÇARPINCA (Yok Olma, Bekle) ---
    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        // Hareketi durdur ve bekleme moduna geç
        this.landed = true;
        this.setDeltaMovement(0, 0, 0);
        this.setNoGravity(true); // Yerçekimini kapat ki kaymasın
    }

    // --- 3. HER SANİYE KONTROL (Yerdeyken) ---
    @Override
    public void tick() {
        super.tick();

        // --- PARTICLE TRAIL (KUYRUK) ---
        // Havadayken arkasından yeşil gaz/sümük çıkarsın
        if (!this.onGround() && this.level().isClientSide) {
            // DRAGON_BREATH (Mor/Pembe) veya SNEEZE (Yeşil) güzel durur. Mutajen için SNEEZE (Yeşil) seçtim.
            // Arkasından çıksın diye (-getDeltaMovement) ekliyoruz.
            double d0 = this.getX() + (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.getBbWidth() * 0.5D;
            double d1 = this.getY() + 0.05D + (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.getBbHeight() * 0.5D;
            double d2 = this.getZ() + (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.getBbWidth() * 0.5D;

            this.level().addParticle(ParticleTypes.SNEEZE, d0, d1, d2, 0, 0, 0);

            // Arada bir balçık damlası da düşsün
            if (this.random.nextFloat() < 0.3f) {
                this.level().addParticle(ParticleTypes.ITEM_SLIME, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            }
        }

        // --- YERDE BEKLEME MANTIĞI (Önceki kodun aynısı) ---
        if (this.landed && !this.level().isClientSide) {
            if (++groundTick > 100) {
                this.level().broadcastEntityEvent(this, (byte) 3);
                this.discard();
                return;
            }

            java.util.List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.5));
            Entity shooter = this.getOwner();

            for (LivingEntity entity : nearbyEntities) {
                if (isFriendly(entity, shooter)) {
                    applyBuffs(entity);
                    this.level().broadcastEntityEvent(this, (byte) 3);
                    this.discard();
                    break;
                }
            }
        }
    }

    // Yardımcı Metot: Bu kişi dost mu?
    private boolean isFriendly(LivingEntity target, Entity shooter) {
        if (shooter == null) return false;
        if (target == shooter) return true; // Atan kişi (biz)
        if (shooter instanceof TamableAnimal tameable) {
            // Mobun sahibi mi?
            return tameable.isOwnedBy(target) || (tameable.getOwnerUUID() != null && tameable.getOwnerUUID().equals(target.getUUID()));
        }
        return false;
    }

    // Yardımcı Metot: Buffları ver
    private void applyBuffs(LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1)); // Güç II
        target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1)); // Yenilenme II
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
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