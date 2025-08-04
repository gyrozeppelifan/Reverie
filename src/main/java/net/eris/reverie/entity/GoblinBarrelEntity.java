package net.eris.reverie.entity;

import net.eris.reverie.init.ReverieModEntities;
import net.eris.reverie.init.ReverieModParticleTypes;
import net.eris.reverie.init.ReverieModSounds;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import java.util.Set;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class GoblinBarrelEntity extends PathfinderMob {
    // — ownerName senkronizasyonu —
    private static final EntityDataAccessor<String> DATA_OWNER_NAME =
            SynchedEntityData.defineId(GoblinBarrelEntity.class, EntityDataSerializers.STRING);

    // Beta tester nick’leri (hepsi lowercase)
    private static final Set<String> NERD_TESTERS = Set.of(
            "orenburglu",
            "steve"
            // … diğer tester nick’leri
    );

    private int state = 0, timer = 0;
    public final AnimationState fallState         = new AnimationState();
    public final AnimationState hitState          = new AnimationState();
    public final AnimationState shakeState        = new AnimationState();
    public final AnimationState preexplosionState = new AnimationState();

    private UUID ownerUUID;

    // -- CLIENT-SIDE düşme detektörü --
    private boolean wasOnGroundClient = false;
    // -- SERVER-SIDE ses flag’ları --
    private boolean playedGroundHit = false;
    private boolean playedBreakSound = false;

    public GoblinBarrelEntity(EntityType<? extends GoblinBarrelEntity> type, Level world) {
        super(type, world);
        this.setNoAi(true);
    }

    public GoblinBarrelEntity(Level world, double x, double y, double z, UUID ownerUUID) {
        this(ReverieModEntities.GOBLIN_BARREL.get(), world);
        this.setPos(x, y, z);
        this.ownerUUID = ownerUUID;
    }

    public GoblinBarrelEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ReverieModEntities.GOBLIN_BARREL.get(), world);
    }

    public static void init() {}

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_OWNER_NAME, "");
    }

    /** ownerName setter/getter **/
    public void setOwnerName(String name) {
        this.entityData.set(DATA_OWNER_NAME, name);
    }
    public String getOwnerName() {
        return this.entityData.get(DATA_OWNER_NAME);
    }

    @Override
    protected void registerGoals() {}

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
        if (tag.contains("OwnerName")) {
            this.entityData.set(DATA_OWNER_NAME, tag.getString("OwnerName"));
        }
        this.state = tag.getInt("State");
        this.timer = tag.getInt("Timer");
        this.playedGroundHit = tag.getBoolean("PlayedGroundHit");
        this.playedBreakSound = tag.getBoolean("PlayedBreakSound");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
        tag.putString("OwnerName", getOwnerName());
        tag.putInt("State", this.state);
        tag.putInt("Timer", this.timer);
        tag.putBoolean("PlayedGroundHit", this.playedGroundHit);
        tag.putBoolean("PlayedBreakSound", this.playedBreakSound);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            // Animasyon güncellemeleri...
            if (!this.onGround()) {
                fallState.animateWhen(true, this.tickCount);
                wasOnGroundClient = false;
            } else {
                if (!wasOnGroundClient) {
                    fallState.animateWhen(false, this.tickCount);
                    hitState.start(this.tickCount);
                    shakeState.start(this.tickCount);
                    wasOnGroundClient = true;
                } else {
                    fallState.animateWhen(false, this.tickCount);
                }
            }
            if (state == 1 || state == 2) shakeState.animateWhen(true, this.tickCount);
            if (state == 3) preexplosionState.animateWhen(true, this.tickCount);
            return;
        }

        Level lvl = this.level();
        if (!(lvl instanceof ServerLevel server)) return;

        // Yer çarpması ve hasar
        if (state == 0 && this.onGround()) {
            AABB box = this.getBoundingBox().inflate(0.2);
            ResourceKey<DamageType> key = ResourceKey.create(Registries.DAMAGE_TYPE,
                    new ResourceLocation("reverie", "barrel_fall"));
            Holder.Reference<DamageType> holder = level()
                    .registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(key);

            Entity attacker = null;
            if (ownerUUID != null) attacker = server.getPlayerByUUID(ownerUUID);
            DamageSource barrelFall = new DamageSource(holder, this, attacker);

            server.getEntitiesOfClass(LivingEntity.class, box, e -> e != this)
                    .forEach(victim -> victim.hurt(barrelFall, 30.0F));

            state = 1; timer = 0;
        }

        // Sesler
        if (state == 1 && !playedGroundHit) {
            server.playSound(null, getX(), getY(), getZ(),
                    ReverieModSounds.GOBLIN_BARREL_GROUND_HIT.get(),
                    SoundSource.NEUTRAL, 1.0F, 1.0F);
            playedGroundHit = true;
        }
        if ((state == 1 || state == 2) && timer % 7 == 0) {
            server.playSound(null, getX(), getY(), getZ(),
                    ReverieModSounds.STRUGGLE.get(),
                    SoundSource.NEUTRAL, 0.6F, 1.0F);
        }

        // Fizik
        if (!this.onGround()) {
            Vec3 vel = getDeltaMovement();
            setDeltaMovement(vel.x, vel.y - 0.04, vel.z);
            move(MoverType.SELF, getDeltaMovement());
        }

        // State makinesi
        if (state > 0) {
            timer++;
            if (state == 1 && timer > 1) {
                state = 2; timer = 0;
            } else if (state == 2 && timer > 60) {
                state = 3; timer = 0;
            } else if (state == 3 && timer > 20) {
                if (!playedBreakSound) {
                    server.playSound(null, getX(), getY(), getZ(),
                            ReverieModSounds.GOBLIN_BARREL_BREAK.get(),
                            SoundSource.NEUTRAL, 1.0F, 1.0F);
                    playedBreakSound = true;
                }
                explodeAndSpawn();
            }
        }
    }

    private void explodeAndSpawn() {
        if (!(level() instanceof ServerLevel server)) return;

        double px = getX(), py = getY() + 1.55, pz = getZ();
        int pieceCount = 14;
        double spreadXZ = 1.0, spreadY = 0.55, speed = 0.32;

        // Beta tester mı?
        boolean isNerd = false;
        String owner = getOwnerName();
        if (owner != null && NERD_TESTERS.contains(owner.toLowerCase(Locale.ROOT))) {
            isNerd = true;
        }

        if (isNerd) {
            server.sendParticles(ParticleTypes.HEART,
                    px, py, pz, pieceCount, spreadXZ, spreadY, spreadXZ, speed);
        } else {
            server.sendParticles(ReverieModParticleTypes.BARREL_SHARD_1.get(),
                    px, py, pz, pieceCount, spreadXZ, spreadY, spreadXZ, speed);
            server.sendParticles(ReverieModParticleTypes.BARREL_SHARD_2.get(),
                    px, py, pz, pieceCount, spreadXZ, spreadY, spreadXZ, speed);
            server.sendParticles(ReverieModParticleTypes.BARREL_NAIL.get(),
                    px, py, pz, pieceCount, spreadXZ, spreadY, spreadXZ, speed * 0.95);
            server.sendParticles(ReverieModParticleTypes.BARREL_METAL_SHARD.get(),
                    px, py, pz, pieceCount, spreadXZ, spreadY, spreadXZ, speed * 0.88);
        }

        // Patlama emitter
        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                px, getY() + 0.6, pz, 1, 0, 0, 0, 0.0);

        // Ses ve goblin spawn kısmı aynen devam eder...
        double x = px, y = getY(), z = pz;
        level().playSound(null, x, y, z,
                ReverieModSounds.BARREL_GOBLIN_SPAWN.get(),
                SoundSource.HOSTILE, 1.0F, 1.0F);

        for (int i = 0; i < 3; i++) {
            BarrelGoblinEntity gob = new BarrelGoblinEntity(
                    ReverieModEntities.BARREL_GOBLIN.get(), level());
            gob.setPos(x, y, z);
            if (ownerUUID != null) {
                gob.setOwnerUUID(ownerUUID);
                gob.setOwnerName(owner);
            }
            double r = random.nextDouble();
            gob.setItemSlot(EquipmentSlot.MAINHAND,
                    new ItemStack(r < 0.10 ? Items.IRON_SWORD :
                            r < 0.40 ? Items.STONE_SWORD :
                                    Items.WOODEN_SWORD));
            gob.setDeltaMovement(
                    (random.nextDouble() - 0.5) * 0.5,
                    0.5 + random.nextDouble() * 0.5,
                    (random.nextDouble() - 0.5) * 0.5);
            level().addFreshEntity(gob);
        }

        remove(RemovalReason.DISCARDED);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.ARMOR, 0.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    private double randomDelta() {
        return (random.nextDouble() - 0.5) * 0.4;
    }
}
