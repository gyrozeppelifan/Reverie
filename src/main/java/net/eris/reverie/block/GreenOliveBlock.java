package net.eris.reverie.block;

import net.eris.reverie.init.ReverieModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;

public class GreenOliveBlock extends FallingBlock {
    public static final BooleanProperty FERTILIZED = BooleanProperty.create("fertilized");
    private static final float TRANSFORM_CHANCE = 0.2f; // %20 per randomTick

    public GreenOliveBlock() {
        super(Properties
                .of()
                .mapColor(MapColor.COLOR_GREEN)
                .sound(SoundType.CROP)
                .strength(0.5f)
                .randomTicks()
        );
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FERTILIZED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) {
        b.add(FERTILIZED);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
        boolean fert = state.getValue(FERTILIZED);
        if (rand.nextFloat() < TRANSFORM_CHANCE) {
            world.setBlock(pos,
                    ReverieModBlocks.BLACK_OLIVE_BLOCK.get()
                            .defaultBlockState()
                            .setValue(BlackOliveBlock.FERTILIZED, fert),
                    3
            );
        }
    }
}
