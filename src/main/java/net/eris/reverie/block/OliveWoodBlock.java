package net.eris.reverie.block;

import net.eris.reverie.init.ReverieModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.BlockHitResult;

public class OliveWoodBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    public OliveWoodBlock() {
        super(BlockBehaviour.Properties.of()
            .sound(SoundType.WOOD)
            .strength(2.0f)
            .requiresCorrectToolForDrops()   // Vanilla mekanikte doğru alet zorunlu
        );
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(AXIS, Direction.Axis.Y)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
            .setValue(AXIS, context.getClickedFace().getAxis());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        if (rot == Rotation.CLOCKWISE_90 || rot == Rotation.COUNTERCLOCKWISE_90) {
            Direction.Axis axis = state.getValue(AXIS);
            if (axis == Direction.Axis.X) axis = Direction.Axis.Z;
            else if (axis == Direction.Axis.Z) axis = Direction.Axis.X;
            return state.setValue(AXIS, axis);
        }
        return state;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return 5;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof AxeItem) {
            if (!world.isClientSide) {
                // Stripped versiyonuna dönüştür, ekseni koru
                BlockState stripped = ReverieModBlocks.STRIPPED_OLIVE_WOOD.get()
                    .defaultBlockState()
                    .setValue(AXIS, state.getValue(AXIS));
                world.setBlock(pos, stripped, 3);
                world.playSound(null, pos, SoundEvents.AXE_STRIP,
                                SoundSource.BLOCKS, 1f, 1f);
                // Baltaya hasar ver
                held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return super.use(state, world, pos, player, hand, hit);
    }
}
