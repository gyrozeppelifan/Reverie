package net.eris.reverie.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class CarvedBlackOliveBlock extends Block {

    public CarvedBlackOliveBlock() {
        super(Properties
                .of()
                .mapColor(MapColor.COLOR_BLACK)
                .sound(SoundType.MUD)
                .strength(0.6f)
                .noOcclusion()
        );
    }

}
