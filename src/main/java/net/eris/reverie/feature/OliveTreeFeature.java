package net.eris.reverie.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class OliveTreeFeature extends Feature<NoneFeatureConfiguration> {
    private final BlockState oliveLog;
    private final BlockState oliveLeaves;
    private final Block oliveBranchesBlock;

    public OliveTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
        this.oliveLog = Block.byItem(net.eris.reverie.init.ReverieModBlocks.OLIVE_LOG.get().asItem()).defaultBlockState();
        this.oliveLeaves = Block.byItem(net.eris.reverie.init.ReverieModBlocks.OLIVE_LEAVES.get().asItem()).defaultBlockState();
        this.oliveBranchesBlock = net.eris.reverie.init.ReverieModBlocks.OLIVE_BRANCHES.get();
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel world = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();

        // Gövde boyu 4-6 blok arası
        int trunkHeight = 4 + random.nextInt(3);
        for (int i = 0; i < trunkHeight; i++) {
            setBlock(world, pos.above(i), oliveLog);
        }

        BlockPos top = pos.above(trunkHeight - 1);

        // + şeklinde dallar
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos branchBase = top.relative(dir);
            setBlock(world, branchBase, oliveLog);

            // Dallar bir blok daha yayılsın (2 blokluk)
            BlockPos outerBranch = branchBase.relative(dir);
            if (world.getBlockState(outerBranch).isAir()) {
                setBlock(world, outerBranch, oliveLog);
            }
        }

        // Ana leaf kümesi (blob ve alt taban iyileştirilmiş)
        int leafRadius = 3;
        for (int dx = -leafRadius; dx <= leafRadius; dx++) {
            for (int dz = -leafRadius; dz <= leafRadius; dz++) {
                for (int dy = 2; dy >= -2; dy--) {
                    BlockPos leafPos = top.offset(dx, dy, dz);
                    double dist = Math.sqrt(dx * dx + dz * dz + Math.abs(dy) * 0.8);
                    double extraBlob = 0;
                    if (dy == 2) extraBlob = 1.2 + random.nextFloat();

                    if (dist <= leafRadius + extraBlob + random.nextFloat() * 0.7f) {
                        if (world.getBlockState(leafPos).isAir()) {
                            // ALT TABANDA MERKEZİ AÇIK BIRAKMA KURALI
                            if (dy <= -1) {
                                double edgeDist = Math.max(Math.abs(dx), Math.abs(dz));
                                float chance = (float) (0.35 + 0.30 * (edgeDist / leafRadius));
                                if (random.nextFloat() > chance) continue;
                            }

                            setBlock(world, leafPos, oliveLeaves);

                            // Altı boşsa belli şansla olive_branches
                            BlockPos below = leafPos.below();
                            if (world.getBlockState(below).isAir() && random.nextFloat() < 0.18f) {
                                int age = random.nextInt(3);
                                BlockState branchState = oliveBranchesBlock.defaultBlockState().setValue(BlockStateProperties.AGE_2, age);
                                setBlock(world, below, branchState);
                            }

                            // Kenar leaflerden aşağıya sarkıntı
                            if ((Math.abs(dx) == leafRadius || Math.abs(dz) == leafRadius) && random.nextFloat() < 0.45f) {
                                BlockPos hangPos = leafPos.below();
                                if (world.getBlockState(hangPos).isAir())
                                    setBlock(world, hangPos, oliveLeaves);
                            }
                        }
                    }
                }
            }
        }

        // En tepeye ve yakın çevreye 2-3 random blobTop ekle
        int blobCount = 2 + random.nextInt(2);
        for (int b = 0; b < blobCount; b++) {
            int dx = random.nextInt(3) - 1; // -1, 0, 1
            int dz = random.nextInt(3) - 1;
            BlockPos blobCenter = top.above(2 + random.nextInt(2)).offset(dx, 0, dz);

            // Mini bir blob döngüsü (radius 1-2)
            int r = 1 + random.nextInt(2);
            for (int bdx = -r; bdx <= r; bdx++) {
                for (int bdz = -r; bdz <= r; bdz++) {
                    BlockPos bp = blobCenter.offset(bdx, 0, bdz);
                    double blobDist = Math.sqrt(bdx * bdx + bdz * bdz);
                    if (blobDist <= r + 0.3f * random.nextFloat()) {
                        if (world.getBlockState(bp).isAir()) {
                            setBlock(world, bp, oliveLeaves);
                            // Blobun kenarından aşağı sarkıt
                            if ((Math.abs(bdx) == r || Math.abs(bdz) == r) && random.nextFloat() < 0.7f) {
                                BlockPos hangPos = bp.below();
                                if (world.getBlockState(hangPos).isAir())
                                    setBlock(world, hangPos, oliveLeaves);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
