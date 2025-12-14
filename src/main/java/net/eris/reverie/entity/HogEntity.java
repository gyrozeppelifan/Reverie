package net.eris.reverie.entity;

import net.eris.reverie.init.ReverieModEntities;
import net.eris.reverie.init.ReverieModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.world.effect.MobEffects;
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
import net.minecraftforge.network.PlayMessages;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

public class HogEntity extends AbstractHorse {

    public final AnimationState idleState = new AnimationState();
    public final AnimationState walkState = new AnimationState();
    public final AnimationState dashState = new AnimationState();
    public final AnimationState roarState = new AnimationState();
    public final AnimationState flyState = new AnimationState();

    private static final EntityDataAccessor<Boolean> DASHING = SynchedEntityData.defineId(HogEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ROARING = SynchedEntityData.defineId(HogEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> UNICORN_MODE = SynchedEntityData.defineId(HogEntity.class, EntityDataSerializers.BOOLEAN);

    private int dashTimer = 0;
    private int dashCooldown = 0;
    private int roarTimer = 0;
    private int roarCooldown = 0;
    private int unicornTimer = 0;
    public float currentSpeed = 0.0F;

    public final LinkedList<Vec3> trailHistory = new LinkedList<>();

    // FIX: NET GÖKKUŞAĞI RENKLERİ (R, G, B) - ROYGBIV
    private static final float[][] RAINBOW_COLORS = {
            {1.0F, 0.0F, 0.0F}, // Kırmızı
            {1.0F, 0.5F, 0.0F}, // Turuncu
            {1.0F, 1.0F, 0.0F}, // Sarı
            {0.0F, 1.0F, 0.0F}, // Yeşil
            {0.0F, 0.5F, 1.0F}, // Açık Mavi
            {0.0F, 0.0F, 1.0F}, // Koyu Mavi (Lacivert gibi)
            {0.5F, 0.0F, 1.0F}  // Mor
    };

    public HogEntity(EntityType<? extends AbstractHorse> type, Level level) {
        super(type, level);
        this.xpReward = 15;
    }

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
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.JUMP_STRENGTH, 1.5D);
    }

    @Override
    public boolean isSaddled() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return !this.isVehicle();
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isAlive()) {
            LivingEntity passenger = this.getControllingPassenger();
            if (this.isVehicle() && passenger != null) {
                LivingEntity rider = passenger;

                // 1. UNICORN MODU
                if (this.isUnicornMode()) {
                    this.setYRot(rider.getYRot());
                    this.yRotO = this.getYRot();
                    this.setXRot(rider.getXRot());
                    this.setRot(this.getYRot(), this.getXRot());
                    this.yBodyRot = this.getYRot();
                    this.yHeadRot = this.yBodyRot;

                    float speed = (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 2.5F;
                    Vec3 move = new Vec3(0, 0, rider.zza).yRot(-this.getYRot() * ((float)Math.PI / 180F));
                    double lookY = -Math.sin(this.getXRot() * ((float)Math.PI / 180F));
                    double vertical = (Math.abs(rider.zza) > 0) ? lookY * speed : 0;
                    if (this.isJumping) vertical += 0.3;

                    this.setDeltaMovement(this.getDeltaMovement().add(move.x * speed * 0.1, vertical * 0.1, move.z * speed * 0.1));
                    this.move(MoverType.SELF, this.getDeltaMovement());
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.9, 0.9, 0.9));
                    return;
                }

                // 2. TANK SÜRÜŞÜ
                float targetYaw = rider.getYRot();
                float yawDiff = Mth.wrapDegrees(targetYaw - this.yBodyRot);
                float maxTurn = this.isDashing() ? 1.5F : 4.5F;
                float turnAmount = Mth.clamp(yawDiff, -maxTurn, maxTurn);

                this.yBodyRot += turnAmount;
                this.yBodyRot = Mth.wrapDegrees(this.yBodyRot);
                this.setYRot(this.yBodyRot);
                this.yHeadRot = this.yBodyRot;
                this.yRotO = this.yBodyRot;

                double maxSpeedAttr = this.getAttributeValue(Attributes.MOVEMENT_SPEED);
                if (this.isDashing()) maxSpeedAttr *= 1.5D;

                if (rider.zza > 0) {
                    float acceleration = this.isDashing() ? 0.08F : 0.015F;
                    this.currentSpeed += acceleration;
                    if (this.currentSpeed > maxSpeedAttr) this.currentSpeed = (float) maxSpeedAttr;
                } else if (rider.zza < 0) {
                    float backSpeed = (float)maxSpeedAttr * 0.3F;
                    this.currentSpeed -= 0.01F;
                    if (this.currentSpeed < -backSpeed) this.currentSpeed = -backSpeed;
                } else {
                    this.currentSpeed *= 0.9F;
                }

                Vec3 forward = new Vec3(0, 0, 1).yRot(-this.yBodyRot * ((float)Math.PI / 180F));
                double vertical = this.getDeltaMovement().y;
                if (!this.onGround()) vertical -= 0.08D;

                // ZIPLAMA (Değişken)
                if (this.playerJumpPendingScale > 0.0F && !this.isJumping && this.onGround()) {
                    double jumpPower = this.getCustomJump() * (double)this.playerJumpPendingScale;

                    if (this.hasEffect(MobEffects.JUMP)) {
                        jumpPower += (double)((float)(this.getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.1F);
                    }

                    Vec3 currentVel = this.getDeltaMovement();
                    this.setDeltaMovement(currentVel.x, jumpPower, currentVel.z);

                    this.setIsJumping(true);
                    this.hasImpulse = true;

                    if (jumpPower > 1.0) jumpPower = 1.0;
                    this.playerJumpPendingScale = 0.0F;
                }

                if (!this.isDashing()) {
                    this.setDeltaMovement(forward.x * this.currentSpeed, vertical, forward.z * this.currentSpeed);
                } else {
                    this.setDeltaMovement(this.getDeltaMovement().x, vertical, this.getDeltaMovement().z);
                }

                this.move(MoverType.SELF, this.getDeltaMovement());

                if (this.level().isClientSide) {
                    if (!this.isDashing()) {
                        this.setDeltaMovement(forward.x * this.currentSpeed, vertical, forward.z * this.currentSpeed);
                    }
                }

                return;
            }
        }
        this.currentSpeed = 0;
        super.travel(travelVector);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        if (fallDistance > 2.5F) {
            this.playSound(SoundEvents.GENERIC_BIG_FALL, 1.5F, 0.8F);
            this.playSound(SoundEvents.GENERIC_EXPLODE, 0.5F, 1.0F);

            if (!this.level().isClientSide) {
                AABB impactArea = this.getBoundingBox().inflate(4.0, 1.0, 4.0);
                List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, impactArea);
                LivingEntity rider = this.getControllingPassenger();

                for (LivingEntity target : targets) {
                    boolean isRider = (rider != null && target == rider);
                    boolean isMe = (target == this);

                    if (!isMe && !isRider) {
                        float damage = 1.0F + (fallDistance * 2.5F);
                        target.hurt(this.damageSources().mobAttack(this), damage);

                        double dx = target.getX() - this.getX();
                        double dz = target.getZ() - this.getZ();
                        target.knockback(1.0D, -dx, -dz);
                        target.push(0, 0.4D, 0);
                    }
                }
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 2, 1.0, 0.0, 1.0, 0.0);
                    serverLevel.sendParticles(ParticleTypes.POOF, this.getX(), this.getY(), this.getZ(), 25, 2.0, 0.5, 2.0, 0.05);
                }
            }
            return super.causeFallDamage(fallDistance, 0.0F, source);
        }
        return super.causeFallDamage(fallDistance, multiplier, source);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            // --- TRAIL & EFEKTLER ---
            if (this.isUnicornMode() || this.isDashing()) {
                float pitch = this.isUnicornMode() ? this.getXRot() : 0;
                Vec3 forward = Vec3.directionFromRotation(pitch, this.getYRot());
                Vec3 up = new Vec3(0, 1, 0);
                Vec3 right = forward.cross(up).normalize();
                Vec3 backward = forward.scale(-1.5);

                // UNICORN RAINBOW TRAIL (NET RENKLER)
                if (this.isUnicornMode()) {
                    int bands = RAINBOW_COLORS.length; // 7 şerit
                    float totalWidth = 1.4F;
                    float spacing = totalWidth / (bands - 1);

                    for (int i = 0; i < bands; i++) {
                        float offset = (i * spacing) - (totalWidth / 2.0F);

                        // FIX: Renkleri direkt diziden alıyoruz
                        float r = RAINBOW_COLORS[i][0];
                        float g = RAINBOW_COLORS[i][1];
                        float b = RAINBOW_COLORS[i][2];

                        double px = this.getX() + backward.x + (right.x * offset);
                        double py = this.getY() + 0.8 + backward.y;
                        double pz = this.getZ() + backward.z + (right.z * offset);

                        // FIX: Boyut küçültüldü (3.5 -> 1.8)
                        for(int k=0; k<2; k++) {
                            this.level().addParticle(new DustParticleOptions(new Vector3f(r, g, b), 1.8F),
                                    px + (this.random.nextDouble() - 0.5) * 0.2,
                                    py + (this.random.nextDouble() - 0.5) * 0.2,
                                    pz + (this.random.nextDouble() - 0.5) * 0.2,
                                    0, 0, 0);
                        }
                    }

                    // FIX: Kara duman silindi. Sadece pırıltı kaldı.
                    if (this.random.nextInt(2) == 0) {
                        this.level().addParticle(ParticleTypes.FIREWORK,
                                this.getX() + backward.x, this.getY() + 0.8, this.getZ() + backward.z,
                                (this.random.nextDouble() - 0.5) * 0.1, 0.1, (this.random.nextDouble() - 0.5) * 0.1);
                    }
                }

                // DASH DUMANI (Geri CAMPFIRE yapıldı, daha kibar)
                if (this.isDashing()) {
                    for(int i = 0; i < 6; i++) {
                        this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                                this.getX() + backward.x + (this.random.nextDouble() - 0.5) * 0.8,
                                this.getY() + 0.5,
                                this.getZ() + backward.z + (this.random.nextDouble() - 0.5) * 0.8,
                                0, 0.08, 0);
                    }
                }
            }

            // --- ANIMASYON KONTROL ---
            boolean isMoving = Math.abs(this.currentSpeed) > 0.01F || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6;

            if (this.isUnicornMode()) {
                if (this.onGround()) {
                    this.walkState.startIfStopped(this.tickCount); stopAnimationsExcept(this.walkState);
                } else {
                    this.flyState.startIfStopped(this.tickCount); stopAnimationsExcept(this.flyState);
                }
            }
            else if (this.isRoaring()) {
                this.roarState.startIfStopped(this.tickCount); stopAnimationsExcept(this.roarState);
            }
            else if (isMoving) {
                this.walkState.startIfStopped(this.tickCount);
                if (this.dashState.isStarted()) this.dashState.stop();
                if (this.idleState.isStarted()) this.idleState.stop();
                if (this.flyState.isStarted()) this.flyState.stop();
                if (this.roarState.isStarted()) this.roarState.stop();
            }
            else {
                this.idleState.startIfStopped(this.tickCount);
                stopAnimationsExcept(this.idleState);
            }

        } else {
            handleAbilities();
        }
    }

    private void handleAbilities() {
        if (this.isDashing()) {
            if (--dashTimer <= 0) {
                this.setDashing(false);
            } else {
                AABB hitbox = this.getBoundingBox().inflate(1.2, 0.5, 1.2);
                List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, hitbox);
                LivingEntity rider = this.getControllingPassenger();

                for (LivingEntity target : targets) {
                    boolean isRider = (rider != null && target == rider);
                    if (target != this && !isRider) {
                        target.hurt(this.damageSources().mobAttack(this), 10.0F);
                        double dx = target.getX() - this.getX();
                        double dz = target.getZ() - this.getZ();
                        target.knockback(1.2D, -dx, -dz);
                        this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 0.8F);
                    }
                }
            }
        }
        if (dashCooldown > 0) dashCooldown--;
        if (this.isRoaring()) { if (--roarTimer <= 0) this.setRoaring(false); }
        if (roarCooldown > 0) roarCooldown--;
        if (this.isUnicornMode()) {
            if (--unicornTimer <= 0) {
                this.setUnicornMode(false);
                this.playSound(SoundEvents.BEACON_DEACTIVATE, 1.0F, 0.5F);
            }
        }
    }

    private void stopAnimationsExcept(AnimationState ignore) {
        if (ignore != this.idleState) this.idleState.stop();
        if (ignore != this.walkState) this.walkState.stop();
        if (ignore != this.dashState) this.dashState.stop();
        if (ignore != this.roarState) this.roarState.stop();
        if (ignore != this.flyState) this.flyState.stop();
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

    public void triggerDash() {
        if (dashCooldown <= 0 && this.onGround()) {
            this.setDashing(true);
            this.dashTimer = 15;
            this.dashCooldown = 80;

            Vec3 look = this.getLookAngle();
            double boostStrength = 4.0D;
            this.setDeltaMovement(this.getDeltaMovement().add(look.x * boostStrength, 0.4, look.z * boostStrength));

            this.playSound(SoundEvents.CAMEL_DASH, 1.5F, 1.0F);
            this.playSound(SoundEvents.TRIDENT_RIPTIDE_3, 1.0F, 0.5F);
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
    @Override protected void playStepSound(BlockPos p, BlockState s) { this.playSound(SoundEvents.HOGLIN_STEP, 1.2F, 0.5F); }
}