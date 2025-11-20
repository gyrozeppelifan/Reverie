// LeatherBlock.java
package net.eris.reverie.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class LeatherBlock extends Block {

    public LeatherBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_BROWN) // dokuya yakın sıcak ton
                .strength(0.6F)                      // elle kırılır, odun gibi
                .sound(SoundType.WOOL)               // yumuşak “pof” sesi
                .ignitedByLava());                   // lav kıvılcımından tutuşabilir (opsiyonel)
    }

    // — Flammability ayarları (odun seviyesine yakın, istersen düşür/yükselt) —
    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return true;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return 60;   // tutuşma kolaylığı (≈ wool/tahta arası)
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return 30;   // yayılma hızı
    }
}
