package net.eris.reverie.feature;

import com.mojang.serialization.Codec;
import net.eris.reverie.block.CoinPileBlock;
import net.eris.reverie.init.ReverieModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SmartCoinPileFeature extends Feature<NoneFeatureConfiguration> {

    public SmartCoinPileFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();

        // 1. Zemin Kontrolü (Havaya veya suya koyma)
        // Altında katı bir blok olmalı (Golden Gravel veya Taş)
        BlockState groundState = level.getBlockState(pos.below());
        if (!groundState.isSolidRender(level, pos.below())) {
            return false;
        }

        // Yerine koyacağımız blok hava veya su olmalı (üzerine yazmayalım)
        // Ama Coin Pile (Snow Layer) suyun içinde oluşmaz, o yüzden sadece hava kontrolü yeterli.
        if (!level.getBlockState(pos).isAir()) {
            return false;
        }

        // 2. YÜKSEKLİK HESABI (LOGIC)
        int layers;

        // A) Duvar Kenarı Kontrolü ("Drift" Efekti)
        // Etrafındaki 4 bloğa bak, eğer katı bir duvar varsa oraya yığılma yap.
        boolean nextToWall = false;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockState neighbor = level.getBlockState(pos.relative(dir));
            if (neighbor.isSolidRender(level, pos.relative(dir)) && !neighbor.is(ReverieModBlocks.GOLDEN_GRAVEL.get())) {
                nextToWall = true;
                break;
            }
        }

        if (nextToWall) {
            // Duvar dibindeyse yüksek olsun (5 ile 8 arası)
            layers = 5 + random.nextInt(4); // 5, 6, 7, 8
        } else {
            // Açık alandaysa genelde alçak olsun
            if (random.nextFloat() < 0.1F) {
                // %10 şansla ortalık yerde de tepecik olsun (Hazine yığını)
                layers = 4 + random.nextInt(5); // 4-8
            } else {
                // Genelde serpinti (1-3)
                layers = 1 + random.nextInt(3);
            }
        }

        // 3. YERLEŞTİRME
        BlockState coinState = ReverieModBlocks.COIN_PILE.get().defaultBlockState()
                .setValue(CoinPileBlock.LAYERS, Math.min(8, layers));

        level.setBlock(pos, coinState, 3);

        return true;
    }
}