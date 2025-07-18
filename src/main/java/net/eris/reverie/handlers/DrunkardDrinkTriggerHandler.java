package net.eris.reverie.handlers;

import net.eris.reverie.entity.DrunkardEntity;
import net.eris.reverie.init.ReverieModMobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "reverie", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DrunkardDrinkTriggerHandler {

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent evt) {
        if (!(evt.getEntity() instanceof DrunkardEntity drunkard)
         || drunkard.level().isClientSide())
            return;

        // 1) Eğer Drunken Rage altındaysa içme tetiklenmez
        if (drunkard.hasEffect(ReverieModMobEffects.DRUNKEN_RAGE.get()))
            return;
        // 2) Eğer şişesi zaten kırıldıysa da içemez
        if (drunkard.getEntityData().get(DrunkardEntity.DATA_hasBrokenBottle))
            return;

        LivingEntity newTarget = evt.getNewTarget();
        if (newTarget != null
         && drunkard.getEntityData().get(DrunkardEntity.DATA_canDrink) == 1) {

            // İçmeye başla
            drunkard.getEntityData().set(DrunkardEntity.DATA_isDrinking, true);
            drunkard.getEntityData().set(DrunkardEntity.DATA_DrinkTime,   60);
            drunkard.getEntityData().set(DrunkardEntity.DATA_canDrink,    0);

            // “drunkard_drink” sesini çal (pitch=0.7F ile kalınlaştırdık)
            SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(
                new ResourceLocation("reverie", "drunkard_drink")
            );
            if (sound != null) {
                drunkard.level().playSound(
                    null,
                    drunkard.getX(), drunkard.getY(), drunkard.getZ(),
                    sound, SoundSource.HOSTILE,
                    3.0F, 1.0F
                );
            }
        }
    }
}
