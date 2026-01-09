package net.eris.reverie.entity.custom;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.eris.reverie.entity.ai.FolkBrain;
import net.eris.reverie.init.ReverieActivities; // Bunu import etmeyi unutma
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag; // Tag importu lazım olabilir
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

public class FolkEntity extends PathfinderMob implements Merchant {
    private static final EntityDataAccessor<Integer> DATA_PROFESSION = SynchedEntityData.defineId(FolkEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(FolkEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WORKING_TICKS = SynchedEntityData.defineId(FolkEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LEVEL = SynchedEntityData.defineId(FolkEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_XP = SynchedEntityData.defineId(FolkEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_PANICKING = SynchedEntityData.defineId(FolkEntity.class, EntityDataSerializers.BOOLEAN);

    private final FolkTradeManager tradeManager;
    private final FolkGossipManager gossipManager;

    @Nullable private Player tradingPlayer;
    @Nullable private MerchantOffers offers;
    private long lastRestockGameTime = 0;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState panicAnimationState = new AnimationState();
    public final AnimationState workAnimationState = new AnimationState();
    public final AnimationState gossipAnimationState = new AnimationState();

    public FolkEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setCanPickUpLoot(true);
        this.tradeManager = new FolkTradeManager(this);
        this.gossipManager = new FolkGossipManager(this);

        if (this.getNavigation() instanceof GroundPathNavigation groundNav) {
            groundNav.setCanOpenDoors(true);
        }
        this.getNavigation().setCanFloat(true);

        if (this.getFolkLevel() == 0) this.setFolkLevel(1);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    public FolkGossipManager getGossipManager() { return this.gossipManager; }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource pSource, float pAmount) {
        if (pSource.getEntity() instanceof Player player) {
            this.gossipManager.addGossip(player.getUUID(), net.minecraft.world.entity.ai.gossip.GossipType.MAJOR_NEGATIVE, 25);
        }
        return super.hurt(pSource, pAmount);
    }

    @Override
    public void handleEntityEvent(byte pId) {
        if (pId == 14) {
            this.addParticlesAroundSelf(ParticleTypes.HAPPY_VILLAGER);
        } else {
            super.handleEntityEvent(pId);
        }
    }

    private void addParticlesAroundSelf(ParticleOptions pParticleOption) {
        for(int i = 0; i < 5; ++i) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            this.level().addParticle(pParticleOption, this.getRandomX(1.0D), this.getRandomY() + 1.0D, this.getRandomZ(1.0D), d0, d1, d2);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_PROFESSION, 0);
        this.entityData.define(DATA_VARIANT, 0);
        this.entityData.define(WORKING_TICKS, 0);
        this.entityData.define(DATA_LEVEL, 1);
        this.entityData.define(DATA_XP, 0);
        this.entityData.define(DATA_IS_PANICKING, false);
    }

    public boolean isHoldingWeapon() {
        ItemStack stack = this.getMainHandItem();
        return stack.getItem() instanceof net.minecraft.world.item.SwordItem ||
                stack.getItem() instanceof net.minecraft.world.item.AxeItem ||
                stack.getItem() instanceof net.minecraft.world.item.BowItem ||
                stack.getItem() instanceof net.minecraft.world.item.CrossbowItem ||
                stack.getItem() instanceof net.minecraft.world.item.TridentItem;
    }

    public boolean tryRestockTrades() {
        if (this.level().isClientSide()) return false;
        long time = this.level().getGameTime();
        if (time - this.lastRestockGameTime > 2000) {
            this.lastRestockGameTime = time;
            for (MerchantOffer offer : this.getOffers()) { offer.resetUses(); }
            return true;
        }
        return false;
    }

    public boolean isTrading() { return this.tradingPlayer != null; }

    @Override
    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        if (this.isAlive() && !this.isTrading() && !this.isBaby()) {
            if (this.isSleeping()) return InteractionResult.FAIL;

            if (pHand == InteractionHand.MAIN_HAND) {
                if (this.getProfessionId() != 0) {
                    if (!this.level().isClientSide) {
                        this.setTradingPlayer(pPlayer);
                        this.openTradingScreen(pPlayer, this.getDisplayName(), this.getFolkLevel());
                    }
                    return InteractionResult.sidedSuccess(this.level().isClientSide);
                }
            }
        }
        return super.mobInteract(pPlayer, pHand);
    }

    @Override public void notifyTrade(MerchantOffer offer) { this.tradeManager.onTrade(offer); }

    @Override
    public MerchantOffers getOffers() {
        if (this.offers == null) { this.offers = new MerchantOffers(); this.tradeManager.updateTrades(this.offers); }
        if (this.offers.isEmpty() && this.getProfessionId() != 0) { this.tradeManager.updateTrades(this.offers); }
        return this.offers;
    }

    @Override
    public void openTradingScreen(Player pPlayer, Component pDisplayName, int pLevel) {
        this.gossipManager.applyPriceModifiers(this.getOffers(), pPlayer);
        OptionalInt optionalint = pPlayer.openMenu(new SimpleMenuProvider((id, inventory, player) -> new MerchantMenu(id, inventory, this), pDisplayName));
        if (optionalint.isPresent()) {
            MerchantOffers merchantoffers = this.getOffers();
            if (!merchantoffers.isEmpty()) pPlayer.sendMerchantOffers(optionalint.getAsInt(), merchantoffers, pLevel, this.getFolkXp(), true, true);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            setupAnimationStates();
        } else {
            boolean actuallyPanicking = this.getBrain().isActive(net.minecraft.world.entity.schedule.Activity.PANIC);
            this.entityData.set(DATA_IS_PANICKING, actuallyPanicking);
        }
        if (this.getWorkingTicks() > 0) {
            this.setWorkingTicks(this.getWorkingTicks() - 1);
        }
    }

    private void setupAnimationStates() {
        if (this.isSleeping()) {
            this.idleAnimationState.stop();
            this.workAnimationState.stop();
            this.panicAnimationState.stop();
            this.gossipAnimationState.stop();
            return;
        }
        if (isPanicking()) {
            this.idleAnimationState.stop();
            this.workAnimationState.stop();
            this.gossipAnimationState.stop();
            this.panicAnimationState.startIfStopped(this.tickCount);
        }
        else if (this.getWorkingTicks() > 0) {
            this.idleAnimationState.stop();
            this.panicAnimationState.stop();
            this.gossipAnimationState.stop();
            this.workAnimationState.startIfStopped(this.tickCount);
        }
        else if (this.getBrain().isActive(ReverieActivities.MEET.get()) && this.getDeltaMovement().horizontalDistanceSqr() < 0.001) {
            this.idleAnimationState.stop();
            this.panicAnimationState.stop();
            this.workAnimationState.stop();
            this.gossipAnimationState.startIfStopped(this.tickCount);
        }
        else {
            this.panicAnimationState.stop();
            this.workAnimationState.stop();
            this.gossipAnimationState.stop();
            this.idleAnimationState.startIfStopped(this.tickCount);
        }
    }

    public boolean isPanicking() { return this.entityData.get(DATA_IS_PANICKING); }

    @Override public boolean isWithinMeleeAttackRange(LivingEntity pEntity) { return this.getPerceivedTargetDistanceSquareForMeleeAttack(pEntity) <= this.getMeleeAttackRangeSqr(pEntity) + 3.0D; }

    @Override
    public boolean doHurtTarget(Entity pEntity) {
        boolean flag = super.doHurtTarget(pEntity);
        if (flag) { this.swing(InteractionHand.MAIN_HAND); this.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1.0F, 1.0F); }
        return flag;
    }

    @Override
    protected Brain.Provider<FolkEntity> brainProvider() {
        return Brain.provider(
                ImmutableList.of(
                        MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.PATH, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                        MemoryModuleType.ATTACK_TARGET, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.ATTACK_COOLING_DOWN,
                        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
                        MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, MemoryModuleType.HOME,
                        MemoryModuleType.DOORS_TO_CLOSE, MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.MEETING_POINT
                ),
                ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.HURT_BY)
        );
    }

    @Override protected Brain<?> makeBrain(Dynamic<?> pDynamic) { return FolkBrain.create(this.brainProvider().makeBrain(pDynamic)); }
    @SuppressWarnings("unchecked") @Override public Brain<FolkEntity> getBrain() { return (Brain<FolkEntity>) super.getBrain(); }
    @Override protected void customServerAiStep() { this.level().getProfiler().push("folkBrain"); FolkBrain.tick(this); this.getBrain().tick((ServerLevel) this.level(), this); this.level().getProfiler().pop(); super.customServerAiStep(); }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("Profession", this.getProfessionId());
        pCompound.putInt("Variant", this.getVariant());
        pCompound.putInt("FolkLevel", this.getFolkLevel());
        pCompound.putInt("FolkXp", this.getFolkXp());
        pCompound.putLong("LastRestock", this.lastRestockGameTime);
        if (this.offers != null) pCompound.put("Offers", this.offers.createTag());

        // --- GOSSIP KAYDI (KESİN ÇÖZÜM) ---
        // result() veya getValue() ile uğraşmadan direkt Tag'e çeviriyoruz
        Tag gossipTag = this.gossipManager.getGossips().store(net.minecraft.nbt.NbtOps.INSTANCE);
        pCompound.put("Gossips", gossipTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.setProfessionId(pCompound.getInt("Profession"));
        this.setVariant(pCompound.getInt("Variant"));
        int loadedLevel = pCompound.getInt("FolkLevel");
        this.setFolkLevel(loadedLevel <= 0 ? 1 : loadedLevel);
        this.setFolkXp(pCompound.getInt("FolkXp"));
        this.lastRestockGameTime = pCompound.getLong("LastRestock");
        if (pCompound.contains("Offers")) this.offers = new MerchantOffers(pCompound.getCompound("Offers"));

        // --- GOSSIP YÜKLEME ---
        if (pCompound.contains("Gossips")) {
            this.gossipManager.getGossips().update(new com.mojang.serialization.Dynamic<>(net.minecraft.nbt.NbtOps.INSTANCE, pCompound.get("Gossips")));
        }
    }

    // Getters & Setters
    public int getProfessionId() { return this.entityData.get(DATA_PROFESSION); }
    public void setProfessionId(int id) { this.entityData.set(DATA_PROFESSION, id); }
    public int getVariant() { return this.entityData.get(DATA_VARIANT); }
    public void setVariant(int id) { this.entityData.set(DATA_VARIANT, id); }
    public int getWorkingTicks() { return this.entityData.get(WORKING_TICKS); }
    public void setWorkingTicks(int ticks) { this.entityData.set(WORKING_TICKS, ticks); }
    public int getFolkLevel() { return this.entityData.get(DATA_LEVEL); }
    public void setFolkLevel(int level) { this.entityData.set(DATA_LEVEL, level); }
    public int getFolkXp() { return this.entityData.get(DATA_XP); }
    public void setFolkXp(int xp) { this.entityData.set(DATA_XP, xp); }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource pDamageSource) {
        super.die(pDamageSource);
        releasePoi();
    }

    private void releasePoi() {
        if (this.level() instanceof ServerLevel serverLevel) {
            if (this.getBrain().hasMemoryValue(MemoryModuleType.JOB_SITE)) {
                GlobalPos jobPos = this.getBrain().getMemory(MemoryModuleType.JOB_SITE).get();
                if (jobPos.dimension() == serverLevel.dimension()) serverLevel.getPoiManager().release(jobPos.pos());
            }
            if (this.getBrain().hasMemoryValue(MemoryModuleType.HOME)) {
                GlobalPos homePos = this.getBrain().getMemory(MemoryModuleType.HOME).get();
                if (homePos.dimension() == serverLevel.dimension()) serverLevel.getPoiManager().release(homePos.pos());
                // --- EKLENEN KISIM: Buluşma Noktasını Bırakma ---
                if (this.getBrain().hasMemoryValue(MemoryModuleType.MEETING_POINT)) {
                    GlobalPos meetPos = this.getBrain().getMemory(MemoryModuleType.MEETING_POINT).get();
                    if (meetPos.dimension() == serverLevel.dimension()) serverLevel.getPoiManager().release(meetPos.pos());
                }
            }
        }
    }

    @Override public void setTradingPlayer(@Nullable Player pPlayer) { this.tradingPlayer = pPlayer; }
    @Override @Nullable public Player getTradingPlayer() { return this.tradingPlayer; }
    @Override public void overrideOffers(MerchantOffers pOffers) { this.offers = pOffers; }
    @Override public void notifyTradeUpdated(ItemStack pStack) {}
    @Override public int getVillagerXp() { return this.getFolkXp(); }
    @Override public void overrideXp(int pXp) { this.setFolkXp(pXp); }
    @Override public boolean showProgressBar() { return true; }
    @Override public SoundEvent getNotifyTradeSound() { return SoundEvents.VILLAGER_TRADE; }
    @Override public boolean isClientSide() { return this.level().isClientSide; }
}