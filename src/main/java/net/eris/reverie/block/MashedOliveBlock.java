package net.eris.reverie.block;

import net.eris.reverie.init.ReverieModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;

public class MashedOliveBlock extends Block {
    public static final BooleanProperty GREASY = BooleanProperty.create("greasy");

    public MashedOliveBlock() {
        super(BlockBehaviour.Properties
            .of()
            .sound(SoundType.HONEY_BLOCK)
            .strength(1.5f)
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(GREASY, false));
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 15;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(GREASY);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(GREASY, false);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        // Sadece greasy=true iken ve eldeki şişe boş ise
        if (state.getValue(GREASY) && held.getItem() == Items.GLASS_BOTTLE) {
            if (!world.isClientSide) {
                // Oyuncuya olive oil bottle ver
                ItemStack bottle = new ItemStack(ReverieModItems.OLIVE_OIL_BOTTLE.get());
                // Creative modunda şişe tüketilmesin
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                if (!player.getInventory().add(bottle)) {
                    player.drop(bottle, false);
                }
                // Bloku posalı hale getir
                world.setBlock(pos, state.setValue(GREASY, false), 3);
                // Ses efekti
                world.playSound(null, pos, SoundEvents.HONEY_BLOCK_FALL, SoundSource.BLOCKS, 1f, 1f);
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        return super.use(state, world, pos, player, hand, hit);
    }
}
