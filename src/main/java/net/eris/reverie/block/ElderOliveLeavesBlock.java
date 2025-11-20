package net.eris.reverie.block;

import net.eris.reverie.init.ReverieModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

// Eğer farklı paketteyse bunu aç:
// import net.eris.reverie.block.ElderOliveBlock;

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

	// TÜM doğal yapraklar tick alsın (vanilla decay yine super.randomTick'te koşul kontrollü çalışır)
	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return !state.getValue(PERSISTENT);
	}

	@Override
	public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		// 1) Önce vanilla decay çalışsın
		super.randomTick(state, level, pos, random);

		// 2) Yaprak hâlâ yerinde mi? (decay ile kırılmış olabilir)
		BlockState now = level.getBlockState(pos);
		if (now.getBlock() != this) return;

		// 3) Persistent değilse VE decay durumunda değilse (DISTANCE < 7) yağmur bonusunu uygula
		if (!now.getValue(PERSISTENT)
				&& now.getValue(DISTANCE) < 7
				&& level.isRainingAt(pos.above())
				&& level.isEmptyBlock(pos.below())) {

			if (random.nextFloat() < 0.20f) {
				boolean fertilized = random.nextFloat() < 0.20f;
				level.setBlock(
						pos.below(),
						net.eris.reverie.init.ReverieModBlocks.ELDER_OLIVE_BLOCK.get()
								.defaultBlockState()
								.setValue(net.eris.reverie.block.ElderOliveBlock.FERTILIZED, fertilized),
						3
				);
			}
		}
	}
}

