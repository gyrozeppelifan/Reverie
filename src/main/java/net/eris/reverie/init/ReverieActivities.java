package net.eris.reverie.init;

import net.eris.reverie.ReverieMod;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ReverieActivities {
    public static final DeferredRegister<Activity> ACTIVITIES =
            DeferredRegister.create(ForgeRegistries.ACTIVITIES, "reverie");

    public static final RegistryObject<Activity> TRADE = ACTIVITIES.register("trade", () -> new Activity("trade"));

    // HATA VEREN KISIM BURASIYDI ("meet" -> "folk_meet" yaptık)
    public static final RegistryObject<Activity> MEET = ACTIVITIES.register("folk_meet", () -> new Activity("folk_meet"));

    public static void register(IEventBus eventBus) {
        ACTIVITIES.register(eventBus);
    }
}