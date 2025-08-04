// src/main/java/net/eris/reverie/block/ElderOliveHeartBlock.java
package net.eris.reverie.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.eris.reverie.init.ReverieModBlockEntities;
import net.eris.reverie.block.entity.ElderOliveHeartBlockEntity;

public class ElderOliveHeartBlock extends BaseEntityBlock {
    private static final int LIGHT_LEVEL = 7;

    public ElderOliveHeartBlock() {
        super(BlockBehaviour.Properties.of()
                .sound(SoundType.STONE)
                .strength(3.0f, 120.0f)
                .requiresCorrectToolForDrops()
                .lightLevel(state -> LIGHT_LEVEL)
                .noOcclusion()
        );
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElderOliveHeartBlockEntity(pos, state);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,
                                                                  BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide && type == ReverieModBlockEntities.ELDER_OLIVE_HEART.get()) {
            return (lvl, pos, st, tile) -> {
                if (tile instanceof ElderOliveHeartBlockEntity heart) {
                    ElderOliveHeartBlockEntity.tick(lvl, pos, st, heart);
                }
            };
        }
        return null;
    }


    public boolean isEmissiveRendering(BlockState state) {
        return true;
    }
}
