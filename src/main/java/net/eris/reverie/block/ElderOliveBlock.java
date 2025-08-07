package net.eris.reverie.block;

import net.eris.reverie.init.ReverieModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

public class ElderOliveBlock extends Block {
    public static final BooleanProperty FERTILIZED = BooleanProperty.create("fertilized");
    private static final float TRANSFORM_CHANCE = 0.2f; // her randomTick'te %20 dönüşüm şansı

    public ElderOliveBlock() {
        super(Properties
                .of()
                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                .sound(SoundType.MUD)
                .strength(0.7f)
                .noOcclusion()
                .randomTicks()
        );
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FERTILIZED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) {
        b.add(FERTILIZED);
    }

    // --- mevcut falling ve carve mekanikleri aynı ---

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide
                && fromPos.equals(pos.above())
                && level.isEmptyBlock(fromPos)
        ) tryFall(level, pos, state);
    }

    @Override
    public void onProjectileHit(Level level, BlockState state,
                                BlockHitResult hit, Projectile proj) {
        if (!level.isClientSide) tryFall(level, hit.getBlockPos(), state);
        super.onProjectileHit(level, state, hit, proj);
    }

    private void tryFall(Level level, BlockPos pos, BlockState state) {
        if (level.isEmptyBlock(pos.below()) && level instanceof ServerLevel srv) {
            level.removeBlock(pos, false);
            FallingBlockEntity.fall(srv, pos, state);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof ShearsItem) {
            if (!level.isClientSide) {
                level.playSound(null, pos, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1, 1);
                level.setBlock(pos,
                        ReverieModBlocks.CARVED_ELDER_OLIVE_BLOCK.get().defaultBlockState(),
                        3
                );
                if (!player.isCreative()) stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    // --- randomTick ile GreenOliveBlock'a dönüşüm ---
    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
        boolean fert = state.getValue(FERTILIZED);
        if (rand.nextFloat() < TRANSFORM_CHANCE) {
            world.setBlock(pos,
                    ReverieModBlocks.GREEN_OLIVE_BLOCK.get()
                            .defaultBlockState()
                            .setValue(GreenOliveBlock.FERTILIZED, fert),
                    3
            );
        }
    }
}
