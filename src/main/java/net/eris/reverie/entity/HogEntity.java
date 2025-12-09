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
import net.minecraftforge.network.PlayMessages;
import org.jetbrains.annotations.Nullable;

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
    private float currentSpeed = 0.0F;

    public final LinkedList<Vec3> trailHistory = new LinkedList<>();

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
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.JUMP_STRENGTH, 1.0D);
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
                float maxTurn = this.isDashing() ? 0.8F : 4.5F;
                float turnAmount = Mth.clamp(yawDiff, -maxTurn, maxTurn);

                this.yBodyRot += turnAmount;
                this.setYRot(this.yBodyRot);
                this.yHeadRot = this.yBodyRot;
                this.yRotO = this.yBodyRot;

                double maxSpeedAttr = this.getAttributeValue(Attributes.MOVEMENT_SPEED);
                if (this.isDashing()) maxSpeedAttr *= 2.5D;

                float targetSpeed = (rider.zza > 0) ? (float)maxSpeedAttr : 0.0F;
                if (rider.zza < 0) targetSpeed = (float)maxSpeedAttr * -0.4F;

                this.currentSpeed = Mth.lerp(0.1F, this.currentSpeed, targetSpeed);

                Vec3 forward = new Vec3(0, 0, 1).yRot(-this.yBodyRot * ((float)Math.PI / 180F));

                double vertical = this.getDeltaMovement().y;
                if (!this.onGround()) vertical -= 0.08D;

                if (this.isJumping && this.onGround()) {
                    vertical = this.getCustomJump();
                    this.currentSpeed *= 1.3F;
                }

                this.setDeltaMovement(forward.x * this.currentSpeed, vertical, forward.z * this.currentSpeed);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.currentSpeed *= 0.96F;
                return;
            }
        }
        super.travel(travelVector);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        if (fallDistance > 2.5F) {
            this.playSound(SoundEvents.ANVIL_LAND, 1.0F, 0.5F);
            this.playSound(SoundEvents.GENERIC_EXPLODE, 0.5F, 1.2F);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            }
            return super.causeFallDamage(fallDistance, multiplier * 0.3F, source);
        }
        return super.causeFallDamage(fallDistance, multiplier, source);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            // --- TRAIL FIX (DÖNÜŞLÜ) ---
            if (this.isUnicornMode() || this.isDashing()) {
                // 1. Arkaya doğru lokal vektör (0, 0, -1.5)
                // 2. Bunu yBodyRot ile döndür.
                // yRot(angle) metodu radyanda çalışır.
                // Minecraft'ta rotation pozitif değerler saatin tersinedir, o yüzden eksi ile çarpıyoruz.
                Vec3 offset = new Vec3(0, 0, -1.5).yRot(-this.yBodyRot * ((float)Math.PI / 180F));

                // 3. Yükseklik ekle (+1.3)
                Vec3 trailPos = this.position().add(offset).add(0, 1.3, 0);

                trailHistory.add(trailPos);
                if (trailHistory.size() > 30) trailHistory.removeFirst();
            } else if (!trailHistory.isEmpty()) {
                trailHistory.removeFirst();
            }
        } else {
            handleAbilities();
        }
    }

    private void handleAbilities() {
        if (this.isDashing()) {
            if (--dashTimer <= 0) this.setDashing(false);
            else {
                AABB hitbox = this.getBoundingBox().inflate(1.5, 0.5, 1.5);
                List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, hitbox);
                for (LivingEntity target : targets) {
                    if (target != this && target != this.getControllingPassenger()) {
                        target.hurt(this.damageSources().mobAttack(this), 15.0F);
                        target.knockback(2.5, this.getX() - target.getX(), this.getZ() - target.getZ());
                        this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.5F, 0.5F);
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

    public void triggerDash() { if (dashCooldown <= 0) { this.setDashing(true); this.dashTimer = 20; this.dashCooldown = 100; this.playSound(SoundEvents.HOGLIN_ANGRY, 1.5F, 0.5F); } }
    public void triggerRoar() { if (roarCooldown <= 0) { this.setRoaring(true); this.roarTimer = 40; this.roarCooldown = 200; this.playSound(SoundEvents.HOGLIN_DEATH, 1.5F, 0.5F); } }

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