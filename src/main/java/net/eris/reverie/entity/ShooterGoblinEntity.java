package net.eris.reverie.entity;

import net.eris.reverie.entity.goal.GoblinHurtByTargetGoal;
import net.eris.reverie.entity.goal.GoblinTargetPlayerGoal;
import net.minecraft.world.entity.Mob;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.network.NetworkHooks;

import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Difficulty;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.monster.RangedAttackMob;

import net.eris.reverie.init.ReverieModEntities;
import net.eris.reverie.init.ReverieModItems;
import net.eris.reverie.entity.BoneSpearProjectileEntity;
import net.eris.reverie.entity.goal.GenericRangedAttackGoal;

import java.util.Optional;
import java.util.UUID;

public class ShooterGoblinEntity extends Monster implements RangedAttackMob {
    public final AnimationState animationState1 = new AnimationState();
    public final AnimationState animationState2 = new AnimationState();
    public final AnimationState animationState3 = new AnimationState();

    private static final EntityDataAccessor<Optional<UUID>> TARGET_UUID =
            SynchedEntityData.defineId(ShooterGoblinEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final EntityDataAccessor<ItemStack> HELD_ITEM =
            SynchedEntityData.defineId(ShooterGoblinEntity.class, EntityDataSerializers.ITEM_STACK);

    private static final int THROWING_ANIMATION_DURATION = 10;
    private int throwingTicks = 0;

    public ShooterGoblinEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ReverieModEntities.SHOOTER_GOBLIN.get(), world);
        this.setHeldItem(new ItemStack(ReverieModItems.BONE_SPEAR.get()));
    }

    public ShooterGoblinEntity(EntityType<ShooterGoblinEntity> type, Level world) {
        super(type, world);
        setMaxUpStep(0.6f);
        xpReward = 0;
        setNoAi(false);
        this.setHeldItem(new ItemStack(ReverieModItems.BONE_SPEAR.get()));
    }


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TARGET_UUID, Optional.empty());
        this.entityData.define(HELD_ITEM, ItemStack.EMPTY);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            LivingEntity target = this.getTarget();
            Optional<UUID> uuid = (target != null && target.isAlive())
                    ? Optional.of(target.getUUID())
                    : Optional.empty();
            if (!this.entityData.get(TARGET_UUID).equals(uuid)) {
                this.entityData.set(TARGET_UUID, uuid);
            }
        }
    }

    public boolean hasTargetClient() {
        return this.entityData.get(TARGET_UUID).isPresent();
    }

    public void setHeldItem(ItemStack stack) {
        this.entityData.set(HELD_ITEM, stack);
    }

    @Override
    public ItemStack getMainHandItem() {
        ItemStack held = this.entityData.get(HELD_ITEM);
        return (held == null || held.isEmpty()) ? super.getMainHandItem() : held;
    }

    @Override
    public boolean doHurtTarget(Entity targetEntity) {
        boolean result = super.doHurtTarget(targetEntity);
        if (result && !this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte)4);
        }
        return result;
    }

    @Override
    public void handleEntityEvent(byte id) {
        super.handleEntityEvent(id);
        if (id == 4) {
            // başka animasyon tetikleyebilirsin
        }
        if (id == 5) {
            this.animationState2.animateWhen(true, this.tickCount);
            this.throwingTicks = THROWING_ANIMATION_DURATION;
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new GenericRangedAttackGoal(this, 1.2D, 10, 16.0F));
        this.targetSelector.addGoal(2, new GoblinTargetPlayerGoal(this)); // <-- Vanilla target goal yerine rep tabanlı goal!
        this.targetSelector.addGoal(3, new GoblinHurtByTargetGoal(this).setAlertOthers());
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new FloatGoal(this));
    }


    // Bu kısım ShooterGoblinEntity'de performRangedAttack fonksiyonunda:
    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        ItemStack held = this.getMainHandItem();
        if (!held.is(ReverieModItems.BONE_SPEAR.get())) return;

        double px = this.getX();
        double py = this.getY() + this.getEyeHeight() - 0.1;
        double pz = this.getZ();

        double dx = target.getX() - px;
        double dy = target.getY() + target.getBbHeight() / 3.5 - py;
        double dz = target.getZ() - pz;

        BoneSpearProjectileEntity spear = new BoneSpearProjectileEntity(ReverieModEntities.BONE_SPEAR_PROJECTILE.get(), this, this.level());
        spear.setPos(px, py, pz);

        // HIZI YÜKSELT: 2.7F // spread'i düşür: 1.2F
        spear.shoot(dx, dy, dz, 2.7F, 1.2F); // Velocity yüksek, spread düşük
        this.level().addFreshEntity(spear);

        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte)5);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.hurt"));
    }

    @Override
    public SoundEvent getDeathSound() {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.death"));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.animationState1.animateWhen(true, this.tickCount);
            if (throwingTicks > 0) {
                throwingTicks--;
                if (throwingTicks == 0) {
                    this.animationState2.stop();
                }
            }
            if (this.hasTargetClient()) {
                this.animationState3.animateWhen(true, this.tickCount);
            } else {
                this.animationState3.stop();
            }
        }
    }

    public static void init() {
        SpawnPlacements.register(
                ReverieModEntities.SHOOTER_GOBLIN.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, world, reason, pos, random) ->
                        world.getDifficulty() != Difficulty.PEACEFUL
                                && Monster.isDarkEnoughToSpawn(world, pos, random)
                                && Mob.checkMobSpawnRules(entityType, world, reason, pos, random)
        );
    }

    public static AttributeSupplier.Builder createAttributes() {
        AttributeSupplier.Builder builder = Mob.createMobAttributes();
        builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3);
        builder = builder.add(Attributes.MAX_HEALTH, 12);
        builder = builder.add(Attributes.ARMOR, 0);
        builder = builder.add(Attributes.ATTACK_DAMAGE, 3);
        builder = builder.add(Attributes.FOLLOW_RANGE, 24);
        return builder;
    }
}