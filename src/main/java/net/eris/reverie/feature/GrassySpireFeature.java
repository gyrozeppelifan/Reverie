package net.eris.reverie.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class GrassySpireFeature extends Feature<NoneFeatureConfiguration> {

    public GrassySpireFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();

        // 1. TAVANI BUL
        if (!level.isEmptyBlock(pos)) {
            return false;
        }

        BlockPos.MutableBlockPos cursor = pos.mutable();
        // Yukarı çık, tavanı bul
        while (cursor.getY() < level.getMaxBuildHeight() && level.isEmptyBlock(cursor)) {
            cursor.move(Direction.UP);
        }

        // DÜZELTME: isStone yerine isValidRock kullanıyoruz
        if (level.isEmptyBlock(cursor) || !isValidRock(level.getBlockState(cursor))) {
            return false;
        }

        // 2. DİKİTİ OLUŞTUR
        int length = 10 + random.nextInt(15);

        for (int i = 0; i < length; i++) {
            cursor.move(Direction.DOWN);

            if (!level.isEmptyBlock(cursor)) break;

            float progress = (float) i / length;
            int radius = (int) (3.0F * (1.0F - progress));

            fillCircle(level, cursor, radius, random);
        }

        return true;
    }

    private void fillCircle(WorldGenLevel level, BlockPos center, int radius, RandomSource random) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x*x + z*z <= radius*radius + 1) {
                    BlockPos target = center.offset(x, 0, z);
                    if (level.isEmptyBlock(target)) {

                        BlockState blockToPlace = Blocks.DIRT.defaultBlockState();

                        if (x*x + z*z >= (radius-1)*(radius-1)) {
                            blockToPlace = random.nextBoolean() ? Blocks.GRASS_BLOCK.defaultBlockState() : Blocks.MOSS_BLOCK.defaultBlockState();

                            if (random.nextFloat() < 0.3F) {
                                placeVine(level, target.below());
                            }
                        }

                        level.setBlock(target, blockToPlace, 3);
                    }
                }
            }
        }
    }

    private void placeVine(WorldGenLevel level, BlockPos pos) {
        if (level.isEmptyBlock(pos)) {
            level.setBlock(pos, Blocks.VINE.defaultBlockState(), 3);
        }
    }

    // DÜZELTME: Metot ismi değiştirildi
    private boolean isValidRock(BlockState state) {
        return state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE) || state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK);
    }
}