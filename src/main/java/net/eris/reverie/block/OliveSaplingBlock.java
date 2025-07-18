// src/main/java/net/eris/reverie/block/OliveSaplingBlock.java
package net.eris.reverie.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.eris.reverie.feature.OliveTreeGrower;

public class OliveSaplingBlock extends SaplingBlock {
    public OliveSaplingBlock() {
        super(
            new OliveTreeGrower(),
            BlockBehaviour.Properties
                .of()
                .randomTicks()       // kendi kendine büyüme
                .sound(SoundType.GRASS)
                .strength(0.1f)
                .noOcclusion()       // yarım blok gibi görünür
                .isRedstoneConductor((bs, br, bp) -> false)
        );
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        // Tamamen çarpışmasız
        return Shapes.empty();
    }
}
