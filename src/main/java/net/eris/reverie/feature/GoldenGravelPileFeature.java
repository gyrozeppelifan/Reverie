package net.eris.reverie.feature;

import com.mojang.serialization.Codec;
import net.eris.reverie.init.ReverieModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class GoldenGravelPileFeature extends Feature<NoneFeatureConfiguration> {

    public GoldenGravelPileFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();

        // 1. Zemin Kontrolü (Sadece zemine koyulabilir)
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.below()).isSolid()) {
            return false;
        }

        // 2. DUVAR KONTROLÜ ("Drift" Efekti)
        // Etrafındaki 4 bloğa bak. Eğer yanında katı bir blok (Duvar) varsa burayı yükselt.
        boolean isNextToWall = false;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);

            // Eğer yanımızdaki blok doluysa (ve bizim kendi bloğumuz değilse)
            if (neighborState.isSolidRender(level, neighborPos) && !neighborState.is(ReverieModBlocks.GOLDEN_GRAVEL.get())) {
                isNextToWall = true;
                break;
            }
        }

        int height = 0;

        if (isNextToWall) {
            // Duvar dibindeyse: %70 ihtimalle 1 blok, %30 ihtimalle 2 blok yükselt
            height = random.nextFloat() < 0.3F ? 2 : 1;
        } else {
            // Açık alandaysa: %5 ihtimalle rastgele tepecik oluştur (Hazine yığını)
            if (random.nextFloat() < 0.05F) {
                height = 1;
            }
        }

        // 3. YERLEŞTİRME
        // Belirlenen yükseklik kadar üst üste Golden Gravel koy
        for (int i = 0; i < height; i++) {
            BlockPos targetPos = pos.above(i);
            if (level.getBlockState(targetPos).isAir()) {
                level.setBlock(targetPos, ReverieModBlocks.GOLDEN_GRAVEL.get().defaultBlockState(), 3);
            }
        }

        return height > 0;
    }
}