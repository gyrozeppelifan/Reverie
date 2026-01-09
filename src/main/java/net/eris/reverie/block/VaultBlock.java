package net.eris.reverie.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

public class VaultBlock extends Block {
    public VaultBlock() {
        // AAA kalite için bloğun sesini ve sertliğini oduna göre ayarlıyoruz.
        super(Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.5f)
                .requiresCorrectToolForDrops());
    }
}