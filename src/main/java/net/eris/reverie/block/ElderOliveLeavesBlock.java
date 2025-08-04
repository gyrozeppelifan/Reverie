package net.eris.reverie.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.eris.reverie.init.ReverieModBlocks;

public class ElderOliveLeavesBlock extends LeavesBlock {
	public ElderOliveLeavesBlock() {
		super(Properties
				.of()
				.mapColor(MapColor.COLOR_LIGHT_BLUE)
				.sound(SoundType.AZALEA_LEAVES)
				.strength(0.6f)
				.noOcclusion()
				.randomTicks()
		);
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
		return true;
	}

	@Override
	public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 0;
	}

	// 1) Kesin random tick alması için
	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return !state.getValue(PERSISTENT);
	}

	// 2) RandomTick içinde yukarıdaki yağmur/gökyüzü kontrolü
	@Override
	public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		// Sadece doğal yapraklar
		if (!state.getValue(PERSISTENT)
				// Global yağmur yağıyor mu?
				&& world.isRaining()
				// O pozisyona gökyüzü görünür mü?
				&& world.canSeeSky(pos)
				// Altı boş mu?
				&& world.isEmptyBlock(pos.below())
		) {
			// %40 şansla spawn et
			if (random.nextFloat() < 0.3f) {
				world.setBlock(
						pos.below(),
						ReverieModBlocks.ELDER_OLIVE_BLOCK.get().defaultBlockState(),
						3
				);
			}
		}
	}
}
