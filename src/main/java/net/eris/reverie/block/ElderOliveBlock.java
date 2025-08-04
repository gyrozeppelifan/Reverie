package net.eris.reverie.block;

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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.eris.reverie.init.ReverieModBlocks;

public class ElderOliveBlock extends Block {

    public ElderOliveBlock() {
        super(Properties
                .of()
                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                .sound(SoundType.BAMBOO_WOOD)
                .strength(0.7f)
                .noOcclusion()
        );
    }

    // Yaprak kırılınca düşür
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide
                && fromPos.equals(pos.above())
                && level.isEmptyBlock(fromPos)
        ) {
            tryFall(level, pos, state);
        }
    }

    // Okla vurulunca düşür
    @Override
    public void onProjectileHit(Level level, BlockState state,
                                BlockHitResult hit, Projectile projectile) {
        if (!level.isClientSide) {
            tryFall(level, hit.getBlockPos(), state);
        }
        super.onProjectileHit(level, state, hit, projectile);
    }

    private void tryFall(Level level, BlockPos pos, BlockState state) {
        // Alt boşsa
        if (level.isEmptyBlock(pos.below())) {
            level.removeBlock(pos, false);
            if (level instanceof ServerLevel server) {
                FallingBlockEntity.fall(server, pos, state);
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() instanceof ShearsItem) {
            if (!level.isClientSide) {
                // 1) Makas sesi
                level.playSound(
                        null,
                        pos,
                        SoundEvents.PUMPKIN_CARVE,        // makas sesi
                        SoundSource.BLOCKS,
                        1.0f,                     // ses seviyesi
                        1.0f                      // pitch
                );

                // 2) Carved versiyonunu koy
                level.setBlock(
                        pos,
                        ReverieModBlocks.CARVED_ELDER_OLIVE_BLOCK.get().defaultBlockState(),
                        3
                );

                // 3) Creative değilse makası yıprat
                if (!player.isCreative()) {
                    stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.use(state, level, pos, player, hand, hit);
    }

}
