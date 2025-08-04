package net.eris.reverie.entity;

import net.eris.reverie.entity.goal.DefendOwnerGoal;
import net.eris.reverie.entity.goal.FollowOwnerGoal;
import net.eris.reverie.entity.goal.OwnerAttackGoal;
import net.eris.reverie.init.ReverieModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;

import java.util.Optional;
import java.util.UUID;

public class BarrelGoblinEntity extends GoblinEntity {
    // --- owner sync: UUID + name ---
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(BarrelGoblinEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> OWNER_NAME =
            SynchedEntityData.defineId(BarrelGoblinEntity.class, EntityDataSerializers.STRING);

    // pending owner-attack
    private LivingEntity pendingOwnerAttackTarget;
    private long pendingOwnerAttackTick = -1;

    // auto-despawn logic
    private int noTargetTicks = 0;
    private static final int MAX_NO_TARGET_TICKS = 20 * 60; // 1 dakika

    public BarrelGoblinEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ReverieModEntities.BARREL_GOBLIN.get(), world);
    }

    public BarrelGoblinEntity(EntityType<? extends BarrelGoblinEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(OWNER_UUID, Optional.empty());
        this.entityData.define(OWNER_NAME, "");
    }

    /** Set both owner UUID and name at once */
    public void setOwner(Player player) {
        this.entityData.set(OWNER_UUID, Optional.of(player.getUUID()));
        this.entityData.set(OWNER_NAME, player.getGameProfile().getName());
    }

    /** Individually set owner UUID */
    public void setOwnerUUID(UUID ownerUUID) {
        this.entityData.set(OWNER_UUID, Optional.of(ownerUUID));
    }

    /** Individually set owner name */
    public void setOwnerName(String name) {
        this.entityData.set(OWNER_NAME, name);
    }

    /** Get the owner's UUID or null */
    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UUID).orElse(null);
    }

    /** Get the owner's name (may be empty) */
    public String getOwnerName() {
        return this.entityData.get(OWNER_NAME);
    }

    /**
     * Restore the original getOwner() so DefendOwnerGoal works.
     * Returns the Player instance for this goblin's owner, or null.
     */
    public Player getOwner() {
        UUID uuid = getOwnerUUID();
        if (uuid != null && this.level() instanceof ServerLevel server) {
            return server.getPlayerByUUID(uuid);
        }
        return null;
    }

    /** Pending-owner-attack helpers */
    public void notifyOwnerAttack(LivingEntity target, long tick) {
        this.pendingOwnerAttackTarget = target;
        this.pendingOwnerAttackTick = tick;
    }

    public LivingEntity consumePendingOwnerAttackTarget(long currentTick) {
        if (pendingOwnerAttackTarget != null && pendingOwnerAttackTick == currentTick) {
            LivingEntity t = pendingOwnerAttackTarget;
            pendingOwnerAttackTarget = null;
            return t;
        }
        return null;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.hasUUID("OwnerUUID")) {
            this.entityData.set(OWNER_UUID, Optional.of(tag.getUUID("OwnerUUID")));
        }
        if (tag.contains("OwnerName")) {
            this.entityData.set(OWNER_NAME, tag.getString("OwnerName"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        UUID uuid = getOwnerUUID();
        if (uuid != null) {
            tag.putUUID("OwnerUUID", uuid);
        }
        tag.putString("OwnerName", getOwnerName());
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // 1 dk boyunca canlı hedef yoksa puff + discard
            LivingEntity tgt = this.getTarget();
            boolean hasLive = tgt != null && tgt.isAlive();
            if (hasLive) {
                noTargetTicks = 0;
            } else if (++noTargetTicks >= MAX_NO_TARGET_TICKS) {
                ((ServerLevel)this.level()).sendParticles(
                        ParticleTypes.CLOUD,
                        this.getX(), this.getY() + 1.0, this.getZ(),
                        10, 0.2, 0.2, 0.2, 0.01
                );
                this.discard();
                return;
            }
        }
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new OwnerAttackGoal(this));
        this.targetSelector.addGoal(2, new DefendOwnerGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(6, new FloatGoal(this));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.45);
    }
}