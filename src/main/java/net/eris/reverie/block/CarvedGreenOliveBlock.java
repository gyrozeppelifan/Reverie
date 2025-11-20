package net.eris.reverie.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class CarvedGreenOliveBlock extends Block {

    public CarvedGreenOliveBlock() {
        super(Properties
                .of()
                .mapColor(MapColor.COLOR_GREEN)
                .sound(SoundType.MUD)
                .strength(0.6f)
                .noOcclusion()
        );
    }

}
