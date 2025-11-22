package net.eris.reverie.block;

import net.eris.reverie.init.ReverieModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
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

    // Vuruş Kutusu (Hitbox)
    protected static final VoxelShape SHAPE = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 16.0D, 10.0D);

    public WildTorchBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)       // DÜZELTİLDİ: Harita rengi Odun oldu.
                .noOcclusion()
                .strength(0.2F)
                .sound(SoundType.WOOD)         // DÜZELTİLDİ: Kemik sesi yerine ODUN sesi.
                .lightLevel(state -> 14)
        );
    }

    // --- ÖNEMLİ DÜZELTME: YERLEŞTİRME KURALI ---
    // DoublePlantBlock normalde sadece toprağa konur. Bunu değiştiriyoruz.
    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        // Eğer bu bloğun ALT yarısıysa:
        if (pState.getValue(HALF) == DoubleBlockHalf.LOWER) {
            BlockPos blockpos = pPos.below();
            // Altındaki bloğun üst yüzeyi sağlam (sturdy) mı? (Taş, odun, demir bloğu vs. olur)
            return pLevel.getBlockState(blockpos).isFaceSturdy(pLevel, blockpos, Direction.UP);
        } else {
            // Eğer ÜST yarıysa, normal bitki mantığı devam etsin (altında alt yarısı olmalı)
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

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        if (pState.getValue(HALF) == DoubleBlockHalf.UPPER) {
            double d0 = (double)pPos.getX() + 0.5D;
            double d1 = (double)pPos.getY() + 0.8D;
            double d2 = (double)pPos.getZ() + 0.5D;

            double offsetX = (pRandom.nextDouble() - 0.5D) * 0.2D;
            double offsetZ = (pRandom.nextDouble() - 0.5D) * 0.2D;

            pLevel.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
    }
}