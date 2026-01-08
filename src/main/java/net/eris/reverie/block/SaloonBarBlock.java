package net.eris.reverie.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class SaloonBarBlock extends Block {
    public SaloonBarBlock() {
        // AAA kalite için bloğun sesini ve sertliğini oduna göre ayarlıyoruz.
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.5f)
                .requiresCorrectToolForDrops());
    }
}