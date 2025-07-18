// src/main/java/net/eris/reverie/handlers/DrunkardStageTickHandler.java

package net.eris.reverie.handlers;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.eris.reverie.entity.DrunkardEntity;
import net.eris.reverie.procedures.DrunkardStagesProcedure;

@Mod.EventBusSubscriber(modid = "reverie", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DrunkardStageTickHandler {
    @SubscribeEvent
    public static void onLivingTick(LivingTickEvent evt) {
        LivingEntity entity = evt.getEntity();
        // Sadece server-side ve sadece DrunkardEntity
        if (!entity.level().isClientSide() && entity instanceof DrunkardEntity drunkard) {
            DrunkardStagesProcedure.execute(drunkard.level(), drunkard);
        }
    }
}
