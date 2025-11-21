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

        // 1. TAVAN ARAMA LOGİĞİ (AKILLI ARAMA)
        // Rastgele seçilen nokta (pos) havada da olabilir, taşın içinde de.
        BlockPos.MutableBlockPos cursor = pos.mutable();
        boolean foundCeiling = false;

        // Eğer taşın içindeysek -> Aşağı inip boşluğu bulalım
        if (level.getBlockState(cursor).isSolid()) {
            for (int i = 0; i < 20; i++) { // En fazla 20 blok aşağı bak
                cursor.move(Direction.DOWN);
                if (level.isEmptyBlock(cursor)) {
                    // Boşluğu bulduk, şimdi tekrar 1 yukarı çıkıp tavana yapışalım
                    cursor.move(Direction.UP);
                    foundCeiling = true;
                    break;
                }
            }
        }
        // Eğer havadaysak -> Yukarı çıkıp tavanı bulalım
        else {
            for (int i = 0; i < 40; i++) { // En fazla 40 blok yukarı bak
                cursor.move(Direction.UP);
                if (level.getBlockState(cursor).isSolid()) {
                    // Tavanı bulduk (cursor şu an taşın içinde), 1 aşağı inelim (havaya)
                    cursor.move(Direction.DOWN);
                    foundCeiling = true;
                    break;
                }
            }
        }

        // Tavan bulunamadıysa veya uygun taş değilse iptal
        if (!foundCeiling || !isValidRock(level.getBlockState(cursor.above()))) {
            return false;
        }

        // 2. DİKİT OLUŞTURMA (PLATFORMLU)
        // Başlangıç noktası: cursor (Tavanın altındaki hava bloğu)

        int length = 15 + random.nextInt(20); // Çok daha uzun (15-35 blok)

        for (int i = 0; i < length; i++) {
            // Yere çarparsak dur
            if (!level.isEmptyBlock(cursor) && !level.getBlockState(cursor).getFluidState().isEmpty()) break;

            // Kalınlık: Üstte kalın, aşağı indikçe incelir
            float progress = (float) i / length;
            int radius = (int) (3.5F * (1.0F - (progress * 0.8F))); // Ucu sivri, kökü kalın

            // --- GÖVDEYİ ÇİZ ---
            fillCircle(level, cursor, radius, random, false);

            // --- PLATFORM (YAPRAK KATMANI) ---
            // Her 5-7 blokta bir, etrafa genişleyen yaprak platformları koy
            if (i > 3 && i % (5 + random.nextInt(2)) == 0) {
                // Yarıçapı +2 blok genişletip sadece yaprak çiziyoruz
                fillCircle(level, cursor, radius + 2, random, true);
            }

            // Bir adım aşağı in
            cursor.move(Direction.DOWN);
        }

        // En uca sarmaşık sarkıt
        placeVine(level, cursor);

        return true;
    }

    private void fillCircle(WorldGenLevel level, BlockPos center, int radius, RandomSource random, boolean isPlatform) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // Daire formülü (biraz rastgelelik ekle)
                if (x*x + z*z <= radius*radius + random.nextInt(2)) {
                    BlockPos target = center.offset(x, 0, z);

                    // Sadece hava varsa koy
                    if (level.isEmptyBlock(target)) {

                        BlockState blockToPlace;

                        if (isPlatform) {
                            // Platformsa sadece Yaprak (Olive Leaves veya Oak Leaves)
                            // Kenarları ince olsun (Sadece %70 ihtimalle koy)
                            if (random.nextFloat() > 0.7F) continue;
                            blockToPlace = Blocks.OAK_LEAVES.defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true);
                        } else {
                            // Gövdeyse Toprak/Çimen
                            // Dış kabuk Çimen veya Yosun
                            if (x*x + z*z >= (radius-1)*(radius-1)) {
                                blockToPlace = random.nextBoolean() ? Blocks.GRASS_BLOCK.defaultBlockState() : Blocks.MOSS_BLOCK.defaultBlockState();
                            } else {
                                blockToPlace = Blocks.DIRT.defaultBlockState();
                            }
                        }

                        level.setBlock(target, blockToPlace, 3);
                    }
                }
            }
        }
    }

    private void placeVine(WorldGenLevel level, BlockPos pos) {
        for (int i = 0; i < 5; i++) { // 5 blok aşağı sarkıt
            if (level.isEmptyBlock(pos)) {
                level.setBlock(pos, Blocks.VINE.defaultBlockState(), 3);
                pos = pos.below();
            }
        }
    }

    private boolean isValidRock(BlockState state) {
        return state.isSolidRender(null, null); // Basitçe "Katı blok mu?" diye bakıyoruz
    }
}