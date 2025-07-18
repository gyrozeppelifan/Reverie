package net.eris.reverie.entity;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.network.NetworkHooks;

import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.Difficulty;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.item.ItemStack;

import net.eris.reverie.init.ReverieModEntities;

import java.util.Optional;
import java.util.UUID;

public class GoblinEntity extends Monster {
    public final AnimationState animationState1 = new AnimationState();
    public final AnimationState animationState2 = new AnimationState();
    public final AnimationState animationState3 = new AnimationState();

    // UUID ile target sync accessor
    private static final EntityDataAccessor<Optional<UUID>> TARGET_UUID =
            SynchedEntityData.defineId(GoblinEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    // Sağ elde tutulacak item’i senkronize etmek için
    private static final EntityDataAccessor<ItemStack> HELD_ITEM =
            SynchedEntityData.defineId(GoblinEntity.class, EntityDataSerializers.ITEM_STACK);

    // attack2 animasyonu süresi (tick cinsinden)
    private static final int ATTACK2_DURATION = 10;
    private int attack2Ticks = 0;

    public GoblinEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ReverieModEntities.GOBLIN.get(), world);
    }

    public GoblinEntity(EntityType<GoblinEntity> type, Level world) {
        super(type, world);
        setMaxUpStep(0.6f);
        xpReward = 0;
        setNoAi(false);
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

    /** Renderer’ın kontrol edeceği metot */
    public boolean hasTargetClient() {
        return this.entityData.get(TARGET_UUID).isPresent();
    }

    /** Sunucudan client’a güncellenecek şekilde item’i set et */
    public void setHeldItem(ItemStack stack) {
        this.entityData.set(HELD_ITEM, stack);
    }

    /** ItemInHandLayer bunu çağırarak renderlar */
    @Override
    public ItemStack getMainHandItem() {
        ItemStack held = this.entityData.get(HELD_ITEM);
        // Eğer özel alan boşsa, vanilla yolundan (super) çek!
        return (held == null || held.isEmpty()) ? super.getMainHandItem() : held;
    }

    /** Sunucuda saldırı gerçekleştiğinde client’a animasyon event’i gönder */
    @Override
    public boolean doHurtTarget(Entity targetEntity) {
        boolean result = super.doHurtTarget(targetEntity);
        if (result && !this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte)4);
        }
        return result;
    }

    /** Client tarafı event’i yakala ve attack2 animasyonunu başlat */
    @Override
    public void handleEntityEvent(byte id) {
        super.handleEntityEvent(id);
        if (id == 4) {
            this.animationState3.animateWhen(true, this.tickCount);
            this.attack2Ticks = ATTACK2_DURATION;
        }
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                this,
                Player.class,
                10,
                true,
                false,
                LivingEntity::isAlive
        ));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new FloatGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.animationState1.animateWhen(true, this.tickCount);
            this.animationState2.animateWhen(true, this.tickCount);
            if (attack2Ticks > 0) {
                attack2Ticks--;
                if (attack2Ticks == 0) {
                    this.animationState3.stop();
                }
            }
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

    public static void init() {
        SpawnPlacements.register(
                ReverieModEntities.GOBLIN.get(),
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
        builder = builder.add(Attributes.MAX_HEALTH, 10);
        builder = builder.add(Attributes.ARMOR, 0);
        builder = builder.add(Attributes.ATTACK_DAMAGE, 3);
        builder = builder.add(Attributes.FOLLOW_RANGE, 16);
        return builder;
    }
}