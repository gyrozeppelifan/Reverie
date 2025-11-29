package net.eris.reverie.entity;

import net.eris.reverie.entity.stitched_abilities.AbilityLightning;
import net.eris.reverie.entity.stitched_abilities.AbilityRoar;
import net.eris.reverie.entity.stitched_abilities.StitchedAbility;
import net.eris.reverie.init.ReverieModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PlayMessages;

public class StitchedEntity extends Monster {
    // States: 0=PASSIVE, 1=ELECTROCUTED, 2=STANDUP, 3=ALIVE, 4=WAITING
    private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(StitchedEntity.class, EntityDataSerializers.INT);

    // Parça Slotları
    private static final EntityDataAccessor<ItemStack> DATA_HEAD_ITEM = SynchedEntityData.defineId(StitchedEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> DATA_BODY_ITEM = SynchedEntityData.defineId(StitchedEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> DATA_ARM_ITEM = SynchedEntityData.defineId(StitchedEntity.class, EntityDataSerializers.ITEM_STACK);

    // --- ANİMASYON DURUMLARI (States) ---
    public final AnimationState idleState = new AnimationState();
    public final AnimationState passiveState = new AnimationState(); // Yerde yatış (Sit Idle)
    public final AnimationState electrocutedState = new AnimationState(); // Electricity
    public final AnimationState standupState = new AnimationState();
    public final AnimationState walkState = new AnimationState();
    public final AnimationState walkNoArmsState = new AnimationState();
    public final AnimationState attackState = new AnimationState();
    public final AnimationState roaringState = new AnimationState();
    public final AnimationState sitRoaringState = new AnimationState(); // Yerde kükreme

    private int animationTimer = 0;

    // Yetenek Sistemi
    private StitchedAbility currentAbility = null;
    public int abilityTick = 0;
    private int abilityCooldown = 0;

    public StitchedEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.refreshDimensions();
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
        this.entityData.define(DATA_HEAD_ITEM, ItemStack.EMPTY);
        this.entityData.define(DATA_BODY_ITEM, ItemStack.EMPTY);
        this.entityData.define(DATA_ARM_ITEM, ItemStack.EMPTY);
    }

    // Getter - Setter
    public ItemStack getHeadItem() { return this.entityData.get(DATA_HEAD_ITEM); }
    public void setHeadItem(ItemStack stack) { this.entityData.set(DATA_HEAD_ITEM, stack); }
    public ItemStack getBodyItem() { return this.entityData.get(DATA_BODY_ITEM); }
    public void setBodyItem(ItemStack stack) { this.entityData.set(DATA_BODY_ITEM, stack); }
    public ItemStack getArmItem() { return this.entityData.get(DATA_ARM_ITEM); }
    public void setArmItem(ItemStack stack) { this.entityData.set(DATA_ARM_ITEM, stack); }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("State", this.getState());
        tag.put("HeadItem", getHeadItem().save(new CompoundTag()));
        tag.put("BodyItem", getBodyItem().save(new CompoundTag()));
        tag.put("ArmItem", getArmItem().save(new CompoundTag()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setState(tag.getInt("State"));
        if (tag.contains("HeadItem")) this.setHeadItem(ItemStack.of(tag.getCompound("HeadItem")));
        if (tag.contains("BodyItem")) this.setBodyItem(ItemStack.of(tag.getCompound("BodyItem")));
        if (tag.contains("ArmItem")) this.setArmItem(ItemStack.of(tag.getCompound("ArmItem")));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown() && this.getState() == 3) {
            if (!this.level().isClientSide) {
                ItemStack handItem = player.getItemInHand(hand);
                if (!handItem.isEmpty()) {
                    this.spawnAtLocation(this.getHeadItem());
                    this.setHeadItem(handItem.split(1));
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Kafa Modu Değişti!"));
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_STATE.equals(key)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        int state = this.getState();
        if (state == 0 || state == 1 || state == 4) {
            return EntityDimensions.fixed(1.5f, 0.5f);
        }
        return super.getDimensions(pose);
    }

    @Override
    public void refreshDimensions() {
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();
        super.refreshDimensions();
        this.setPos(d0, d1, d2);
    }

    @Override
    protected void registerGoals() {
        if (getState() == 3) {
            this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0D));
            this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
            this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
            // TODO: MeleeAttackGoal ekle (attackState için)
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    public int getState() { return this.entityData.get(DATA_STATE); }
    public void setState(int state) { this.entityData.set(DATA_STATE, state); }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt lightning) {
        if (getState() == 0) {
            this.setState(1); // ELECTROCUTED
            this.animationTimer = 60;
            this.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 2.0F, 1.0F);
        } else {
            super.thunderHit(level, lightning);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_FIRE) || source.is(net.minecraft.world.damagesource.DamageTypes.ON_FIRE)) {
            return false;
        }
        if (getState() < 3 && !source.isCreativePlayer()) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean isPushable() { return getState() == 3 && super.isPushable(); }

    @Override
    public void push(Entity entity) { if (getState() == 3) super.push(entity); }

    @Override
    protected void doPush(Entity entity) { if (getState() == 3) super.doPush(entity); }

    @Override
    public void tick() {
        super.tick();
        if (abilityCooldown > 0) abilityCooldown--;
        if (this.isOnFire()) this.clearFire();

        if (!this.level().isClientSide) {
            // Yetenek Mantığı
            if (currentAbility != null) {
                abilityTick++;
                if (!currentAbility.tick(this)) {
                    currentAbility.stop(this);
                    currentAbility = null;
                    abilityTick = 0;
                }
            } else {
                handleStateLogic();
            }
        } else {
            // Animasyon Mantığı (Client Sadece)
            setupAnimationStates();
        }
    }

    public void triggerAbility() {
        if (currentAbility != null || abilityCooldown > 0) return;
        ItemStack headItem = this.getHeadItem();
        StitchedAbility abilityToUse = null;

        if (headItem.is(Items.LIGHTNING_ROD)) {
            abilityToUse = new AbilityLightning();
        } else {
            abilityToUse = new AbilityRoar();
        }

        if (abilityToUse != null && abilityToUse.canUse(this)) {
            this.currentAbility = abilityToUse;
            this.abilityTick = 0;
            this.currentAbility.start(this);

            // Eğer yetenek ROAR ise, animasyon state'ini tetikle
            if (abilityToUse instanceof AbilityRoar) {
                this.roaringState.start(this.tickCount);
            }

            this.abilityCooldown = abilityToUse.getDuration() + 100;
        }
    }

    private void handleStateLogic() {
        int currentState = getState();
        if (currentState == 1) {
            if (--animationTimer <= 0) { setState(4); animationTimer = 20; }
        } else if (currentState == 4) {
            if (--animationTimer <= 0) { setState(2); animationTimer = 70; }
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

    // --- ANİMASYON MANTIĞI (YENİLENDİ) ---
    private void setupAnimationStates() {
        int state = getState();

        // Tüm animasyonları durdurmaya gerek yok, startIfStopped zaten kontrol eder
        // Ama temiz geçiş için bazen gerekebilir.

        if (state == 0 || state == 4) { // Pasif (Yatıyor)
            passiveState.startIfStopped(this.tickCount);
            electrocutedState.stop();
            standupState.stop();
            idleState.stop();
            walkState.stop();
        }
        else if (state == 1) { // Electrocuted
            electrocutedState.startIfStopped(this.tickCount);
            passiveState.stop();
            standupState.stop();
        }
        else if (state == 2) { // Standup (Kalkıyor)
            standupState.startIfStopped(this.tickCount);
            electrocutedState.stop();
            passiveState.stop();
        }
        else if (state == 3) { // Alive (Canlı)
            standupState.stop();

            // Hareket halindeyse Yürüme, değilse Idle
            if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6) {
                // İleride "Kolsuz Yürüme" için buraya 'if (hasNoArms) walkNoArms.start' ekleyebiliriz
                walkState.startIfStopped(this.tickCount);
                idleState.stop();
            } else {
                idleState.startIfStopped(this.tickCount);
                walkState.stop();
            }

            // Saldırı animasyonu (Swing animasyonuna bağlayabiliriz)
            if (this.swinging) {
                attackState.startIfStopped(this.tickCount);
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