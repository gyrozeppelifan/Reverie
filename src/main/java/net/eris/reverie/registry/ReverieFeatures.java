// src/main/java/net/eris/reverie/registry/ReverieFeatures.java
package net.eris.reverie.registry;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.eris.reverie.feature.OliveTreeFeature;

public class ReverieFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
        DeferredRegister.create(ForgeRegistries.FEATURES, "reverie");

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> OLIVE_TREE =
        FEATURES.register("olive_tree", 
            () -> new OliveTreeFeature(NoneFeatureConfiguration.CODEC)
        );
}
