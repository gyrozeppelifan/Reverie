package net.eris.reverie.mixins;

import net.eris.reverie.init.ReverieModMobEffects;
import net.eris.reverie.util.IAncientCloakData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDataMixin extends Entity implements IAncientCloakData {

    public LivingEntityDataMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    // Ancient Cloak Verisi
    @Unique
    private static final EntityDataAccessor<Boolean> HAS_ANCIENT_CLOAK = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);

    // YENİ: Drunken Rage Verisi
    @Unique
    private static final EntityDataAccessor<Boolean> HAS_DRUNKEN_RAGE = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void reverie_defineSynchedData(CallbackInfo ci) {
        this.entityData.define(HAS_ANCIENT_CLOAK, false);
        this.entityData.define(HAS_DRUNKEN_RAGE, false); // Yeni veriyi tanımla
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void reverie_tick(CallbackInfo ci) {
        if (!this.level().isClientSide) {
            LivingEntity self = (LivingEntity) (Object) this;

            // 1. Ancient Cloak Kontrolü
            boolean hasCloak = self.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get());
            if (this.entityData.get(HAS_ANCIENT_CLOAK) != hasCloak) {
                this.entityData.set(HAS_ANCIENT_CLOAK, hasCloak);
            }

            // 2. Drunken Rage Kontrolü (YENİ)
            boolean hasRage = self.hasEffect(ReverieModMobEffects.DRUNKEN_RAGE.get());
            if (this.entityData.get(HAS_DRUNKEN_RAGE) != hasRage) {
                this.entityData.set(HAS_DRUNKEN_RAGE, hasRage);
            }
        }
    }

    // Erişim Metotları
    @Override
    public boolean reverie$hasAncientCloak() {
        return this.entityData.get(HAS_ANCIENT_CLOAK);
    }
    @Override
    public void reverie$setAncientCloak(boolean hasCloak) {
        this.entityData.set(HAS_ANCIENT_CLOAK, hasCloak);
    }

    // Yeni Erişim Metotları
    @Override
    public boolean reverie$hasDrunkenRage() {
        return this.entityData.get(HAS_DRUNKEN_RAGE);
    }
    @Override
    public void reverie$setDrunkenRage(boolean hasRage) {
        this.entityData.set(HAS_DRUNKEN_RAGE, hasRage);
    }
}