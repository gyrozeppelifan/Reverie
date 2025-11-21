package net.eris.reverie.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
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

        // 1. ZEMİNİ BUL (Dikitler yerden çıkar)
        BlockPos.MutableBlockPos cursor = pos.mutable();
        boolean foundFloor = false;

        // Eğer havadaysak -> Aşağı inip zemini bul
        if (level.isEmptyBlock(cursor)) {
            for (int i = 0; i < 40; i++) {
                cursor.move(Direction.DOWN);
                if (level.getBlockState(cursor).isSolid()) {
                    foundFloor = true;
                    break;
                }
            }
        }
        // Eğer taşın içindeysek -> Yukarı çıkıp yüzeyi bul
        else {
            for (int i = 0; i < 20; i++) {
                cursor.move(Direction.UP);
                if (level.isEmptyBlock(cursor) && level.getBlockState(cursor.below()).isSolid()) {
                    cursor.move(Direction.DOWN); // Zemine geri bas
                    foundFloor = true;
                    break;
                }
            }
        }

        // Zemin uygun değilse (örn: su, lav veya havadaysa) iptal
        if (!foundFloor || !isValidRock(level.getBlockState(cursor))) {
            return false;
        }

        // 2. DİKİTİ OLUŞTUR (Yerden Yukarı)
        int length = 15 + random.nextInt(25); // 15-40 blok yüksekliğinde kuleler

        for (int i = 0; i < length; i++) {
            // Yukarı çıkıyoruz
            cursor.move(Direction.UP);

            // Tavana çarparsak dur
            if (!level.isEmptyBlock(cursor)) break;

            // İlerleme oranı (0.0 = Dip, 1.0 = Tepe)
            float progress = (float) i / length;

            // Kalınlık: Altta kalın, yukarı çıktıkça incelir
            int radius = (int) (4.5F * (1.0F - (progress * 0.7F)));

            // --- GÖVDEYİ ÇİZ ---
            fillCircle(level, cursor, radius, random, false, progress);

            // --- PLATFORM (YAPRAK KATMANI) ---
            // Zıplama alanları için genişleyen yapraklar
            if (i > 4 && i % (6 + random.nextInt(3)) == 0) {
                fillCircle(level, cursor, radius + 2, random, true, progress);
            }
        }

        // Tepesine biraz sarmaşık veya süs eklenebilir
        if (random.nextBoolean()) {
            level.setBlock(cursor, Blocks.AZALEA.defaultBlockState(), 3);
        }

        return true;
    }

    private void fillCircle(WorldGenLevel level, BlockPos center, int radius, RandomSource random, boolean isPlatform, float progress) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // Daire formülü (biraz gürültülü/doğal)
                if (x*x + z*z <= radius*radius + random.nextInt(2)) {
                    BlockPos target = center.offset(x, 0, z);

                    if (level.isEmptyBlock(target) || level.getBlockState(target).canBeReplaced()) {

                        BlockState blockToPlace;

                        if (isPlatform) {
                            // Platformlar her zaman Yaprak (Parkur için)
                            // Kenarları seyrek olsun
                            if (random.nextFloat() > 0.8F) continue;
                            blockToPlace = Blocks.AZALEA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
                        } else {
                            // --- MATERYAL GEÇİŞİ (GRADIENT) ---
                            // Dip (%0-20): Sert Taşlar (Deepslate, Tuff)
                            // Orta (%20-60): Yosunlu Taş (Mossy Cobblestone)
                            // Üst (%60-100): Toprak ve Yosun Bloğu

                            if (progress < 0.2F) {
                                blockToPlace = random.nextBoolean() ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.TUFF.defaultBlockState();
                            } else if (progress < 0.5F) {
                                blockToPlace = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                            } else if (progress < 0.8F) {
                                blockToPlace = random.nextBoolean() ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.DIRT.defaultBlockState();
                            } else {
                                // En tepeye çimen
                                blockToPlace = Blocks.GRASS_BLOCK.defaultBlockState();
                            }
                        }

                        level.setBlock(target, blockToPlace, 3);
                    }
                }
            }
        }
    }

    private boolean isValidRock(BlockState state) {
        // Üzerine dikit koyabileceğimiz sağlam bloklar
        return state.isSolidRender(null, null) || state.is(Blocks.DEEPSLATE) || state.is(Blocks.TUFF) || state.is(net.eris.reverie.init.ReverieModBlocks.GOLDEN_GRAVEL.get());
    }
}