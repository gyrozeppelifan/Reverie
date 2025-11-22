package net.eris.reverie.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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

    // Vuruş Kutusu (Hitbox) Tanımı:
    // Modelinize uygun olarak merkezde 4x4 piksel genişliğinde.
    protected static final VoxelShape SHAPE = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 16.0D, 10.0D);

    // --- YENİ CONSTRUCTOR ---
    // Artık dışarıdan Properties almıyor, kendi içinde tanımlıyor.
    public WildTorchBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.SAND)       // Harita rengi (Kemik)
                .noOcclusion()                 // Şeffaf model için gerekli
                .strength(0.2F)                // Çok kolay kırılır
                .sound(SoundType.BONE_BLOCK)   // Kırılma/Koyma sesi: KEMİK
                .lightLevel(state -> 14)       // Işık seviyesi: 14 (Parlak)
        );
    }

    // Oyuncunun gördüğü siyah çerçeve (Hitbox)
    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    // Çarpışma Kutusu (Collision Box):
    // Shapes.empty() döndürerek içinden geçilebilir (non-solid) olmasını sağlıyoruz.
    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.empty();
    }

    // Partikül Efektleri (Duman ve Alev):
    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        // Sadece ÜST (UPPER) yarıdan partikül çıksın.
        if (pState.getValue(HALF) == DoubleBlockHalf.UPPER) {
            double d0 = (double)pPos.getX() + 0.5D;
            double d1 = (double)pPos.getY() + 0.7D; // Ateşin çıkış yüksekliği
            double d2 = (double)pPos.getZ() + 0.5D;

            // Arada sırada duman
            if (pRandom.nextInt(5) == 0) {
                pLevel.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            }
            // Sürekli alev
            pLevel.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
    }
}