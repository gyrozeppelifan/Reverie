package net.eris.reverie.entity;

import net.eris.reverie.ReverieMod; // Advancement için lazım
import net.eris.reverie.init.ReverieModEnchantments;
import net.eris.reverie.init.ReverieModItems;
import net.eris.reverie.init.ReverieModSounds;
import net.eris.reverie.init.ReverieModEntities;
import net.eris.reverie.procedures.SpikedLogPlaybackConditionProcedure;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer; // Advancement için lazım
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpikedLogEntity extends PathfinderMob {
    private static final double BASE_SPEED = 0.2;
    private static final int ROLL_DURATION = 120;

    private static final EntityDataAccessor<Boolean> DATA_IS_ROLLING =
            SynchedEntityData.defineId(SpikedLogEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> DATA_OWNER_NAME =
            SynchedEntityData.defineId(SpikedLogEntity.class, EntityDataSerializers.STRING);

    // --- BÜYÜ VERİLERİ ---
    private static final EntityDataAccessor<Integer> DATA_WILDFIRE = SynchedEntityData.defineId(SpikedLogEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MOMENTUM = SynchedEntityData.defineId(SpikedLogEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_RICOCHET = SynchedEntityData.defineId(SpikedLogEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VORTEX = SynchedEntityData.defineId(SpikedLogEntity.class, EntityDataSerializers.INT);

    // --- RECALL VERİLERİ ---
    private static final EntityDataAccessor<Integer> DATA_RECALL = SynchedEntityData.defineId(SpikedLogEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_RETURNING = SynchedEntityData.defineId(SpikedLogEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState animationState0 = new AnimationState();
    public final AnimationState animationState1 = new AnimationState();

    private final Set<Integer> hitEntities = new HashSet<>();
    private UUID ownerUUID = null;
    private int bouncesLeft = 0;

    public void setOwner(Player player) { this.ownerUUID = player.getUUID(); }
    public UUID getOwnerUUID() { return ownerUUID; }
    public void setOwnerName(String name) { this.entityData.set(DATA_OWNER_NAME, name); }
    public String getOwnerName() { return this.entityData.get(DATA_OWNER_NAME); }

    // Setterlar
    public void setWildfireLevel(int level) { this.entityData.set(DATA_WILDFIRE, level); }
    public void setMomentumLevel(int level) { this.entityData.set(DATA_MOMENTUM, level); }
    public void setRicochetLevel(int level) {
        this.entityData.set(DATA_RICOCHET, level);
        this.bouncesLeft = level * 2;
    }
    public void setVortexLevel(int level) { this.entityData.set(DATA_VORTEX, level); }
    public void setRecallLevel(int level) { this.entityData.set(DATA_RECALL, level); }

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
        this.entityData.define(DATA_OWNER_NAME, "");
        this.entityData.define(DATA_WILDFIRE, 0);
        this.entityData.define(DATA_MOMENTUM, 0);
        this.entityData.define(DATA_RICOCHET, 0);
        this.entityData.define(DATA_VORTEX, 0);
        this.entityData.define(DATA_RECALL, 0);
        this.entityData.define(DATA_RETURNING, false);
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
        nbt.putString("OwnerName", this.entityData.get(DATA_OWNER_NAME));
        if (this.ownerUUID != null) nbt.putUUID("Owner", this.ownerUUID);
        nbt.putInt("Wildfire", this.entityData.get(DATA_WILDFIRE));
        nbt.putInt("Momentum", this.entityData.get(DATA_MOMENTUM));
        nbt.putInt("Ricochet", this.entityData.get(DATA_RICOCHET));
        nbt.putInt("Vortex", this.entityData.get(DATA_VORTEX));
        nbt.putInt("Recall", this.entityData.get(DATA_RECALL));
        nbt.putInt("BouncesLeft", this.bouncesLeft);
        nbt.putBoolean("IsReturning", this.entityData.get(DATA_RETURNING));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("IsRolling")) this.entityData.set(DATA_IS_ROLLING, nbt.getBoolean("IsRolling"));
        if (nbt.contains("OwnerName")) this.entityData.set(DATA_OWNER_NAME, nbt.getString("OwnerName"));
        if (nbt.hasUUID("Owner")) this.ownerUUID = nbt.getUUID("Owner");
        if (nbt.contains("Wildfire")) this.entityData.set(DATA_WILDFIRE, nbt.getInt("Wildfire"));
        if (nbt.contains("Momentum")) this.entityData.set(DATA_MOMENTUM, nbt.getInt("Momentum"));
        if (nbt.contains("Ricochet")) this.entityData.set(DATA_RICOCHET, nbt.getInt("Ricochet"));
        if (nbt.contains("Vortex")) this.entityData.set(DATA_VORTEX, nbt.getInt("Vortex"));
        if (nbt.contains("Recall")) this.entityData.set(DATA_RECALL, nbt.getInt("Recall"));
        if (nbt.contains("BouncesLeft")) this.bouncesLeft = nbt.getInt("BouncesLeft");
        if (nbt.contains("IsReturning")) this.entityData.set(DATA_RETURNING, nbt.getBoolean("IsReturning"));
    }

    public boolean isRolling() {
        return this.entityData.get(DATA_IS_ROLLING);
    }

    // --- HELPER: Büyülü Item Stack Oluşturma ---
    private ItemStack createSpikedLogItemStack() {
        ItemStack stack = new ItemStack(ReverieModItems.SPIKED_LOG_ITEM.get());
        if (this.entityData.get(DATA_WILDFIRE) > 0) stack.enchant(ReverieModEnchantments.WILDFIRE.get(), this.entityData.get(DATA_WILDFIRE));
        if (this.entityData.get(DATA_MOMENTUM) > 0) stack.enchant(ReverieModEnchantments.MOMENTUM.get(), this.entityData.get(DATA_MOMENTUM));
        if (this.entityData.get(DATA_RICOCHET) > 0) stack.enchant(ReverieModEnchantments.RICOCHET.get(), this.entityData.get(DATA_RICOCHET));
        if (this.entityData.get(DATA_VORTEX) > 0) stack.enchant(ReverieModEnchantments.VORTEX.get(), this.entityData.get(DATA_VORTEX));
        if (this.entityData.get(DATA_RECALL) > 0) stack.enchant(ReverieModEnchantments.RECALL.get(), this.entityData.get(DATA_RECALL));
        return stack;
    }

    private void dropSpikedLogItem() {
        if (this.entityData.get(DATA_RETURNING)) return;
        this.spawnAtLocation(createSpikedLogItemStack());
    }

    // --- YENİ: ADVANCEMENT HELPER ---
    private void grantAdvancement(String advancementName) {
        if (this.level() instanceof ServerLevel serverLevel && this.ownerUUID != null) {
            Player player = serverLevel.getPlayerByUUID(this.ownerUUID);
            if (player instanceof ServerPlayer serverPlayer) {
                var advancement = serverLevel.getServer().getAdvancements().getAdvancement(new ResourceLocation(ReverieMod.MODID, advancementName));
                if (advancement != null) {
                    var progress = serverPlayer.getAdvancements().getOrStartProgress(advancement);
                    if (!progress.isDone()) {
                        for (String criterion : progress.getRemainingCriteria()) serverPlayer.getAdvancements().award(advancement, criterion);
                    }
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        String msgId = source.getMsgId();
        if (msgId.contains("explosion") || msgId.contains("blast") || msgId.contains("lava") || msgId.equals("outOfWorld") || msgId.equals("genericKill")) {
            return super.hurt(source, amount);
        }

        if (!this.level().isClientSide() && source.getEntity() instanceof LivingEntity attacker) {
            ItemStack stack = attacker.getMainHandItem();
            if (stack.getItem() instanceof AxeItem) {
                ServerLevel server = (ServerLevel) this.level();
                server.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 0.5, this.getZ(), 20, 0.2, 0.2, 0.2, 0.02);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ReverieModSounds.SPIKED_LOG_CRASH.get(), SoundSource.BLOCKS, 1f, 1f);
                dropSpikedLogItem();
                hitEntities.clear();
                this.discard();
                return true;
            }
        }
        return false;
    }

    @Override
    public void die(DamageSource source) {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ReverieModSounds.SPIKED_LOG_CRASH.get(), SoundSource.BLOCKS, 1f, 1f);
        dropSpikedLogItem();
        hitEntities.clear();
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();

        // --- RECALL (GERİ DÖNÜŞ) ---
        if (this.entityData.get(DATA_RETURNING)) {
            this.setNoGravity(true);
            this.noPhysics = true;

            if (ownerUUID != null && this.level() instanceof ServerLevel server) {
                Entity owner = server.getEntity(ownerUUID);
                if (owner != null && owner.isAlive()) {
                    Vec3 ownerPos = owner.getEyePosition().subtract(0, 0.5, 0);
                    Vec3 toOwner = ownerPos.subtract(this.position());

                    if (toOwner.length() < 1.5) {
                        if (owner instanceof Player player) {
                            if (!player.getInventory().add(createSpikedLogItemStack())) {
                                player.drop(createSpikedLogItemStack(), false);
                            }
                            player.playSound(SoundEvents.ITEM_PICKUP, 1.0f, 1.0f);

                            // ADVANCEMENT: Geri döndü!
                            grantAdvancement("spiked_log_recall");
                        }
                        this.discard();
                        return;
                    }

                    double speed = 0.5 + (this.entityData.get(DATA_RECALL) * 0.2);
                    this.setDeltaMovement(toOwner.normalize().scale(speed));
                } else {
                    this.entityData.set(DATA_RETURNING, false);
                    this.setNoGravity(false);
                    this.noPhysics = false;
                }
            }
            this.move(MoverType.SELF, this.getDeltaMovement());
            return;
        }

        // --- MEVCUT MANTIK ---
        if (this.isInWater()) this.setNoGravity(true);
        else this.setNoGravity(false);

        // Wildfire
        if (this.entityData.get(DATA_WILDFIRE) > 0) {
            this.setSecondsOnFire(2);
            if (this.level().isClientSide) {
                this.level().addParticle(ParticleTypes.FLAME, this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), 0, 0.05, 0);
            } else if (this.tickCount % 20 == 0) {
                // ADVANCEMENT: Yanıyor!
                grantAdvancement("spiked_log_flame");
            }
        }

        if (!this.level().isClientSide() && this.entityData.get(DATA_IS_ROLLING)) {
            if (this.tickCount % 7 == 0) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ReverieModSounds.SPIKED_LOG_ROLL.get(), SoundSource.BLOCKS, 1f, 1f);
            }
        }

        boolean shouldStop = false;
        if (!this.level().isClientSide()) {
            if (this.isInWater()) shouldStop = true;
            if (this.tickCount >= ROLL_DURATION) shouldStop = true;
        }

        // --- RICOCHET ---
        if (this.horizontalCollision) {
            int ricochetLvl = this.entityData.get(DATA_RICOCHET);
            if (ricochetLvl > 0 && bouncesLeft > 0) {
                this.bouncesLeft--;
                this.setYRot(this.getYRot() + 180.0F);
                hitEntities.clear();
                this.level().playSound(null, this.blockPosition(), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.5f, 1.5f);

                // ADVANCEMENT: Sekti!
                grantAdvancement("spiked_log_ricochet");
            } else {
                this.level().playSound(null, this.blockPosition(), SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1f, 1f);
                shouldStop = true;
                if (ricochetLvl == 0 || bouncesLeft <= 0) {
                    this.entityData.set(DATA_IS_ROLLING, false);
                }
            }
        }

        // Stop & Recall Check
        if (shouldStop && this.entityData.get(DATA_IS_ROLLING)) {
            this.entityData.set(DATA_IS_ROLLING, false);
            this.setDeltaMovement(Vec3.ZERO);
            hitEntities.clear();

            if (this.entityData.get(DATA_RECALL) > 0) {
                this.entityData.set(DATA_RETURNING, true);
                this.level().playSound(null, this.blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }

        // --- VORTEX ---
        if (this.entityData.get(DATA_VORTEX) > 0 && this.entityData.get(DATA_IS_ROLLING)) {
            int vortexLvl = this.entityData.get(DATA_VORTEX);
            double range = 2.0 + (vortexLvl * 1.5);
            boolean pulledSomeone = false;
            for (LivingEntity nearby : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(range))) {
                if (nearby != this && nearby.getUUID() != ownerUUID) {
                    Vec3 direction = this.position().subtract(nearby.position()).normalize().scale(0.15 * vortexLvl);
                    nearby.push(direction.x, 0.1, direction.z);
                    pulledSomeone = true;
                }
            }
            // ADVANCEMENT: Birini çekti!
            if (pulledSomeone && this.tickCount % 10 == 0) grantAdvancement("spiked_log_vortex");
        }

        // --- HAREKET ---
        if (!this.entityData.get(DATA_IS_ROLLING)) {
            if (!this.level().isClientSide()) {
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(0, motion.y, 0);
            }
        } else {
            if (!this.level().isClientSide()) {
                Vec3 oldMotion = this.getDeltaMovement();
                double currentY = oldMotion.y;
                int momentum = this.entityData.get(DATA_MOMENTUM);
                double speed = BASE_SPEED + (momentum * 0.15);
                double yawRad = Math.toRadians(-this.getYRot());
                Vec3 dirXZ = new Vec3(Math.sin(yawRad), 0, Math.cos(yawRad)).normalize().scale(speed);
                Vec3 newMotion = new Vec3(dirXZ.x, currentY, dirXZ.z);
                this.setDeltaMovement(newMotion);
                this.hasImpulse = true;
                this.move(MoverType.SELF, newMotion);
            }
        }

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

        // --- HASAR ---
        if (this.entityData.get(DATA_IS_ROLLING)) {
            for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox())) {
                if (target == this) continue;
                if (this.ownerUUID != null && target.getUUID().equals(this.ownerUUID)) continue;

                if (this.entityData.get(DATA_WILDFIRE) > 0) target.setSecondsOnFire(6);

                if (!hitEntities.contains(target.getId())) {
                    hitEntities.add(target.getId());
                    double dx = target.getX() - this.getX();
                    double dz = target.getZ() - this.getZ();
                    double dist = Math.hypot(dx, dz);
                    int momentum = this.entityData.get(DATA_MOMENTUM);
                    double knockbackBase = 2.0 + (momentum * 1.0);
                    if (dist > 0) {
                        target.push(dx/dist * knockbackBase, 0.5, dz/dist * knockbackBase);
                    }
                    // ADVANCEMENT: Momentum vuruşu!
                    if (momentum > 0) grantAdvancement("spiked_log_momentum");
                }

                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));
                DamageSource logSmash = new DamageSource(
                        this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                                .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("reverie", "spikedlogsmash"))),
                        this,
                        (ownerUUID != null && this.level() instanceof ServerLevel server) ? server.getPlayerByUUID(ownerUUID) : null
                );

                float damage = 12f;
                if (target.getHealth() <= 6f) target.hurt(logSmash, Float.MAX_VALUE);
                else target.hurt(logSmash, damage);
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

    @Override public boolean canBeAffected(MobEffectInstance effect) { return false; }
    @Override public boolean canBeCollidedWith() { return true; }
    @Override public boolean isPushable() { return false; }
    @Override public void push(Entity e) {}

    public static void init() {}
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.MAX_HEALTH, 3)
                .add(Attributes.ARMOR, 0)
                .add(Attributes.FOLLOW_RANGE, 0);
    }
}