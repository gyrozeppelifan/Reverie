package net.eris.reverie.block;

import net.eris.reverie.entity.GobletEntity;
import net.eris.reverie.init.ReverieModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;

public class BlackOliveBlock extends FallingBlock {
    public static final BooleanProperty FERTILIZED = BooleanProperty.create("fertilized");
    public static final IntegerProperty STAGE      = IntegerProperty.create("stage", 0, 2);

    private static final float PROGRESS_CHANCE = 0.2f; // %20 per randomTick
    private static final int HATCH_WAIT_TICKS = 160 * 20; // 160s × 20

    public BlackOliveBlock() {
        super(Properties
                .of()
                .mapColor(MapColor.COLOR_BLACK)
                .sound(SoundType.CROP)
                .strength(0.5f)
                .randomTicks()
        );
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FERTILIZED, false)
                .setValue(STAGE, 0)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) {
        b.add(FERTILIZED, STAGE);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(FERTILIZED);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
        int stage = state.getValue(STAGE);

        if (stage < 2) {
            // 0→1→2 progression
            if (rand.nextFloat() < PROGRESS_CHANCE) {
                world.setBlock(pos, state.setValue(STAGE, stage + 1), 3);
                // Eğer yeni stage==2 ise, 160s sonra spawn tetiklesin
                if (stage + 1 == 2) {
                    world.scheduleTick(pos, this, HATCH_WAIT_TICKS);
                }
            }

        } else {
            // stage==2 için scheduleTick sonrası tick() çağrısı
            // spawn goblet ve blok kaldır
            world.removeBlock(pos, false);
            EntityType<GobletEntity> type = ReverieModEntities.GOBLET.get();
            GobletEntity goblet = type.create(world);
            if (goblet != null) {
                goblet.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                world.addFreshEntity(goblet);
            }
        }
    }
}
