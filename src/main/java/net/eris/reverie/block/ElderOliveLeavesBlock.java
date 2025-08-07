package net.eris.reverie.block;

import net.eris.reverie.init.ReverieModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.eris.reverie.block.ElderOliveBlock;

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

	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return !state.getValue(PERSISTENT);
	}

	@Override
	public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		if (!state.getValue(PERSISTENT)
				&& world.isRaining()
				&& world.canSeeSky(pos)
				&& world.isEmptyBlock(pos.below())
		) {
			// %30 şansla blok oluştur
			if (random.nextFloat() < 0.3f) {
				// Oluşan blok %20 ihtimalle fertilized=true
				boolean fertilized = random.nextFloat() < 0.2f;
				world.setBlock(
						pos.below(),
						ReverieModBlocks.ELDER_OLIVE_BLOCK.get()
								.defaultBlockState()
								.setValue(ElderOliveBlock.FERTILIZED, fertilized),
						3
				);
			}
		}
	}
}
