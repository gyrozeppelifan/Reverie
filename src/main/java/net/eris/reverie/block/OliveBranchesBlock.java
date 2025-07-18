package net.eris.reverie.block;

import net.eris.reverie.init.ReverieModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class OliveBranchesBlock extends Block implements SimpleWaterloggedBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 2);
    // Yarım blok yüksekliğinde ve kenarlardan 2 piksel içeri çekilmiş bir bounding box
    private static final VoxelShape SHAPE = Block.box(2, 8, 2, 14, 16, 14);

    public OliveBranchesBlock() {
        super(BlockBehaviour.Properties.of()
            .sound(SoundType.GRASS)
            .strength(0.2f)
            .noOcclusion()
            .randomTicks()
            .isRedstoneConductor((bs, br, bp) -> false)
        );
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(AGE, 0)
            .setValue(BlockStateProperties.WATERLOGGED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, BlockStateProperties.WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        boolean waterlogged = ctx.getLevel().getFluidState(pos).getType() == Fluids.WATER;
        if (ctx.getClickedFace() == Direction.DOWN) {
            return defaultBlockState()
                .setValue(AGE, 0)
                .setValue(BlockStateProperties.WATERLOGGED, waterlogged);
        }
        return null;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED)
            ? Fluids.WATER.getSource(false)
            : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState fstate,
                                  LevelAccessor world, BlockPos pos, BlockPos fpos) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        return super.updateShape(state, facing, fstate, world, pos, fpos);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return state.getFluidState().isEmpty();
    }

    // CollisionShape artık içten 2 px çekilmiş ve slab yüksekliğinde
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return Shapes.empty();
    }

    // Outline/visual shape de aynı
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return 10;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age < 2 && random.nextInt(7) == 0) {
            world.setBlock(pos, state.setValue(AGE, age + 1), 2);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        int age = state.getValue(AGE);
        if (age > 0) {
            if (!world.isClientSide) {
                ItemStack drop = new ItemStack(
                    age == 1
                        ? ReverieModItems.GREEN_OLIVE_BRANCH.get()
                        : ReverieModItems.BLACK_OLIVE_BRANCH.get()
                );
                popResource(world, pos, drop);
                world.setBlock(pos, state.setValue(AGE, 0), 2);
                world.playSound(null, pos,
                    SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
                    SoundSource.BLOCKS, 1f, 1f
                );
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return world.getBlockState(pos.above())
            .isFaceSturdy(world, pos.above(), Direction.DOWN);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos,
                                Block block, BlockPos fromPos, boolean moving) {
        if (fromPos.equals(pos.above()) &&
            (world.getBlockState(fromPos).isAir() ||
             world.getFluidState(fromPos).getType() == Fluids.WATER)
        ) {
            world.destroyBlock(pos, true);
        }
        super.neighborChanged(state, world, pos, block, fromPos, moving);
    }
}
