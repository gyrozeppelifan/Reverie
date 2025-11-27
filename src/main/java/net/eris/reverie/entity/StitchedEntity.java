package net.eris.reverie.entity;

import net.eris.reverie.client.model.animations.StitchedAnimation;
import net.eris.reverie.init.ReverieModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PlayMessages; // <-- BU IMPORT EKSİKTİ

public class StitchedEntity extends Monster {
    private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(StitchedEntity.class, EntityDataSerializers.INT);

    public final AnimationState passiveState = new AnimationState();
    public final AnimationState electrocutedState = new AnimationState();
    public final AnimationState standupState = new AnimationState();
    public final AnimationState walkState = new AnimationState();

    private int animationTimer = 0;

    public StitchedEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public StitchedEntity(Level level) {
        this(ReverieModEntities.STITCHED.get(), level);
    }

    // --- İŞTE EKSİK OLAN CONSTRUCTOR BU ---
    // Bu constructor olmazsa "Invalid constructor reference" hatası alırsın.
    public StitchedEntity(PlayMessages.SpawnEntity packet, Level level) {
        this(ReverieModEntities.STITCHED.get(), level);
    }
    // ----------------------------------------

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STATE, 0);
    }

    @Override
    protected void registerGoals() {
        if (getState() == 3) {
            this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0D));
            this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
            this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    public int getState() {
        return this.entityData.get(DATA_STATE);
    }

    public void setState(int state) {
        this.entityData.set(DATA_STATE, state);
    }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt lightning) {
        if (getState() == 0) {
            this.setState(1);
            this.animationTimer = 20;
            this.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 2.0F, 1.0F);
        } else {
            super.thunderHit(level, lightning);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (getState() < 3 && !source.isCreativePlayer()) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            setupAnimationStates();
        } else {
            handleStateLogic();
        }
    }

    private void handleStateLogic() {
        int currentState = getState();
        if (currentState == 1) {
            if (--animationTimer <= 0) {
                setState(2);
                animationTimer = 48;
            }
        } else if (currentState == 2) {
            if (--animationTimer <= 0) {
                setState(3);
                this.goalSelector.removeAllGoals(goal -> true);
                this.registerGoals();
            }
        }

        if (currentState < 3) {
            this.getNavigation().stop();
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
        }
    }

    private void setupAnimationStates() {
        int state = getState();
        if (state == 0) {
            passiveState.startIfStopped(this.tickCount);
            electrocutedState.stop();
            standupState.stop();
            walkState.stop();
        } else if (state == 1) {
            passiveState.stop();
            electrocutedState.startIfStopped(this.tickCount);
            standupState.stop();
        } else if (state == 2) {
            passiveState.stop();
            electrocutedState.stop();
            standupState.startIfStopped(this.tickCount);
        } else if (state == 3) {
            standupState.stop();
            if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6) {
                walkState.startIfStopped(this.tickCount);
            } else {
                walkState.stop();
            }
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        if (getState() == 3) {
            this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 1.0F);
        }
    }
}