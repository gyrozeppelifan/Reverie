package net.eris.reverie.block;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

import java.util.Locale;

public class ElderOliveMouthBlock extends HorizontalDirectionalBlock {
    /** Sadece yatay yön tutmak için */
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    /** Blok durumu: living, angry veya death */
    public static final EnumProperty<EyeState> STATE =
            EnumProperty.create("state", EyeState.class);

    public ElderOliveMouthBlock() {
        super(BlockBehaviour.Properties.of()
                .sound(SoundType.CHERRY_WOOD)
                .strength(15.0f)
                .requiresCorrectToolForDrops()
        );
        // Varsayılan: kuzeye baksın ve living state’te olsun
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(STATE, EyeState.LIVING)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STATE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction dir = context.getHorizontalDirection().getOpposite();
        return this.defaultBlockState()
                .setValue(FACING, dir)
                .setValue(STATE, EyeState.LIVING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        // Mirror.getRotation returns a Rotation to apply
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    /** Blok durumları için enum */
    public enum EyeState implements StringRepresentable {
        LIVING,
        ANGRY;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
