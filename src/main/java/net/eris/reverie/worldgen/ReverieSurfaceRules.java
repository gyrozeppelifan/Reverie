package net.eris.reverie.worldgen;

import net.eris.reverie.init.ReverieModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.SurfaceRules;

public class ReverieSurfaceRules {

    // Bloklarımızı kural parçası haline getiriyoruz
    private static final SurfaceRules.RuleSource GOLDEN_GRAVEL = makeStateRule(ReverieModBlocks.GOLDEN_GRAVEL.get());

    public static SurfaceRules.RuleSource makeRules() {

        // KURAL: Eğer zemindeysek (ON_FLOOR), Golden Gravel koy.
        SurfaceRules.RuleSource goblinCaveFloor = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, GOLDEN_GRAVEL)
        );

        return SurfaceRules.sequence(
                // Şart: Eğer "reverie:goblin_cave" biyomundaysak yukarıdaki kuralı uygula
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