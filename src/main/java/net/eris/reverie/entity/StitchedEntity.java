package net.eris.reverie.entity;

import net.eris.reverie.entity.stitched_abilities.AbilityLightning;
import net.eris.reverie.entity.stitched_abilities.AbilityRoar;
import net.eris.reverie.entity.stitched_abilities.StitchedAbility;
import net.eris.reverie.gui.StitchedMenu;
import net.eris.reverie.init.ReverieModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StitchedEntity extends TamableAnimal implements Enemy, MenuProvider {

    // States
    private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(StitchedEntity.class, EntityDataSerializers.INT);

    // Parçalar
    private static final EntityDataAccessor<ItemStack> DATA_HEAD_ITEM = SynchedEntityData.defineId(StitchedEntity.class, EntityDataSerializers.ITEM_STACK);

    // --- ENVANTER SİSTEMİ ---
    // 4 Slot: 0=Kafa, 1=Gövde, 2=Kol1, 3=Kol2
    public final ItemStackHandler inventory = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot == 0) {
                StitchedEntity.this.setHeadItem(this.getStackInSlot(0));
            }
        }
    };
    private final LazyOptional<ItemStackHandler> inventoryOptional = LazyOptional.of(() -> inventory);

    // Animasyonlar
    public final AnimationState idleState = new AnimationState();
    public final AnimationState passiveState = new AnimationState();
    public final AnimationState electrocutedState = new AnimationState();
    public final AnimationState standupState = new AnimationState();
    public final AnimationState walkState = new AnimationState();
    public final AnimationState walkNoArmsState = new AnimationState();
    public final AnimationState attackState = new AnimationState();
    public final AnimationState roaringState = new AnimationState();
    public final AnimationState sitRoaringState = new AnimationState();

    private int animationTimer = 0;
    private int attackAnimationTick = 0;
    private static final int ATTACK_DURATION = 35;
    private int roarAnimationTick = 0;
    private static final int ROAR_DURATION = 80;

    private StitchedAbility currentAbility = null;
    public int abilityTick = 0;
    private int abilityCooldown = 0;

    public StitchedEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.refreshDimensions();
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.DAMAGE_FIRE, -1.0F);
    }

    public StitchedEntity(Level level) {
        this(ReverieModEntities.STITCHED.get(), level);
    }

    public StitchedEntity(PlayMessages.SpawnEntity packet, Level level) {
        this(ReverieModEntities.STITCHED.get(), level);
    }

    // --- FORGE CAPABILITY ---
    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return inventoryOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inventoryOptional.invalidate();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STATE, 0);
        this.entityData.define(DATA_HEAD_ITEM, ItemStack.EMPTY);
    }

    public ItemStack getHeadItem() { return this.entityData.get(DATA_HEAD_ITEM); }
    public void setHeadItem(ItemStack stack) { this.entityData.set(DATA_HEAD_ITEM, stack); }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("State", this.getState());
        tag.put("Inventory", this.inventory.serializeNBT());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setState(tag.getInt("State"));
        if (tag.contains("Inventory")) {
            this.inventory.deserializeNBT(tag.getCompound("Inventory"));
            this.setHeadItem(this.inventory.getStackInSlot(0));
        }
        if (this.getState() == 3) {
            this.reassessTameGoals();
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (this.getState() == 3) {
            if (!this.isTame() && itemstack.is(Items.DIAMOND)) {
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                if (!this.level().isClientSide) {
                    this.tame(player);
                    this.navigation.stop();
                    this.setTarget(null);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                    player.sendSystemMessage(Component.literal("§aStitched artık dostun!"));
                    this.reassessTameGoals();
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (this.isTame() && this.isOwnedBy(player) && player.isShiftKeyDown()) {
                if (!this.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                    NetworkHooks.openScreen(serverPlayer, this, buf -> buf.writeInt(this.getId()));
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
        return super.mobInteract(player, hand);
    }

    // --- MENU PROVIDER ---
    // DİKKAT: getDisplayName metodunu SİLDİM.
    // Artık Entity sınıfının varsayılan metodunu kullanacak ve özel ismi varsa onu gösterecek.

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new StitchedMenu(containerId, playerInventory, this);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.setPersistenceRequired();
    }

    public void reassessTameGoals() {
        this.goalSelector.removeAllGoals(g -> true);
        this.targetSelector.removeAllGoals(g -> true);
        this.registerGoals();
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
        if (this.getState() != 3) return EntityDimensions.fixed(1.8f, 1.0f);
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
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        if (this.getState() < 3) return 0.5f;
        return super.getStandingEyeHeight(pose, dimensions);
    }

    @Override
    protected void registerGoals() {
        if (getState() != 3) return;

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));

        if (this.isTame()) {
            this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.2D, 10.0F, 2.0F, false));
            this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, false));
            this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
            this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
            this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

            this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
            this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
            this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        } else {
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
            this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
            this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
            this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

            this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7D);
    }

    public int getState() { return this.entityData.get(DATA_STATE); }
    public void setState(int state) { this.entityData.set(DATA_STATE, state); }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt lightning) {
        if (getState() == 0) {
            this.setState(1);
            this.animationTimer = 60;
            this.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 2.0F, 1.0F);
        } else {
            super.thunderHit(level, lightning);
        }
    }

    @Override
    public boolean isInvulnerable() {
        return this.getState() < 3 || super.isInvulnerable();
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
        if (this.attackAnimationTick > 0) this.attackAnimationTick--;
        if (this.roarAnimationTick > 0) this.roarAnimationTick--;

        if (this.level().isClientSide) {
            if (this.attackAnimationTick <= 0 && this.attackState.isStarted()) {
                this.attackState.stop();
            }
            if (this.roarAnimationTick <= 0 && this.roaringState.isStarted()) {
                this.roaringState.stop();
            }
        }

        if (abilityCooldown > 0) abilityCooldown--;
        if (this.isOnFire()) this.clearFire();

        if (getState() < 3) {
            this.setTarget(null);
            this.getNavigation().stop();
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
            this.yBodyRot = this.yRotO;
            this.yHeadRot = this.yRotO;
        }

        if (!this.level().isClientSide) {
            if (currentAbility != null) {
                this.getNavigation().stop();
                this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
                this.yBodyRot = this.yRotO;
                this.yHeadRot = this.yRotO;

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
            setupAnimationStates();
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean result = super.doHurtTarget(entity);
        if (result) {
            this.level().broadcastEntityEvent(this, (byte) 4);
        }
        return result;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 4) {
            this.attackAnimationTick = ATTACK_DURATION;
            this.attackState.start(this.tickCount);
        } else if (id == 8) {
            this.roarAnimationTick = ROAR_DURATION;
            this.roaringState.start(this.tickCount);
            this.attackState.stop();
            this.walkState.stop();
            this.idleState.stop();
        } else {
            super.handleEntityEvent(id);
        }
    }

    public void triggerAbility() {
        if (currentAbility != null || abilityCooldown > 0) return;
        ItemStack headItem = this.inventory.getStackInSlot(0);
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

            if (abilityToUse instanceof AbilityRoar) {
                this.level().broadcastEntityEvent(this, (byte) 8);
                this.roarAnimationTick = ROAR_DURATION;
            }
            this.abilityCooldown = abilityToUse.getDuration() + 100;
        }
    }

    private void handleStateLogic() {
        int currentState = getState();
        if (currentState == 1) {
            if (--animationTimer <= 0) { setState(2); animationTimer = 70; }
        } else if (currentState == 2) {
            if (--animationTimer <= 0) { setState(5); animationTimer = 40; }
        } else if (currentState == 5) {
            if (--animationTimer <= 0) {
                setState(3);
                this.reassessTameGoals();
            }
        }
    }

    private void setupAnimationStates() {
        int state = getState();
        if (state != 0 && state != 4) passiveState.stop();
        if (state != 1) electrocutedState.stop();
        if (state != 2) standupState.stop();
        if (state != 5) sitRoaringState.stop();

        if (state == 0 || state == 4) {
            passiveState.startIfStopped(this.tickCount);
        }
        else if (state == 1) {
            electrocutedState.startIfStopped(this.tickCount);
        }
        else if (state == 2) {
            standupState.startIfStopped(this.tickCount);
        }
        else if (state == 5) {
            sitRoaringState.startIfStopped(this.tickCount);
        }
        else if (state == 3) {
            if (this.roarAnimationTick > 0) {
                if (!roaringState.isStarted()) roaringState.start(this.tickCount);
                walkState.stop();
                walkNoArmsState.stop();
                idleState.stop();
                attackState.stop();
                return;
            } else {
                roaringState.stop();
            }

            if (this.attackAnimationTick > 0) {
                walkState.stop();
                walkNoArmsState.stop();
                idleState.stop();
            }
            else {
                boolean isMoving = this.getDeltaMovement().horizontalDistanceSqr() > 0.005;
                if (isMoving) {
                    walkState.startIfStopped(this.tickCount);
                    idleState.stop();
                } else {
                    idleState.startIfStopped(this.tickCount);
                    walkState.stop();
                }
            }
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mob) {
        return null;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        if (getState() == 3) {
            this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 1.0F);
        }
    }
}