package net.eris.reverie.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.network.NetworkHooks;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity.RemovalReason;

import net.eris.reverie.init.ReverieModEntities;

public class GobletEntity extends Monster {
    // growthTime tanımı
    private static final EntityDataAccessor<Integer> GROWTH_TIME =
            SynchedEntityData.defineId(GobletEntity.class, EntityDataSerializers.INT);
    private static final int INITIAL_GROWTH = 20 * 20; // 400 tick = ~20 saniye

    public final AnimationState animationState0 = new AnimationState();

    // GobletEntity.java içinde
    /** Kalan büyüme tick’ini verir */
    public int getRemainingGrowth() {
        return this.entityData.get(GROWTH_TIME);
    }

    public GobletEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ReverieModEntities.GOBLET.get(), world);
    }

    public GobletEntity(EntityType<GobletEntity> type, Level world) {
        super(type, world);
        setMaxUpStep(0.6f);
        xpReward = 0;
        setNoAi(false);
        // growthTime başlangıcı
        this.entityData.set(GROWTH_TIME, INITIAL_GROWTH);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        // growthTime’ı data tracker’a tanıt
        this.entityData.define(GROWTH_TIME, INITIAL_GROWTH);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.2));
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(4, new FloatGoal(this));
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



        // client tarafı animasyonu
        if (this.level().isClientSide) {
            this.animationState0.animateWhen(true, this.tickCount);
        }

        // server tarafı: growthTime azaltma & dönüşüm kontrol
        if (!this.level().isClientSide) {
            int time = this.entityData.get(GROWTH_TIME);
            if (time <= 0) {
                this.playSound(SoundEvents.FROG_TONGUE, 0.5f,
                        0.9f + (random.nextFloat() - 0.5f) * 0.2f);
                doGrowthTransform();
                return;
            }
            this.entityData.set(GROWTH_TIME, time - 1);
        }
    }

    /**
     * Renderer’da scale için kullanabileceğin metod.
     * Eğer kalan time < 100 tick ise sinüsle salınım yapar, değilse 1.0f.
     */
    public float getStretchScale(float partialTicks) {
        int time = this.entityData.get(GROWTH_TIME);
        if (time >= 100) return 1.0f;
        float phase = ((100 - time) + partialTicks) * (2 * (float)Math.PI / 20f);
        return 1.0f + (float)Math.sin(phase) * 0.2f;
    }

    /**
     * growthTime bittiğinde rastgele yeni goblin spawnlayıp bu entity’yi kaldırır.
     */
    private void doGrowthTransform() {
        double x = this.getX(), y = this.getY(), z = this.getZ();
        float yaw = this.getYRot(), pitch = this.getXRot();
        int roll = this.random.nextInt(100);
        EntityType<? extends Mob> newType;

        if (roll < 50) {
            newType = ReverieModEntities.GOBLIN.get();
        } else if (roll < 90) {
            newType = ReverieModEntities.SHOOTER_GOBLIN.get();
        } else {
            newType = ReverieModEntities.GOBLIN_BRUTE.get();
        }

        Mob baby = newType.create(this.level());
        if (baby != null) {
            baby.moveTo(x, y, z, yaw, pitch);
            this.level().addFreshEntity(baby);
        }
        this.remove(RemovalReason.DISCARDED);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.MAX_HEALTH, 10)
                .add(Attributes.ARMOR, 0)
                .add(Attributes.ATTACK_DAMAGE, 3)
                .add(Attributes.FOLLOW_RANGE, 16);
    }

    public static void init() {
        // ek init gerekirse buraya
    }
}
