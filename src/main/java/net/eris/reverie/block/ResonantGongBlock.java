package net.eris.reverie.block;

import net.eris.reverie.block.entity.ResonantGongBlockEntity;
import net.eris.reverie.block.properties.GongPart;
import net.eris.reverie.init.ReverieModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ResonantGongBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<GongPart> PART = EnumProperty.create("part", GongPart.class);

    // --- FIX: YÖNE GÖRE HITBOX TANIMLARI ---
    // Kuzey/Güney bakarken (Z ekseninde ince)
    protected static final VoxelShape SHAPE_NORTH_SOUTH = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    // Doğu/Batı bakarken (X ekseninde ince)
    protected static final VoxelShape SHAPE_EAST_WEST = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

    public ResonantGongBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                .noOcclusion()
                .strength(5.0F)
                .sound(SoundType.ANVIL));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, GongPart.TOP_LEFT));
    }

    // --- FIX: GET SHAPE METODU ---
    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction facing = pState.getValue(FACING);
        // Eğer Kuzey veya Güney'e bakıyorsa Z-eksenli şekli, yoksa X-eksenli şekli ver
        return (facing == Direction.NORTH || facing == Direction.SOUTH) ? SHAPE_NORTH_SOUTH : SHAPE_EAST_WEST;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos clickedPos = context.getClickedPos();
        Level level = context.getLevel();
        Direction facing = context.getHorizontalDirection().getOpposite();

        BlockPos topRight = clickedPos.relative(facing.getClockWise());
        BlockPos bottomLeft = clickedPos.below();
        BlockPos bottomRight = clickedPos.below().relative(facing.getClockWise());

        if (level.isInWorldBounds(bottomRight) &&
                level.getBlockState(topRight).canBeReplaced(context) &&
                level.getBlockState(bottomLeft).canBeReplaced(context) &&
                level.getBlockState(bottomRight).canBeReplaced(context)) {

            return this.defaultBlockState().setValue(FACING, facing).setValue(PART, GongPart.TOP_LEFT);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            Direction facing = state.getValue(FACING);
            level.setBlock(pos.relative(facing.getClockWise()), state.setValue(PART, GongPart.TOP_RIGHT), 3);
            level.setBlock(pos.below(), state.setValue(PART, GongPart.BOTTOM_LEFT), 3);
            level.setBlock(pos.below().relative(facing.getClockWise()), state.setValue(PART, GongPart.BOTTOM_RIGHT), 3);
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            GongPart part = state.getValue(PART);
            Direction facing = state.getValue(FACING);

            BlockPos mainPos = pos;
            if (part == GongPart.TOP_RIGHT) mainPos = pos.relative(facing.getCounterClockWise());
            if (part == GongPart.BOTTOM_LEFT) mainPos = pos.above();
            if (part == GongPart.BOTTOM_RIGHT) mainPos = pos.above().relative(facing.getCounterClockWise());

            BlockPos[] allParts = { mainPos, mainPos.relative(facing.getClockWise()), mainPos.below(), mainPos.below().relative(facing.getClockWise()) };
            for (BlockPos p : allParts) {
                if (level.getBlockState(p).is(this)) level.setBlock(p, Blocks.AIR.defaultBlockState(), 35);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand == InteractionHand.MAIN_HAND) {
            GongPart part = state.getValue(PART);
            Direction facing = state.getValue(FACING);
            BlockPos mainPos = pos;
            if (part == GongPart.TOP_RIGHT) mainPos = pos.relative(facing.getCounterClockWise());
            if (part == GongPart.BOTTOM_LEFT) mainPos = pos.above();
            if (part == GongPart.BOTTOM_RIGHT) mainPos = pos.above().relative(facing.getCounterClockWise());

            BlockEntity be = level.getBlockEntity(mainPos);
            if (be instanceof ResonantGongBlockEntity gong) {
                gong.ring(player);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(PART) == GongPart.TOP_LEFT) return new ResonantGongBlockEntity(pos, state);
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ReverieModBlockEntities.RESONANT_GONG.get(), ResonantGongBlockEntity::tick);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }
}