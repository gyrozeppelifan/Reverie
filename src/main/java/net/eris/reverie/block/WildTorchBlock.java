package net.eris.reverie.block;

// IMPORT DEĞİŞİKLİĞİ: Vanilla ParticleTypes yerine kendi kayıt dosyamızı alıyoruz.
import net.eris.reverie.init.ReverieModParticleTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WildTorchBlock extends DoublePlantBlock {

    protected static final VoxelShape SHAPE = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 16.0D, 10.0D);

    public WildTorchBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .noOcclusion()
                .strength(0.2F)
                .sound(SoundType.WOOD)
                .lightLevel(state -> 14)
        );
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        if (pState.getValue(HALF) == DoubleBlockHalf.LOWER) {
            BlockPos blockpos = pPos.below();
            return pLevel.getBlockState(blockpos).isFaceSturdy(pLevel, blockpos, Direction.UP);
        } else {
            return super.canSurvive(pState, pLevel, pPos);
        }
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.empty();
    }

    // --- PARTİKÜL METODU ---
    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        // Sadece üst yarıdan partikül çıksın
        if (pState.getValue(HALF) == DoubleBlockHalf.UPPER) {
            // Bloğun merkez koordinatları
            double x = (double)pPos.getX() + 0.5D;
            // Yükseklik: Bloğun üst kısmına yakın bir yerden çıksın (0.7 iyi bir nokta)
            double y = (double)pPos.getY() + 0.7D;
            double z = (double)pPos.getZ() + 0.5D;

            // Rastgelelik: Hep aynı noktadan çıkmasın, hafif sağa sola dağılsın.
            // (pRandom.nextDouble() - 0.5D) bize -0.5 ile +0.5 arası bir sayı verir.
            // Bunu 0.2 ile çarparak dağılma alanını daraltıyoruz.
            double randomOffset = 0.2D;
            double offsetX = (pRandom.nextDouble() - 0.5D) * randomOffset;
            double offsetZ = (pRandom.nextDouble() - 0.5D) * randomOffset;

            // --- BİZİM ÖZEL PARTİKÜLÜMÜZÜ EKLEME ---
            pLevel.addParticle(
                    ReverieModParticleTypes.WILD_FIRE.get(), // Hangi partikül?
                    x + offsetX, y, z + offsetZ,             // Nerede? (Hafif dağılmış pozisyon)
                    0.0D, 0.015D, 0.0D                       // Hız vektörü? (Çok hafif yukarı doğru süzülsün)
            );
        }
    }
}