package net.eris.reverie.entity.custom;

import com.mojang.serialization.Dynamic;
import net.eris.reverie.entity.ai.FolkLookAtTradingPlayerGoal;
import net.eris.reverie.entity.ai.FolkTradeWithPlayerGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.eris.reverie.entity.ai.FolkBrain;
import java.util.List;

public abstract class FolkEntity extends PathfinderMob implements Merchant {
    // --- SENKRONİZE VERİLER (Server & Client) ---
    private static final EntityDataAccessor<Integer> DATA_PROFESSION = SynchedEntityData.defineId(FolkEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(FolkEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WORKING_TICKS = SynchedEntityData.defineId(FolkEntity.class, EntityDataSerializers.INT);

    // --- DEĞİŞKENLER ---
    @Nullable private BlockPos workstationPos; // İş yerinin koordinatı
    @Nullable private Player tradingPlayer;    // O an ticaret yaptığı oyuncu
    protected MerchantOffers offers;           // Takas listesi

    // --- ANİMASYON DURUMLARI ---
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState panicAnimationState = new AnimationState();
    public final AnimationState workAnimationState = new AnimationState();

    protected FolkEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    // --- HİBRİT SİSTEM BÖLÜM 1: REFLEKSLER (GOALS) ---
    // Buraya anlık tepkileri koyuyoruz (Suya düşme, Oyuncuya bakma vb.)
    @Override
    protected void registerGoals() {
        // 0. Öncelik: Suda batmama (Hayatta kalma)
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // 1. Öncelik: Ticaret (Etkileşim)
        this.goalSelector.addGoal(1, new FolkTradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new FolkLookAtTradingPlayerGoal(this));

        // 2. Öncelik: Oyuncuya bakma (Doğallık)
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    // --- HİBRİT SİSTEM BÖLÜM 2: ZEKA (BRAIN) ---
    // Buraya uzun vadeli planları koyuyoruz (İşe gitme, Çalışma, Gezme)
    @Override
    protected Brain.Provider<FolkEntity> brainProvider() {
        return Brain.provider(
                // Hafıza Modülleri: İş yeri, Yürüme hedefi, Bakma hedefi vb.
                List.of(MemoryModuleType.JOB_SITE, MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH),
                // Sensörler: Etraftaki canlıları algılama
                List.of(net.minecraft.world.entity.ai.sensing.SensorType.NEAREST_LIVING_ENTITIES)
        );
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
        Brain<FolkEntity> brain = this.brainProvider().makeBrain(pDynamic);
        // Beyni SADECE BİR KERE başlatıyoruz
        FolkBrain.init(this, brain);
        return brain;
    }

    @Override
    public Brain<FolkEntity> getBrain() {
        return (Brain<FolkEntity>) super.getBrain();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("folkBrain");

        // Beyni ve aktiviteleri güncelle
        this.getBrain().tick((ServerLevel) this.level(), this);
        FolkBrain.updateActivity(this);

        this.level().getProfiler().pop();

        // --- DİNAMİK MESLEK KONTROLÜ ---
        if (this.level() instanceof ServerLevel serverLevel) {
            // Hafızada bir iş yeri var mı?
            if (this.getBrain().getMemory(MemoryModuleType.JOB_SITE).isPresent()) {
                // İş yerinin koordinatını al (GlobalPos'tan BlockPos'a)
                BlockPos jobPos = this.getBrain().getMemory(MemoryModuleType.JOB_SITE).get().pos();

                // Eğer henüz bir mesleğim yoksa veya iş yerim değiştiyse kontrol et
                if (this.getProfessionId() == 0) {
                    this.assignProfessionFromBlock(jobPos);
                }
            } else {
                // Hafızada iş yeri yoksa işsiz kal
                if (this.getProfessionId() != 0) {
                    this.setProfessionId(0);
                }
            }
        }

        super.customServerAiStep();
    }

    // İş yerini bırakma ve hafızayı temizleme metodu
    public void releaseWorkstation() {
        // Sadece sunucu tarafında çalışmalı
        if (this.level() instanceof ServerLevel serverLevel && this.workstationPos != null) {
            // POI Manager'a gidip "Bu koordinattaki rezervasyonumu iptal et" diyoruz.
            // Böylece başka bir Geck orayı kapabilir.
            serverLevel.getPoiManager().release(this.workstationPos);
        }

        // Kendi hafızamızdan da siliyoruz
        this.workstationPos = null;
        this.setProfessionId(0); // Artık resmen işsizsin!
    }

    // --- YENİ EKLENECEK METOD: BLOĞA GÖRE MESLEK SEÇİCİ ---
    private void assignProfessionFromBlock(BlockPos pos) {
        // Bloğun ne olduğuna bak
        net.minecraft.world.level.block.state.BlockState state = this.level().getBlockState(pos);

        // 1. BARMENLİK KONTROLÜ
        if (state.is(net.eris.reverie.init.ReverieModBlocks.SALOON_BAR.get())) {
            this.setProfessionId(1); // 1: Barmen
        }
        // 2. İLERİDE EKLENECEK ŞERİF KONTROLÜ (Örnek)
        // else if (state.is(ReverieBlocks.SHERIFF_DESK.get())) {
        //     this.setProfessionId(2);
        // }
        // 3. İLERİDE EKLENECEK DEMİRCİ KONTROLÜ (Örnek)
        // else if (state.is(ReverieBlocks.ANVIL_STATION.get())) {
        //     this.setProfessionId(3);
        // }

        // Eğer hiçbirine uymuyorsa (belki blok kırılmıştır), işi bırak
        else {
            this.releaseWorkstation();
        }
    }

    // --- ANİMASYON VE FİZİK MANTIĞI ---
    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            handleAnimationLogic(); // Client: Animasyon oynat
        } else {
            // Server: Çalışma sayacını düşür
            if (this.getWorkingTicks() > 0) {
                this.setWorkingTicks(this.getWorkingTicks() - 1);
            }
        }
    }

    private void handleAnimationLogic() {
        // 1. Panik (En yüksek öncelik)
        if (this.isPanicking()) {
            this.panicAnimationState.startIfStopped(this.tickCount);
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.workAnimationState.stop();
            return;
        } else {
            this.panicAnimationState.stop();
        }

        // 2. Çalışma (İş yerindeyken)
        if (this.getWorkingTicks() > 0) {
            this.workAnimationState.startIfStopped(this.tickCount);
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
        } else {
            this.workAnimationState.stop();

            // 3. Yürüme vs Idle
            if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D) {
                this.walkAnimationState.startIfStopped(this.tickCount);
                this.idleAnimationState.stop();
            } else {
                this.idleAnimationState.startIfStopped(this.tickCount);
                this.walkAnimationState.stop();
            }
        }
    }

    public boolean isPanicking() {
        return this.getLastHurtByMob() != null && (this.tickCount - this.getLastHurtByMobTimestamp() < 100);
    }

    // --- KAYIT SİSTEMİ (NBT) ---
    // Oyundan çıkıp girince verileri hatırla
    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("Profession", this.getProfessionId());
        pCompound.putInt("Variant", this.getVariant());
        if (this.workstationPos != null) {
            pCompound.put("WorkstationPos", NbtUtils.writeBlockPos(this.workstationPos));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.setProfessionId(pCompound.getInt("Profession"));
        this.setVariant(pCompound.getInt("Variant"));
        if (pCompound.contains("WorkstationPos")) {
            this.workstationPos = NbtUtils.readBlockPos(pCompound.getCompound("WorkstationPos"));
        }
    }

    // --- GETTERS & SETTERS ---
    // Verilere dışarıdan erişim
    public int getProfessionId() { return this.entityData.get(DATA_PROFESSION); }
    public void setProfessionId(int id) { this.entityData.set(DATA_PROFESSION, id); }

    public int getVariant() { return this.entityData.get(DATA_VARIANT); }
    public void setVariant(int id) { this.entityData.set(DATA_VARIANT, id); }

    public int getWorkingTicks() { return this.entityData.get(WORKING_TICKS); }
    public void setWorkingTicks(int ticks) { this.entityData.set(WORKING_TICKS, ticks); }

    @Nullable public BlockPos getWorkstationPos() { return workstationPos; }
    public void setWorkstationPos(@Nullable BlockPos pos) { this.workstationPos = pos; }

    // --- ENTITY CONFIG ---
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.5D); // Hız ayarı
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_PROFESSION, 0);
        this.entityData.define(DATA_VARIANT, 0);
        this.entityData.define(WORKING_TICKS, 0);
    }

    // --- MERCHANT (TİCARET) ARAYÜZÜ ---
    @Override public void setTradingPlayer(@Nullable Player pPlayer) { this.tradingPlayer = pPlayer; }
    @Override @Nullable public Player getTradingPlayer() { return this.tradingPlayer; }
    @Override public MerchantOffers getOffers() {
        if (this.offers == null) {
            this.offers = new MerchantOffers();
            // Buraya ileride tarifler eklenecek
        }
        return this.offers;
    }
    @Override public void overrideOffers(MerchantOffers pOffers) { this.offers = pOffers; }
    @Override public void notifyTrade(MerchantOffer pOffer) {
        pOffer.increaseUses();
        this.ambientSoundTime = -this.getAmbientSoundInterval();
        this.playSound(this.getNotifyTradeSound(), this.getSoundVolume(), this.getVoicePitch());
    }
    @Override public void notifyTradeUpdated(ItemStack pStack) {} // UI güncellemesi gerekirse
    @Override public int getVillagerXp() { return 0; } // XP sistemi şimdilik kapalı
    @Override public void overrideXp(int pXp) {}
    @Override public boolean showProgressBar() { return true; } // Ticaret barı görünsün
    @Override public SoundEvent getNotifyTradeSound() { return SoundEvents.VILLAGER_TRADE; } // Evet sesi
    @Override public boolean isClientSide() { return this.level().isClientSide(); }
}