package net.eris.reverie.entity;

import net.eris.reverie.init.ReverieModEntities;
import net.eris.reverie.init.ReverieModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PlayMessages; // EKLENDİ
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class HogEntity extends AbstractHorse {

    // --- ANİMASYON DURUMLARI ---
    public final AnimationState idleState = new AnimationState();
    public final AnimationState walkState = new AnimationState();
    public final AnimationState dashState = new AnimationState();
    public final AnimationState roarState = new AnimationState();
    public final AnimationState flyState = new AnimationState();

    // --- DATA SYNC ---
    private static final EntityDataAccessor<Boolean> DASHING = SynchedEntityData.defineId(HogEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ROARING = SynchedEntityData.defineId(HogEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> UNICORN_MODE = SynchedEntityData.defineId(HogEntity.class, EntityDataSerializers.BOOLEAN);

    // --- DEĞİŞKENLER ---
    private int dashTimer = 0;
    private int dashCooldown = 0;
    private int roarTimer = 0;
    private int roarCooldown = 0;
    private int unicornTimer = 0;

    // Gökkuşağı İzi için Hafıza
    public final LinkedList<Vec3> trailHistory = new LinkedList<>();

    public HogEntity(EntityType<? extends AbstractHorse> type, Level level) {
        super(type, level);
        this.xpReward = 15;
    }

    // --- BU METOT EKSİKTİ, BU YÜZDEN KAYIT HATASI ALIYORDUN ---
    public HogEntity(PlayMessages.SpawnEntity packet, Level level) {
        this(ReverieModEntities.HOG.get(), level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DASHING, false);
        this.entityData.define(ROARING, false);
        this.entityData.define(UNICORN_MODE, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7D)
                .add(Attributes.JUMP_STRENGTH, 0.7D);
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isAlive()) {
            // DÜZELTME: instanceof hatası giderildi.
            LivingEntity passenger = this.getControllingPassenger();
            if (this.isVehicle() && passenger != null) {
                LivingEntity rider = passenger; // Artık rider değişkenimiz var

                // 1. UNICORN MODU (UÇUŞ)
                if (this.isUnicornMode()) {
                    this.setYRot(rider.getYRot());
                    this.yRotO = this.getYRot();
                    this.setXRot(rider.getXRot() * 0.5F);
                    this.setRot(this.getYRot(), this.getXRot());
                    this.yBodyRot = this.getYRot();
                    this.yHeadRot = this.yBodyRot;

                    float speed = (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.5F;

                    double vertical = 0;
                    if (this.isJumping) vertical += 0.2;

                    Vec3 move = new Vec3(0, 0, rider.zza).yRot(-this.getYRot() * ((float)Math.PI / 180F));
                    this.setDeltaMovement(this.getDeltaMovement().add(move.x * speed, vertical, move.z * speed).multiply(0.9, 0.9, 0.9));

                    this.move(MoverType.SELF, this.getDeltaMovement());
                    return;
                }

                // 2. TANK SÜRÜŞÜ (YERDE)
                float targetYaw = rider.getYRot();
                float turnSpeed = this.isDashing() ? 0.01F : 0.06F;
                this.yBodyRot = rotLerp(this.yBodyRot, targetYaw, turnSpeed);
                this.setYRot(this.yBodyRot);
                this.yHeadRot = this.yBodyRot;
                this.yRotO = this.yBodyRot;

                double speed = this.getAttributeValue(Attributes.MOVEMENT_SPEED);
                double forwardInput = rider.zza;

                if (this.isDashing()) {
                    speed *= 2.5D;
                    forwardInput = 1.0D;
                }

                this.setSpeed((float)speed);
                super.travel(new Vec3(0, travelVector.y, forwardInput));

                this.setDeltaMovement(this.getDeltaMovement().multiply(0.97D, 1.0D, 0.97D));
                return;
            }
        }
        super.travel(travelVector);
    }

    private float rotLerp(float current, float target, float alpha) {
        float f = Mth.wrapDegrees(target - current);
        if (f > 180.0F) f -= 360.0F;
        if (f < -180.0F) f += 360.0F;
        return current + f * alpha;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            if (this.isUnicornMode() || this.isDashing()) {
                Vec3 offset = new Vec3(0, 0.8, -0.5).yRot(-this.yBodyRot * ((float)Math.PI / 180F));
                Vec3 trailPos = this.position().add(offset);

                trailHistory.add(trailPos);
                if (trailHistory.size() > 30) trailHistory.removeFirst();
            } else if (!trailHistory.isEmpty()) {
                trailHistory.removeFirst();
            }

            setupAnimationStates();
        } else {
            handleAbilities();
        }
    }

    private void handleAbilities() {
        if (this.isDashing()) {
            if (--dashTimer <= 0) {
                this.setDashing(false);
            } else {
                AABB hitbox = this.getBoundingBox().inflate(1.0, 0.0, 1.0);
                List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, hitbox);
                for (LivingEntity target : targets) {
                    if (target != this && target != this.getControllingPassenger()) {
                        target.hurt(this.damageSources().mobAttack(this), 12.0F);
                        target.knockback(2.0, this.getX() - target.getX(), this.getZ() - target.getZ());
                        this.playSound(SoundEvents.ANVIL_LAND, 1.0F, 0.5F);
                    }
                }
            }
        }
        if (dashCooldown > 0) dashCooldown--;

        if (this.isRoaring()) {
            if (--roarTimer <= 0) this.setRoaring(false);
        }
        if (roarCooldown > 0) roarCooldown--;

        if (this.isUnicornMode()) {
            if (--unicornTimer <= 0) {
                this.setUnicornMode(false);
                this.playSound(SoundEvents.BEACON_DEACTIVATE, 1.0F, 0.5F);
            }
            if (this.tickCount % 5 == 0) {
                ((ServerLevel)this.level()).sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY()+1, this.getZ(), 1, 0.2, 0.2, 0.2, 0.05);
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(ReverieModItems.RAINBOW_DROPPING.get())) {
            if (!this.level().isClientSide) {
                this.setUnicornMode(true);
                this.unicornTimer = 2400;
                if (!player.getAbilities().instabuild) stack.shrink(1);
                this.playSound(SoundEvents.TOTEM_USE, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    private void setupAnimationStates() {
        if (this.isUnicornMode() && !this.onGround()) {
            flyState.startIfStopped(this.tickCount);
            walkState.stop();
            idleState.stop();
            return;
        } else {
            flyState.stop();
        }

        if (this.isDashing()) {
            dashState.startIfStopped(this.tickCount);
            walkState.stop();
            return;
        } else {
            dashState.stop();
        }

        if (this.isRoaring()) {
            roarState.startIfStopped(this.tickCount);
            return;
        } else {
            roarState.stop();
        }

        if (this.getDeltaMovement().horizontalDistanceSqr() > 0.001) {
            walkState.startIfStopped(this.tickCount);
            idleState.stop();
        } else {
            idleState.startIfStopped(this.tickCount);
            walkState.stop();
        }
    }

    public void triggerDash() {
        if (dashCooldown <= 0) {
            this.setDashing(true);
            this.dashTimer = 20;
            this.dashCooldown = 100;
            this.playSound(SoundEvents.HOGLIN_ANGRY, 1.5F, 0.5F);
        }
    }

    public void triggerRoar() {
        if (roarCooldown <= 0) {
            this.setRoaring(true);
            this.roarTimer = 40;
            this.roarCooldown = 200;
            this.playSound(SoundEvents.HOGLIN_DEATH, 1.5F, 0.5F);
        }
    }

    public boolean isDashing() { return this.entityData.get(DASHING); }
    public void setDashing(boolean val) { this.entityData.set(DASHING, val); }
    public boolean isRoaring() { return this.entityData.get(ROARING); }
    public void setRoaring(boolean val) { this.entityData.set(ROARING, val); }
    public boolean isUnicornMode() { return this.entityData.get(UNICORN_MODE); }
    public void setUnicornMode(boolean val) { this.entityData.set(UNICORN_MODE, val); }

    @Override public boolean isTamed() { return true; }
    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob p) { return ReverieModEntities.HOG.get().create(l); }
    @Override public boolean isFood(ItemStack s) { return s.is(Items.GOLDEN_CARROT); }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.HOGLIN_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource p) { return SoundEvents.HOGLIN_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.HOGLIN_DEATH; }
    @Override protected void playStepSound(BlockPos p, BlockState s) { this.playSound(SoundEvents.HOGLIN_STEP, 1.0F, 0.5F); }
}