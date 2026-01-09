package net.eris.reverie.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

public class BountyBoardBlock extends Block {
    public BountyBoardBlock() {
        // AAA kalite için bloğun sesini ve sertliğini oduna göre ayarlıyoruz.
        super(Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.5f)
                .requiresCorrectToolForDrops());
    }
}