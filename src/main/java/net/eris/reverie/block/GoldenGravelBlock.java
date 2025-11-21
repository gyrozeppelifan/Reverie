package net.eris.reverie.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class GoldenGravelBlock extends FallingBlock {
    public GoldenGravelBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.GOLD)
                .strength(0.6F)
                .sound(SoundType.SAND) // Üstüne basınca çıtırdayan ses (veya metal istersen CHAIN)
        );
    }
}