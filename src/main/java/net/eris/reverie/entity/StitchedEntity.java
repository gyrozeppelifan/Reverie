package net.eris.reverie.entity;

import net.eris.reverie.client.model.animations.StitchedAnimation;
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

    // --- YENİ: MODÜLER PARÇA SLOTLARI (Senkronize Veri) ---
    // Bu değişkenler Stitched'in üzerinde ne takılı olduğunu tutar
    private static final EntityDataAccessor<ItemStack> DATA_HEAD_ITEM = SynchedEntityData.defineId(StitchedEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> DATA_BODY_ITEM = SynchedEntityData.defineId(StitchedEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> DATA_ARM_ITEM = SynchedEntityData.defineId(StitchedEntity.class, EntityDataSerializers.ITEM_STACK);

    public final AnimationState passiveState = new AnimationState();
    public final AnimationState electrocutedState = new AnimationState();
    public final AnimationState standupState = new AnimationState();
    public final AnimationState walkState = new AnimationState();

    private int animationTimer = 0;

    // --- YENİ: YETENEK SİSTEMİ DEĞİŞKENLERİ ---
    private StitchedAbility currentAbility = null;
    public int abilityTick = 0; // Helperlar okusun diye public
    private int abilityCooldown = 0; // Spam engellemek için

    public StitchedEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        // Mob doğduğu an boyutunu hesaplasın (Hitbox fix)
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
        // Yeni slotları tanımla (Boş item ile başlar)
        this.entityData.define(DATA_HEAD_ITEM, ItemStack.EMPTY);
        this.entityData.define(DATA_BODY_ITEM, ItemStack.EMPTY);
        this.entityData.define(DATA_ARM_ITEM, ItemStack.EMPTY);
    }

    // --- YENİ: PARÇA YÖNETİMİ (GETTER/SETTER) ---
    public ItemStack getHeadItem() { return this.entityData.get(DATA_HEAD_ITEM); }
    public void setHeadItem(ItemStack stack) { this.entityData.set(DATA_HEAD_ITEM, stack); }

    public ItemStack getBodyItem() { return this.entityData.get(DATA_BODY_ITEM); }
    public void setBodyItem(ItemStack stack) { this.entityData.set(DATA_BODY_ITEM, stack); }

    public ItemStack getArmItem() { return this.entityData.get(DATA_ARM_ITEM); }
    public void setArmItem(ItemStack stack) { this.entityData.set(DATA_ARM_ITEM, stack); }

    // --- YENİ: KAYIT SİSTEMİ (NBT) ---
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

    // --- YENİ: ETKİLEŞİM (GUI TESTİ İÇİN) ---
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        // Shift+Sağ tık ile envanter açma (İleride GUI kodunu buraya bağlayacağız)
        if (player.isShiftKeyDown() && this.getState() == 3) {
            if (!this.level().isClientSide) {
                // Geçici test kodu: Elindeki itemi kafasına takar (Test etmek istersen)
                ItemStack handItem = player.getItemInHand(hand);
                if (!handItem.isEmpty()) {
                    this.spawnAtLocation(this.getHeadItem()); // Eskiyi düşür
                    this.setHeadItem(handItem.split(1)); // Yeniyi tak
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Kafa Modu Değişti!"));
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    // State değiştiğinde hitbox'ı güncelle
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_STATE.equals(key)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    // --- DİNAMİK HITBOX AYARI ---
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        int state = this.getState();
        // Yatıyor (0), Çarpılıyor (1) veya Bekliyor (4) ise yatay hitbox
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
            this.animationTimer = 60;
            this.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 2.0F, 1.0F);
        } else {
            super.thunderHit(level, lightning);
        }
    }

    // --- GOD MODE & HASAR ENGELLEME ---
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

    // --- İTİLMEZLİK ---
    @Override
    public boolean isPushable() {
        return getState() == 3 && super.isPushable();
    }

    @Override
    public void push(Entity entity) {
        if (getState() == 3) {
            super.push(entity);
        }
    }

    @Override
    protected void doPush(Entity entity) {
        if (getState() == 3) {
            super.doPush(entity);
        }
    }

    @Override
    public void tick() {
        super.tick();

        // Cooldown sayacı
        if (abilityCooldown > 0) abilityCooldown--;

        if (this.isOnFire()) {
            this.clearFire();
        }

        // --- YENİ: YETENEK SİSTEMİ MANTIĞI (Sadece Server) ---
        if (!this.level().isClientSide) {
            if (currentAbility != null) {
                abilityTick++;
                // Helper Class'ın tick metodunu çalıştır
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

    // --- YENİ: KUMANDA (ITEM) TETİKLEYİCİSİ ---
    public void triggerAbility() {
        // Eğer zaten bir yetenek kullanıyorsa veya cooldown varsa yapma
        if (currentAbility != null || abilityCooldown > 0) return;

        // 1. Kafadaki itemi kontrol et
        ItemStack headItem = this.getHeadItem();
        StitchedAbility abilityToUse = null;

        // 2. İteme göre yeteneği seç (Strategy Pattern)
        if (headItem.is(Items.LIGHTNING_ROD)) {
            abilityToUse = new AbilityLightning();
        }
        // else if (headItem.is(Items.SPYGLASS)) { abilityToUse = new AbilityLaser(); }
        else {
            // Hiçbir şey yoksa veya tanımlı olmayan bir şeyse -> BAZ YETENEK
            abilityToUse = new AbilityRoar();
        }

        // 3. Yeteneği Başlat
        if (abilityToUse != null && abilityToUse.canUse(this)) {
            this.currentAbility = abilityToUse;
            this.abilityTick = 0;
            this.currentAbility.start(this);

            // Yetenek bitince cooldown koy (Süre + 5 saniye)
            this.abilityCooldown = abilityToUse.getDuration() + 100;
        }
    }

    private void handleStateLogic() {
        int currentState = getState();

        if (currentState == 1) { // AŞAMA 1: ELECTROCUTED
            if (--animationTimer <= 0) {
                setState(4); // WAITING
                animationTimer = 20;
            }
        }
        else if (currentState == 4) { // AŞAMA 2: BEKLEME
            if (--animationTimer <= 0) {
                setState(2); // STANDUP
                animationTimer = 70;
            }
        }
        else if (currentState == 2) { // AŞAMA 3: STANDUP
            if (--animationTimer <= 0) {
                setState(3); // ALIVE
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

        if (state == 0 || state == 4) {
            passiveState.startIfStopped(this.tickCount);
            electrocutedState.stop();
            standupState.stop();
            walkState.stop();
        }
        else if (state == 1) {
            passiveState.stop();
            electrocutedState.startIfStopped(this.tickCount);
            standupState.stop();
        }
        else if (state == 2) {
            passiveState.stop();
            electrocutedState.stop();
            standupState.startIfStopped(this.tickCount);
        }
        else if (state == 3) {
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