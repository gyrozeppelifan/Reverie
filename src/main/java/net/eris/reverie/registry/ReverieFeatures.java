package net.eris.reverie.registry;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.feature.GrassySpireFeature;
import net.eris.reverie.feature.OliveTreeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ReverieFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, ReverieMod.MODID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> GRASSY_SPIRE =
            FEATURES.register("grassy_spire", () -> new GrassySpireFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> OLIVE_TREE =
            FEATURES.register("olive_tree",
                    () -> new OliveTreeFeature(NoneFeatureConfiguration.CODEC)
            );

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}