package net.eris.reverie.block;

import net.eris.reverie.block.entity.CopperConduitBlockEntity;
import net.eris.reverie.init.ReverieModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public class CopperConduitBlock extends Block implements EntityBlock {
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());

    public CopperConduitBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(2.0f, 6.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.COPPER)                        // Copper place/break sound
                .lightLevel(state -> state.getValue(POWERED) ? 14 : 0)
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(POWERED, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        // Tıklanan yüzü al:
        Direction face = ctx.getClickedFace();
        // Eğer horizontal ise face olarak, değilse (up/down) de face olarak
        return this.defaultBlockState()
                .setValue(FACING, face)
                .setValue(POWERED, false);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void neighborChanged(BlockState state, Level lvl, BlockPos pos, Block block, BlockPos from, boolean isMoving) {
        super.neighborChanged(state, lvl, pos, block, from, isMoving);
        BlockPos above = pos.above();
        BlockState up = lvl.getBlockState(above);
        if (up.getBlock() == Blocks.LIGHTNING_ROD
                && up.hasProperty(BlockStateProperties.POWERED)
                && up.getValue(BlockStateProperties.POWERED)) {
            this.energize(lvl, pos, Direction.UP);
        }
    }

    public void energize(Level lvl, BlockPos pos, Direction dir) {
        BlockEntity be = lvl.getBlockEntity(pos);
        if (be instanceof CopperConduitBlockEntity conduit) {
            conduit.startPowerCycle();
        }
    }

    // --- BlockEntity boilerplate ---

    public boolean hasBlockEntity(BlockState state) {
        return true;
    }

    @Nullable @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState st) {
        return new CopperConduitBlockEntity(pos, st);
    }

    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level lvl, BlockState st, BlockEntityType<T> type) {
        if (lvl.isClientSide) return null;
        return (level, pos, state, be) -> {
            if (be instanceof CopperConduitBlockEntity ccb) {
                CopperConduitBlockEntity.tick(level, pos, state, ccb);
            }
        };
    }

    // --- Energy transfer helper ---
    public void transferEnergy(Level lvl, BlockPos pos, Direction fromDir) {
        Direction out = lvl.getBlockState(pos).getValue(FACING);
        BlockPos tgt = pos.relative(out);
        BlockState nb = lvl.getBlockState(tgt);
        if (nb.getBlock() instanceof CopperConduitBlock) {
            ((CopperConduitBlock)nb.getBlock()).energize(lvl, tgt, out.getOpposite());
        }
    }
}
