package net.eris.reverie.worldgen;

import net.eris.reverie.init.ReverieModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.SurfaceRules;

public class ReverieSurfaceRules {
    private static final SurfaceRules.RuleSource GOLDEN_GRAVEL = makeStateRule(ReverieModBlocks.GOLDEN_GRAVEL.get());

    public static SurfaceRules.RuleSource makeRules() {
        // Zemin ve hemen altı komple Altın Çakıl olsun
        SurfaceRules.RuleSource goblinCaveFloor = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, GOLDEN_GRAVEL),
                SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, GOLDEN_GRAVEL)
        );

        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(net.minecraft.resources.ResourceKey.create(
                                net.minecraft.core.registries.Registries.BIOME,
                                new net.minecraft.resources.ResourceLocation("reverie", "goblin_cave"))
                        ),
                        goblinCaveFloor
                )
        );
    }

    private static SurfaceRules.RuleSource makeStateRule(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }
}