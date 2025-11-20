package net.eris.reverie.block;

import net.eris.reverie.entity.GobletEntity;
import net.eris.reverie.init.ReverieModBlocks;
import net.eris.reverie.init.ReverieModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

public class BlackOliveBlock extends FallingBlock {
    public static final BooleanProperty FERTILIZED = BooleanProperty.create("fertilized");
    public static final IntegerProperty STAGE      = IntegerProperty.create("stage", 0, 2);

    private static final float PROGRESS_CHANCE = 0.2f; // %20 per randomTick
    private static final int HATCH_WAIT_TICKS = 160 * 20; // 160s × 20

    public BlackOliveBlock() {
        super(Properties
                .of()
                .mapColor(MapColor.COLOR_BLACK)
                .sound(SoundType.CROP)
                .strength(0.5f)
                .randomTicks()
        );
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FERTILIZED, false)
                .setValue(STAGE, 0)
        );
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
                        ReverieModBlocks.CARVED_BLACK_OLIVE_BLOCK.get().defaultBlockState(),
                        3
                );
                if (!player.isCreative()) stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) {
        b.add(FERTILIZED, STAGE);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(FERTILIZED);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
        int stage = state.getValue(STAGE);

        if (stage < 2) {
            // 0→1→2 progression
            if (rand.nextFloat() < PROGRESS_CHANCE) {
                BlockState next = state.setValue(STAGE, stage + 1);
                world.setBlock(pos, next, 3);

                // – Ses çıkar
                world.playSound(null, pos, SoundEvents.SNIFFER_EGG_HATCH, SoundSource.BLOCKS, 1f, 1f);

                // – Partikül yay: yeşil “growth” hissi (Happy Villager)
                double cx = pos.getX() + 0.5;
                double cy = pos.getY() + 0.5;
                double cz = pos.getZ() + 0.5;
                world.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        cx, cy, cz,
                        10,    // count
                        0.25,  // dx
                        0.25,  // dy
                        0.25,  // dz
                        0.02   // speed
                );

                // Eğer yeni stage==2 ise, 160s sonra spawn tetiklesin
                if (stage + 1 == 2) {
                    world.scheduleTick(pos, this, HATCH_WAIT_TICKS);
                }
            }

        } else {
            // stage==2 için scheduleTick sonrası tick() çağrısı
            // --- before hatch efekti ---
            // Ses: yumurtadan kırılma efekti gibi
            world.playSound(null, pos, SoundEvents.SNIFFER_EGG_HATCH, SoundSource.BLOCKS, 1f, 1f);

            // Partikül: büyükçe bir growth patlaması
            double cx = pos.getX() + 0.5;
            double cy = pos.getY() + 0.5;
            double cz = pos.getZ() + 0.5;
            world.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    cx, cy, cz,
                    20,    // count
                    0.5,   // dx
                    0.5,   // dy
                    0.5,   // dz
                    0.05   // speed
            );

            // spawn goblet ve blok kaldır
            world.removeBlock(pos, false);
            GobletEntity goblet = ReverieModEntities.GOBLET.get().create(world);
            if (goblet != null) {
                goblet.setPos(cx, pos.getY(), cz);
                world.addFreshEntity(goblet);
            }
        }
    }
}
