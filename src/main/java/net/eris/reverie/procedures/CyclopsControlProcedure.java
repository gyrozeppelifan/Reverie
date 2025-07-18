package net.eris.reverie.procedures;

import net.eris.reverie.entity.DrunkardEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CyclopsControlProcedure {
    @SubscribeEvent
    public static void onEntityTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() == null || event.getEntity().level().isClientSide()) return;
        execute(event.getEntity().level(), event.getEntity());
    }

    public static void execute(LevelAccessor world, Entity entity) {
        // Sadece DrunkardEntity için çalışsın
        if (!(entity instanceof DrunkardEntity)) return;
        DrunkardEntity drunkard = (DrunkardEntity) entity;

        // İsim etiketi
        String name = entity.getDisplayName().getString();
        boolean isCyclops = "Cyclops".equals(name);

        // DATA_Cyclops alanını güncelle
        drunkard.getEntityData().set(DrunkardEntity.DATA_Cyclops, isCyclops);
    }
}
