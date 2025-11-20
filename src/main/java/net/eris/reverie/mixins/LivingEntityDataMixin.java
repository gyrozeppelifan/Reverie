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

    // Yeni Veri Kanalı: Sadece Boolean taşır (True/False)
    @Unique
    private static final EntityDataAccessor<Boolean> HAS_ANCIENT_CLOAK = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);

    // 1. Veriyi Kaydet
    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void reverie_defineSynchedData(CallbackInfo ci) {
        this.entityData.define(HAS_ANCIENT_CLOAK, false);
    }

    // 2. Her Tick'te Kontrol Et (SADECE SERVER)
    @Inject(method = "tick", at = @At("TAIL"))
    private void reverie_tick(CallbackInfo ci) {
        if (!this.level().isClientSide) {
            // Kendini LivingEntity olarak al
            LivingEntity self = (LivingEntity) (Object) this;

            // Efekt var mı?
            boolean hasEffect = self.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get());

            // Veri şu anki durumdan farklıysa güncelle (Paket gönderir)
            if (this.entityData.get(HAS_ANCIENT_CLOAK) != hasEffect) {
                this.entityData.set(HAS_ANCIENT_CLOAK, hasEffect);
            }
        }
    }

    // 3. Erişim Metotları
    @Override
    public boolean reverie$hasAncientCloak() {
        return this.entityData.get(HAS_ANCIENT_CLOAK);
    }

    @Override
    public void reverie$setAncientCloak(boolean hasCloak) {
        this.entityData.set(HAS_ANCIENT_CLOAK, hasCloak);
    }
}