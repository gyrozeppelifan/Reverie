// HangingHideBlock.java
package net.eris.reverie.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.MapColor;

public class HangingHideBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public HangingHideBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_BROWN)
                .strength(0.4F)
                .sound(SoundType.WOOL)
                .noOcclusion()); // komşu alt/üst yüzler render edilsin (bizde top/bottom yok)
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING); }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        if (ctx.getClickedFace() != Direction.DOWN) return null; // sadece tavan altına
        BlockPos above = ctx.getClickedPos().above();
        Level level = ctx.getLevel();
        if (!level.getBlockState(above).isFaceSturdy(level, above, Direction.DOWN)) return null;
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        return level.getBlockState(above).isFaceSturdy(level, above, Direction.DOWN);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState other, LevelAccessor level, BlockPos pos, BlockPos otherPos) {
        if (dir == Direction.UP && !canSurvive(state, level, pos)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, dir, other, level, pos, otherPos);
    }

    @Override public BlockState rotate(BlockState s, Rotation r) { return s.setValue(FACING, r.rotate(s.getValue(FACING))); }
    @Override public BlockState mirror(BlockState s, Mirror m) { return s.rotate(m.getRotation(s.getValue(FACING))); }

    // isteğe bağlı: yanıcı olsun
    @Override public boolean isFlammable(BlockState s, BlockGetter l, BlockPos p, Direction f) { return true; }
    @Override public int getFlammability(BlockState s, BlockGetter l, BlockPos p, Direction f) { return 60; }
    @Override public int getFireSpreadSpeed(BlockState s, BlockGetter l, BlockPos p, Direction f) { return 30; }
}
