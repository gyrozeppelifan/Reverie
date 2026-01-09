package net.eris.reverie.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

public class CoffinTrestleBlock extends Block {
    public CoffinTrestleBlock() {
        // AAA kalite için bloğun sesini ve sertliğini oduna göre ayarlıyoruz.
        super(Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.5f)
                .requiresCorrectToolForDrops());
    }
}