package net.eris.reverie.entity;

import net.eris.reverie.init.ReverieModEntities;
import net.eris.reverie.init.ReverieModItems;
import net.eris.reverie.procedures.*;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.EnumSet;

public class DrunkardEntity extends Raider {
    public static final EntityDataAccessor<Boolean> DATA_isCharging      = SynchedEntityData.defineId(DrunkardEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> DATA_hasBrokenBottle = SynchedEntityData.defineId(DrunkardEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> DATA_isDrinking      = SynchedEntityData.defineId(DrunkardEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> DATA_DrinkTime      = SynchedEntityData.defineId(DrunkardEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DATA_canDrink       = SynchedEntityData.defineId(DrunkardEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> DATA_Cyclops        = SynchedEntityData.defineId(DrunkardEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> DATA_isAttacking    = SynchedEntityData.defineId(DrunkardEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState animationState0 = new AnimationState();
    public final AnimationState animationState1 = new AnimationState();
    public final AnimationState animationState3 = new AnimationState();

    public DrunkardEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ReverieModEntities.DRUNKARD.get(), world);
    }

    public DrunkardEntity(EntityType<DrunkardEntity> type, Level world) {
        super(type, world);
        setMaxUpStep(0.6f);
        xpReward = 5;
        setPersistenceRequired();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_isCharging,      false);
        entityData.define(DATA_hasBrokenBottle, false);
        entityData.define(DATA_isDrinking,      false);
        entityData.define(DATA_DrinkTime,       120);
        entityData.define(DATA_canDrink,        1);
        entityData.define(DATA_Cyclops,         false);
        entityData.define(DATA_isAttacking,     false);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        getNavigation().getNodeEvaluator().setCanOpenDoors(true);

        // 1. Player target
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
        // 2. Villager target
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Villager.class, false));
        // 3. HurtBy retaliation
        targetSelector.addGoal(3, new HurtByTargetGoal(this));

        // Combat: melee attack with memory
        goalSelector.addGoal(4, new MeleeAttackGoal(this, 2.0, true));
        // Bottle retrieval
        goalSelector.addGoal(5, new RetrieveBottleGoal(this, 1.0));

        // Wander when no target and not charging
        goalSelector.addGoal(6, new RandomStrollGoal(this, 1.0) {
            @Override public boolean canUse() {
                return super.canUse() && getTarget() == null && !entityData.get(DATA_isCharging);
            }
            @Override public boolean canContinueToUse() {
                return super.canContinueToUse() && getTarget() == null && !entityData.get(DATA_isCharging);
            }
        });
        // Look around idle
        goalSelector.addGoal(7, new RandomLookAroundGoal(this) {
            @Override public boolean canUse() {
                return super.canUse() && getTarget() == null && !entityData.get(DATA_isCharging);
            }
            @Override public boolean canContinueToUse() {
                return super.canContinueToUse() && getTarget() == null && !entityData.get(DATA_isCharging);
            }
        });

        goalSelector.addGoal(8, new OpenDoorGoal(this, true));
        goalSelector.addGoal(9, new FloatGoal(this));
    }

    private SoundEvent getSound(String key) {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("reverie:" + key));
    }

    @Override public SoundEvent getAmbientSound()      { return getSound("drunkard_idle"); }
    @Override public SoundEvent getHurtSound(DamageSource ds) { return getSound("drunkard_hurt"); }
    @Override public SoundEvent getDeathSound()        { return getSound("drunkard_death"); }
    @Override public SoundEvent getCelebrateSound()    { return getSound("drunkard_charge"); }

    @Override
    public boolean hurt(DamageSource src, float amount) {
        DrunkardEntityIsHurtProcedure.execute(this, src.getEntity());
        return super.hurt(src, amount);
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("isCharging",      entityData.get(DATA_isCharging));
        tag.putBoolean("hasBrokenBottle", entityData.get(DATA_hasBrokenBottle));
        tag.putBoolean("isDrinking",      entityData.get(DATA_isDrinking));
        tag.putInt("drinkTime",           entityData.get(DATA_DrinkTime));
        tag.putInt("canDrink",            entityData.get(DATA_canDrink));
        tag.putBoolean("isCyclops",       entityData.get(DATA_Cyclops));
        tag.putBoolean("isAttacking",     entityData.get(DATA_isAttacking));
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if(tag.contains("isCharging"))      entityData.set(DATA_isCharging,      tag.getBoolean("isCharging"));
        if(tag.contains("hasBrokenBottle")) entityData.set(DATA_hasBrokenBottle, tag.getBoolean("hasBrokenBottle"));
        if(tag.contains("isDrinking"))      entityData.set(DATA_isDrinking,      tag.getBoolean("isDrinking"));
        if(tag.contains("drinkTime"))       entityData.set(DATA_DrinkTime,       tag.getInt("drinkTime"));
        if(tag.contains("canDrink"))        entityData.set(DATA_canDrink,        tag.getInt("canDrink"));
        if(tag.contains("isCyclops"))       entityData.set(DATA_Cyclops,         tag.getBoolean("isCyclops"));
        if(tag.contains("isAttacking"))     entityData.set(DATA_isAttacking,     tag.getBoolean("isAttacking"));
    }

    @Override public void tick() {
        super.tick();
        if(level().isClientSide) {
            animationState0.animateWhen(DrunkardChargeProcedure.execute(this), tickCount);
            animationState1.animateWhen(DrunkardDrinkPlaybackProcedure.execute(this), tickCount);
            animationState3.animateWhen(DrunkardAttackConditionProcedure.execute(level(), this), tickCount);
        }
    }

    @Override public void baseTick() {
        super.baseTick();
        DrunkardStageUpdateProcedure.execute(level(), this);
    }

    public static void init() {
        Raid.RaiderType.create("drunkard", ReverieModEntities.DRUNKARD.get(), new int[]{0,4,3,3,4,4,4,2});
    }

    @Override public void applyRaidBuffs(int wave, boolean unused) {}

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED,      0.23)
            .add(Attributes.MAX_HEALTH,         30)
            .add(Attributes.ARMOR,               2)
            .add(Attributes.ATTACK_DAMAGE,       5)
            .add(Attributes.FOLLOW_RANGE,       48)
            .add(Attributes.KNOCKBACK_RESISTANCE,0.2)
            .add(Attributes.ATTACK_KNOCKBACK,    0.5);
    }

    private static class RetrieveBottleGoal extends Goal {
        private final DrunkardEntity mob;
        private final double speed;
        private BlockPos targetChest;
        private int scanCooldown;
        private boolean hasOpened;

        public RetrieveBottleGoal(DrunkardEntity mob, double speed) {
            this.mob = mob;
            this.speed = speed;
            this.scanCooldown = 0;
            this.hasOpened = false;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override public boolean canUse() {
            if(!mob.entityData.get(DATA_hasBrokenBottle)) return false;
            if(mob.getTarget() != null) return false;
            if(mob.entityData.get(DATA_canDrink) == 1) return false;
            if(targetChest != null) return true;
            if(--scanCooldown > 0) return false; scanCooldown = 20;
            int cx = mob.blockPosition().getX() >> 4;
            int cz = mob.blockPosition().getZ() >> 4;
            for(int dx=-2; dx<=2; dx++){
                for(int dz=-2; dz<=2; dz++){
                    LevelChunk chunk = mob.level().getChunk(cx+dx, cz+dz);
                    for(BlockEntity be : chunk.getBlockEntities().values()){
                        if(be instanceof ChestBlockEntity chest && containsBooze(chest)){
                            targetChest = be.getBlockPos();
                            hasOpened = false;
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private boolean containsBooze(ChestBlockEntity chest){
            for(int i=0; i<chest.getContainerSize(); i++){
                if(chest.getItem(i).getItem() == ReverieModItems.BOOZE_BOTTLE.get()) return true;
            }
            return false;
        }

        @Override public void start() {
            double x = targetChest.getX()+0.5, y = targetChest.getY()+0.5, z = targetChest.getZ()+0.5;
            mob.getNavigation().moveTo(x, y, z, speed);
        }

        @Override public void tick() {
            double x = targetChest.getX()+0.5, y = targetChest.getY()+0.5, z = targetChest.getZ()+0.5;
            double distSq = mob.distanceToSqr(x, y, z);
            if(!hasOpened && distSq < 4.0) {
                ChestBlock block = (ChestBlock)mob.level().getBlockState(targetChest).getBlock();
                mob.level().blockEvent(targetChest, block, 1, 1);
                mob.level().playSound(null, targetChest, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 1f, 1f);
                hasOpened = true;
            }
            if(distSq < 2.25) {
                BlockEntity be = mob.level().getBlockEntity(targetChest);
                if(be instanceof ChestBlockEntity chest) {
                    for(int i=0; i<chest.getContainerSize(); i++){
                        ItemStack stack = chest.getItem(i);
                        if(stack.getItem() == ReverieModItems.BOOZE_BOTTLE.get()){
                            chest.removeItem(i, 1);
                            mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ReverieModItems.BOOZE_BOTTLE.get()));
                            mob.entityData.set(DATA_hasBrokenBottle, false);
                            mob.entityData.set(DATA_canDrink, 1);
                            break;
                        }
                    }
                }
                if(hasOpened) {
                    ChestBlock block = (ChestBlock)mob.level().getBlockState(targetChest).getBlock();
                    mob.level().blockEvent(targetChest, block, 1, 0);
                    mob.level().playSound(null, targetChest, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 1f, 1f);
                }
                stop();
            }
        }

        @Override public boolean canContinueToUse() {
            return targetChest != null && mob.entityData.get(DATA_hasBrokenBottle);
        }

        @Override public void stop() {
            targetChest = null;
            mob.getNavigation().stop();
        }
    }
}
