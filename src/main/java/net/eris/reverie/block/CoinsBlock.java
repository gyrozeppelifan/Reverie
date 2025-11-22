package net.eris.reverie.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CoinsBlock extends Block {
    // Bloğun Vuruş Kutusu (Hitbox):
    // 16x16 genişlikte ama sadece 1 pikselden bile ince (0.05D) yükseklikte.
    // Bu, oyuncunun ona baktığında gördüğü siyah çerçevedir.
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 0.05D, 16.0D);

    // --- YENİ CONSTRUCTOR ---
    // Artık dışarıdan Properties almıyor, kendi içinde tanımlıyor.
    public CoinsBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.GOLD)       // Harita rengi (Kemik)
                .noOcclusion()                 // Şeffaf model için gerekli
                .strength(0.1F)                // Çok kolay kırılır
                .sound(SoundType.CHAIN)   // Kırılma/Koyma sesi: KEMİK
                .lightLevel(state -> 5)       // Işık seviyesi: 14 (Parlak)
        );
    }

    // Hitbox'ı döndür
    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    // Bu blok burada hayatta kalabilir mi? (Sadece zemine konma kuralı)
    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        BlockPos blockpos = pPos.below(); // Altındaki bloğun konumu
        BlockState blockstate = pLevel.getBlockState(blockpos); // Altındaki bloğun kendisi
        // Altındaki bloğun üst yüzeyi (Direction.UP) sağlam mı (sturdy)?
        // Bu, bloğun havaya veya çim gibi desteklenmeyen yüzeylere konmasını engeller.
        return blockstate.isFaceSturdy(pLevel, blockpos, Direction.UP);
    }

    // Komşu blok değiştiğinde ne yapayım? (Altımdaki kırılırsa ben de kırılayım)
    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        // Eğer aşağı yön (DOWN) değiştiyse ve artık hayatta kalamıyorsam...
        if (pFacing == Direction.DOWN && !pState.canSurvive(pLevel, pCurrentPos)) {
            // ...hava (AIR) bloğuna dönüş (yani kırıl).
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }
}