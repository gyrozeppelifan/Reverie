package net.eris.reverie.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;

import net.eris.reverie.init.ReverieModItems;
import net.eris.reverie.init.ReverieModSounds;
import net.eris.reverie.procedures.SpikedLogPlaybackConditionProcedure;
import net.eris.reverie.init.ReverieModEntities;

import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpikedLogEntity extends PathfinderMob {
    private static final double SPEED = 0.2;
    private static final int ROLL_DURATION = 120; // 6 saniye

    private static final EntityDataAccessor<Boolean> DATA_IS_ROLLING =
        SynchedEntityData.defineId(SpikedLogEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState animationState0 = new AnimationState();
    public final AnimationState animationState1 = new AnimationState();

    // İlk vuruşta knockback alan entity'ler
    private final Set<Integer> hitEntities = new HashSet<>();

        // OWNER TAKİBİ
    private UUID ownerUUID = null;

    public void setOwner(Player player) {
        this.ownerUUID = player.getUUID();
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public SpikedLogEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ReverieModEntities.SPIKED_LOG.get(), world);
    }

    public SpikedLogEntity(EntityType<SpikedLogEntity> type, Level world) {
        super(type, world);
        setMaxUpStep(1.0f);
        xpReward = 0;
        setNoAi(false);
        setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_ROLLING, true);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override public MobType getMobType() { return MobType.UNDEFINED; }
    @Override public boolean removeWhenFarAway(double d) { return false; }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("IsRolling", this.entityData.get(DATA_IS_ROLLING));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("IsRolling")) {
            this.entityData.set(DATA_IS_ROLLING, nbt.getBoolean("IsRolling"));
        }
    }

    /** Public getter for renderer use */
    public boolean isRolling() {
        return this.entityData.get(DATA_IS_ROLLING);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        String msgId = source.getMsgId();
        if (msgId.contains("explosion") || msgId.contains("blast") || msgId.contains("lava") || msgId.equals("outOfWorld") || msgId.equals("genericKill")) {
            return super.hurt(source, amount);
        }

        // Sadece balta ile (her zaman), instant break
        if (!this.level().isClientSide() && source.getEntity() instanceof LivingEntity attacker) {
            ItemStack stack = attacker.getMainHandItem();
            if (stack.getItem() instanceof AxeItem) {
                ServerLevel server = (ServerLevel) this.level();
                server.sendParticles(
                    ParticleTypes.POOF,
                    this.getX(), this.getY() + 0.5, this.getZ(),
                    20, 0.2, 0.2, 0.2, 0.02
                );
                this.level().playSound(
                    null, this.getX(), this.getY(), this.getZ(),
                    ReverieModSounds.SPIKED_LOG_CRASH.get(),
                    SoundSource.BLOCKS, 1f, 1f
                );
                this.spawnAtLocation(ReverieModItems.SPIKED_LOG_ITEM.get(), 1);
                hitEntities.clear();
                this.discard();
                return true;
            }
        }

        // Diğer tüm saldırı türlerinde hiç hasar alma
        return false;
    }

    @Override
    public void die(DamageSource source) {
        this.level().playSound(
            null, this.getX(), this.getY(), this.getZ(),
            ReverieModSounds.SPIKED_LOG_CRASH.get(),
            SoundSource.BLOCKS, 1f, 1f
        );
            // ITEM DROP!
        this.spawnAtLocation(ReverieModItems.SPIKED_LOG_ITEM.get(), 1);
        hitEntities.clear();
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();

        // Gravity sadece sudayken kapalı, karada ve havada açık
        if (this.isInWater()) {
            this.setNoGravity(true);
        } else {
            this.setNoGravity(false);
        }

        // Rolling modundayken sesi her 0.5 sn'de bir (7 tickte bir) çal
        if (!this.level().isClientSide() && this.entityData.get(DATA_IS_ROLLING)) {
            if (this.tickCount % 7 == 0) {
                this.level().playSound(
                    null, this.getX(), this.getY(), this.getZ(),
                    ReverieModSounds.SPIKED_LOG_ROLL.get(),
                    SoundSource.BLOCKS, 1f, 1f
                );
            }
        }

        // SU İLE TEMAS: rolling hemen kapanır
        if (!this.level().isClientSide() && this.isInWater() && this.entityData.get(DATA_IS_ROLLING)) {
            this.entityData.set(DATA_IS_ROLLING, false);
            this.setDeltaMovement(Vec3.ZERO);
            hitEntities.clear();
        }

        // Süre bitince rolling'i kapat
        if (!this.level().isClientSide()
            && this.tickCount >= ROLL_DURATION
            && this.entityData.get(DATA_IS_ROLLING)) {
            this.entityData.set(DATA_IS_ROLLING, false);
            this.setDeltaMovement(Vec3.ZERO);
            hitEntities.clear();
        }

        // Rolling kapalıysa ileri hareketi sıfırla, gravity'ye karışma!
        if (!this.entityData.get(DATA_IS_ROLLING)) {
            if (!this.level().isClientSide()) {
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(0, motion.y, 0);
            }
        } else {
            // Sadece rolling'de ileri hareket
            if (!this.level().isClientSide()) {
                Vec3 oldMotion = this.getDeltaMovement();
                double currentY = oldMotion.y;

                double yawRad = Math.toRadians(-this.getYRot());
                Vec3 dirXZ = new Vec3(Math.sin(yawRad), 0, Math.cos(yawRad))
                    .normalize().scale(SPEED);

                Vec3 newMotion = new Vec3(dirXZ.x, currentY, dirXZ.z);
                this.setDeltaMovement(newMotion);
                this.hasImpulse = true;
                this.move(MoverType.SELF, newMotion);
            }
        }

        // Client-side: animasyon sadece isRolling == true
        if (this.level().isClientSide()) {
            if (this.entityData.get(DATA_IS_ROLLING)) {
                this.animationState0.animateWhen(true, this.tickCount);
                this.animationState1.animateWhen(
                    SpikedLogPlaybackConditionProcedure.execute(),
                    this.tickCount
                );
            }
            return;
        }

        // Duvara çarpınca sadece ses
        if (this.horizontalCollision) {
            this.level().playSound(
                null, this.blockPosition(),
                SoundEvents.WOOD_BREAK,
                SoundSource.BLOCKS, 1f, 1f
            );
        }

if (this.entityData.get(DATA_IS_ROLLING)) { // SADECE rolling modundayken etki uygula!
    for (LivingEntity target : this.level().getEntitiesOfClass(
            LivingEntity.class, this.getBoundingBox())) {
        if (target == this) continue;

        // *** OWNER'A VURMA! ***
        if (this.ownerUUID != null && target.getUUID().equals(this.ownerUUID)) continue;

        // Eğer log yanıyorsa, target'ı da yak!
        if (this.isOnFire()) {
            target.setSecondsOnFire(7);
        }

        // İlk temasta knockback uygula
        if (!hitEntities.contains(target.getId())) {
            hitEntities.add(target.getId());
            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            double dist = Math.hypot(dx, dz);
            if (dist > 0) {
                target.push(dx/dist * 2.0, 0.5, dz/dist * 2.0);
            }
        }

        // Her temasta slow ve hasar uygula!
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));

        DamageSource logSmash = new DamageSource(
            this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("reverie", "spikedlogsmash"))),
            this,
            (ownerUUID != null && this.level() instanceof ServerLevel server) ? server.getPlayerByUUID(ownerUUID) : null
        );

        if (target.getHealth() <= 6f) {
            target.hurt(logSmash, Float.MAX_VALUE);
        } else {
            target.hurt(logSmash, 12f);
        }
        break;
    }
}
}


@Override
public LivingEntity getKillCredit() {
    if (ownerUUID != null && this.level() instanceof ServerLevel server) {
        Player owner = server.getPlayerByUUID(ownerUUID);
        if (owner != null) return owner;
    }
    return super.getKillCredit();
}

@Override
public boolean canBeAffected(MobEffectInstance effect) {
    return false;
}


    @Override
    public boolean canBeCollidedWith() { return true; }
    @Override
    public boolean isPushable() { return false; }
    @Override
    public void push(Entity e) {}

    public static void init() {}
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.MAX_HEALTH, 3)
            .add(Attributes.ARMOR, 0)
            .add(Attributes.FOLLOW_RANGE, 0);
    }
}
