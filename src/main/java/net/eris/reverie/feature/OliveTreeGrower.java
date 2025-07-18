package net.eris.reverie.feature;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class OliveTreeGrower extends AbstractTreeGrower {
    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean largeHive) {
        // JSON ile register ettiğimiz "reverie:olive_tree" configured feature'ının ResourceKey'i
        return ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            new ResourceLocation("reverie", "olive_tree")
        );
    }
}
