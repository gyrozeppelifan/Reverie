
package net.eris.reverie.block;

import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

public class OliveFenceGateBlock extends FenceGateBlock {
	public OliveFenceGateBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(2f).forceSolidOn(), WoodType.OAK);
	}

	@Override
	public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return 5;
	}
}
