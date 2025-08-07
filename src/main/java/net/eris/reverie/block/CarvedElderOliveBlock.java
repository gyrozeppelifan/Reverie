package net.eris.reverie.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class CarvedElderOliveBlock extends Block {

    public CarvedElderOliveBlock() {
        super(Properties
                .of()
                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                .sound(SoundType.MUD)
                .strength(0.6f)
                .noOcclusion()
        );
    }

}
