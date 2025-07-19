// CopperJunctionBlock.java
package net.eris.reverie.block;

import net.eris.reverie.block.entity.CopperJunctionBlockEntity;
import net.eris.reverie.block.CopperConduitBlock;
import net.eris.reverie.init.ReverieModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

public class CopperJunctionBlock extends Block implements EntityBlock {
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public CopperJunctionBlock() {
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
        b.add(POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(POWERED, false);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);

        // Hangi yöden tetiklendiğini bul
        Direction face = null;
        for (Direction d : Direction.values()) {
            if (pos.relative(d).equals(fromPos)) {
                face = d;
                break;
            }
        }
        if (face == null) return;

        BlockState nb = level.getBlockState(fromPos);

        // 1) Lightning Rod’dan enerji al
        if (nb.getBlock() == Blocks.LIGHTNING_ROD
                && nb.hasProperty(BlockStateProperties.POWERED)
                && nb.getValue(BlockStateProperties.POWERED)) {
            energize(level, pos, face);
            return;
        }

        // 2) CopperConduit’dan enerji al (ters yüz gerekli)
        if (nb.getBlock() instanceof CopperConduitBlock) {
            boolean conduitPowered = nb.getValue(CopperConduitBlock.POWERED);
            Direction conduitFace = nb.getValue(CopperConduitBlock.FACING);
            // Alırken conduit’un junction’a dönük ters yüzü bakması lazım
            if (conduitPowered && conduitFace == face.getOpposite()) {
                energize(level, pos, face);
            }
        }
    }

    /** Gelen yöne göre power cycle’ı başlatır */
    public void energize(Level level, BlockPos pos, Direction incoming) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CopperJunctionBlockEntity j) {
            j.startPowerCycle(incoming);
        }
    }

    // — BlockEntity boilerplate —
    public boolean hasBlockEntity(BlockState s) { return true; }

    @Nullable @Override
    public BlockEntity newBlockEntity(BlockPos p, BlockState s) {
        return new CopperJunctionBlockEntity(p, s);
    }
    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type
    ) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof CopperJunctionBlockEntity junc) {
                CopperJunctionBlockEntity.tick(lvl, pos, st, junc);
            }
        };
    }
}
