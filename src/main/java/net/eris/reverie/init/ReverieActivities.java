package net.eris.reverie.init;

import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ReverieActivities {
    public static final DeferredRegister<Activity> ACTIVITIES =
            DeferredRegister.create(ForgeRegistries.ACTIVITIES, "reverie");

    // İşte aradığımız o kayıp sembol!
    public static final RegistryObject<Activity> TRADE = ACTIVITIES.register("trade", () -> new Activity("trade"));

    // ... TRADE satırının altına ekle ...
    public static final RegistryObject<Activity> MEET = ACTIVITIES.register("meet", () -> new Activity("meet"));

    public static void register(IEventBus eventBus) {
        ACTIVITIES.register(eventBus);
    }
}