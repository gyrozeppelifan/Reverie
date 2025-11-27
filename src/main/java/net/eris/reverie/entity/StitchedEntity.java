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
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PlayMessages;

public class StitchedEntity extends Monster {
    // States: 0=PASSIVE, 1=ELECTROCUTED, 2=STANDUP, 3=ALIVE, 4=WAITING (Ara geçiş)
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

    public StitchedEntity(PlayMessages.SpawnEntity packet, Level level) {
        this(ReverieModEntities.STITCHED.get(), level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STATE, 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        // State değiştiğinde hitbox'ı güncelle (Client & Server)
        if (DATA_STATE.equals(key)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    // --- HITBOX AYARLAMASI ---
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        int state = this.getState();
        // Yatıyor (0), Çarpılıyor (1) veya Bekliyor (4) ise YATAY hitbox kullan
        if (state == 0 || state == 1 || state == 4) {
            return EntityDimensions.fixed(0.8f, 0.5f);
        }
        // Diğer durumlarda (Standup, Alive) normal boyuta dön
        return super.getDimensions(pose);
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
            this.setState(1); // ELECTROCUTED
            this.animationTimer = 60; // 3 Saniye boyunca çarpılma (önceki 20'ydi)
            this.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 2.0F, 1.0F);
        } else {
            super.thunderHit(level, lightning);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Yanma hasarını engelle
        if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_FIRE) || source.is(net.minecraft.world.damagesource.DamageTypes.ON_FIRE)) {
            return false;
        }
        if (getState() < 3 && !source.isCreativePlayer()) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        super.tick();

        // --- GÖRSEL YANMAYI ENGELLE ---
        if (this.isOnFire()) {
            this.clearFire(); // Söndür (Visual fire overlay'i kaldırır)
        }

        if (this.level().isClientSide) {
            setupAnimationStates();
        } else {
            handleStateLogic();
        }
    }

    private void handleStateLogic() {
        int currentState = getState();

        if (currentState == 1) { // AŞAMA 1: ELECTROCUTED (3 sn)
            if (--animationTimer <= 0) {
                setState(4); // WAITING (Ara geçişe git)
                animationTimer = 20; // 1 Saniye bekle
            }
        }
        else if (currentState == 4) { // AŞAMA 2: BEKLEME (1 sn)
            if (--animationTimer <= 0) {
                setState(2); // STANDUP (Kalkmaya başla)
                animationTimer = 48; // ~2.4 sn animasyon süresi
            }
        }
        else if (currentState == 2) { // AŞAMA 3: STANDUP
            if (--animationTimer <= 0) {
                setState(3); // ALIVE (Tamamlandı)
                this.goalSelector.removeAllGoals(goal -> true);
                this.registerGoals();
            }
        }

        // Canlanana kadar hareket etme
        if (currentState < 3) {
            this.getNavigation().stop();
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
        }
    }

    private void setupAnimationStates() {
        int state = getState();

        // 0 (Passive) veya 4 (Waiting) -> Passive Animasyon
        if (state == 0 || state == 4) {
            passiveState.startIfStopped(this.tickCount);
            electrocutedState.stop();
            standupState.stop();
            walkState.stop();
        }
        else if (state == 1) { // Electrocuted
            passiveState.stop();
            electrocutedState.startIfStopped(this.tickCount);
            standupState.stop();
        }
        else if (state == 2) { // Standup
            passiveState.stop();
            electrocutedState.stop();
            standupState.startIfStopped(this.tickCount);
        }
        else if (state == 3) { // Alive
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