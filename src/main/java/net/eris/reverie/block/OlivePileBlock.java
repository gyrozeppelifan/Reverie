package net.eris.reverie.block;

import net.eris.reverie.init.ReverieModBlocks;
import net.eris.reverie.block.MashedOliveBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class OlivePileBlock extends Block {
    public OlivePileBlock() {
        super(BlockBehaviour.Properties
            .of()
            .sound(SoundType.HONEY_BLOCK)  // Honey Block sesleri
            .strength(1.5f)
        );
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos,
                                Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, blockIn, fromPos, isMoving);

        // Üstte piston kafası mı ve aşağı bakıyor mu?
        BlockPos up = pos.above();
        BlockState aboveState = world.getBlockState(up);
        if (!aboveState.is(Blocks.PISTON_HEAD) ||
            aboveState.getValue(BlockStateProperties.FACING) != Direction.DOWN) {
            return;
        }

        // Altındaki blok hava mı? Eğer havaysa ezilmeyecek
        if (world.isEmptyBlock(pos.below())) {
            return;
        }

        // Ezilme sesi (Honey Block düşme sesi değil, ancak yine de uyumlu hissi için)
        world.playSound(
            null,
            pos,
            SoundEvents.HONEY_BLOCK_FALL, 
            SoundSource.BLOCKS,
            1.0F,
            1.0F
        );

        // OlivePile → MashedOlive (greasy = true)
        world.setBlock(pos,
            ReverieModBlocks.MASHED_OLIVE
                .get().defaultBlockState()
                .setValue(MashedOliveBlock.GREASY, true),
            3
        );
    }
}
