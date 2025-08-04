package net.eris.reverie.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.init.ReverieModBlocks;
import net.eris.reverie.block.ElderOliveRightEyeBlock;
import net.eris.reverie.block.ElderOliveLeftEyeBlock;
import net.eris.reverie.block.ElderOliveMouthBlock;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ElderOliveTreeHandler {
    private static final int SCAN_RADIUS = 12;
    private static final int ANGRY_TICKS  = 20 * 20; // 10 saniye

    /**
     * Organ parçalarından herhangi birine kazma girişiminde
     * sadece RIGHT_EYE, LEFT_EYE, MOUTH bloklarını ANGRY yap.
     */
    @SubscribeEvent
    public static void onLeftClickBlock(LeftClickBlock event) {
        LevelAccessor la = event.getLevel();
        if (!(la instanceof Level level) || level.isClientSide()) return;

        Player player = event.getEntity();
        if (player.isCreative()) return;

        BlockPos pos = event.getPos();
        BlockState target = level.getBlockState(pos);

        // any organ?
        boolean isOrganPart =
                target.is(ReverieModBlocks.ELDER_OLIVE_HEART.get())
                        || target.is(ReverieModBlocks.ELDER_OLIVE_NOSE_UP.get())
                        || target.is(ReverieModBlocks.ELDER_OLIVE_NOSE_DOWN.get())
                        || target.is(ReverieModBlocks.ELDER_OLIVE_RIGHT_EYE.get())
                        || target.is(ReverieModBlocks.ELDER_OLIVE_LEFT_EYE.get())
                        || target.is(ReverieModBlocks.ELDER_OLIVE_MOUTH.get());
        if (!isOrganPart) return;

        List<BlockPos> angryList = new ArrayList<>();
        for (BlockPos p : BlockPos.betweenClosed(
                pos.offset(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),
                pos.offset( SCAN_RADIUS,  SCAN_RADIUS,  SCAN_RADIUS))) {

            BlockState st = level.getBlockState(p);
            // RIGHT_EYE
            if (st.is(ReverieModBlocks.ELDER_OLIVE_RIGHT_EYE.get())) {
                level.setBlock(p,
                        st.setValue(ElderOliveRightEyeBlock.STATE, ElderOliveRightEyeBlock.EyeState.ANGRY),
                        3
                );
                angryList.add(p.immutable());
            }
            // LEFT_EYE
            else if (st.is(ReverieModBlocks.ELDER_OLIVE_LEFT_EYE.get())) {
                level.setBlock(p,
                        st.setValue(ElderOliveLeftEyeBlock.STATE, ElderOliveLeftEyeBlock.EyeState.ANGRY),
                        3
                );
                angryList.add(p.immutable());
            }
            // MOUTH
            else if (st.is(ReverieModBlocks.ELDER_OLIVE_MOUTH.get())) {
                level.setBlock(p,
                        st.setValue(ElderOliveMouthBlock.STATE, ElderOliveMouthBlock.EyeState.ANGRY),
                        3
                );
                angryList.add(p.immutable());
            }
        }

        // 10 s sonra geri LIVING
        ReverieMod.queueServerWork(ANGRY_TICKS, () -> {
            for (BlockPos p : angryList) {
                BlockState st2 = level.getBlockState(p);
                if (st2.is(ReverieModBlocks.ELDER_OLIVE_RIGHT_EYE.get())) {
                    level.setBlock(p,
                            st2.setValue(ElderOliveRightEyeBlock.STATE, ElderOliveRightEyeBlock.EyeState.LIVING),
                            3
                    );
                }
                else if (st2.is(ReverieModBlocks.ELDER_OLIVE_LEFT_EYE.get())) {
                    level.setBlock(p,
                            st2.setValue(ElderOliveLeftEyeBlock.STATE, ElderOliveLeftEyeBlock.EyeState.LIVING),
                            3
                    );
                }
                else if (st2.is(ReverieModBlocks.ELDER_OLIVE_MOUTH.get())) {
                    level.setBlock(p,
                            st2.setValue(ElderOliveMouthBlock.STATE, ElderOliveMouthBlock.EyeState.LIVING),
                            3
                    );
                }
            }
        });
    }

    /**
     * Gerçekten kırıldığında (any organ),
     * tüm Elder Olive bloklarını DEAD_LOG’a dönüştür.
     */
    @SubscribeEvent
    public static void onBlockBreak(BreakEvent event) {
        LevelAccessor la = event.getLevel();
        if (!(la instanceof Level level) || level.isClientSide()) return;

        Player player = event.getPlayer();
        if (player.isCreative()) return;

        BlockPos origin = event.getPos();
        BlockState bs = level.getBlockState(origin);

        // any organ?
        boolean isOrgan =
                bs.is(ReverieModBlocks.ELDER_OLIVE_HEART.get())
                        || bs.is(ReverieModBlocks.ELDER_OLIVE_NOSE_UP.get())
                        || bs.is(ReverieModBlocks.ELDER_OLIVE_NOSE_DOWN.get())
                        || bs.is(ReverieModBlocks.ELDER_OLIVE_RIGHT_EYE.get())
                        || bs.is(ReverieModBlocks.ELDER_OLIVE_LEFT_EYE.get())
                        || bs.is(ReverieModBlocks.ELDER_OLIVE_MOUTH.get())
                        || bs.is(ReverieModBlocks.ELDER_OLIVE_LOG.get())
                        || bs.is(ReverieModBlocks.SHINY_ELDER_OLIVE_LOG.get());
        if (!isOrgan) return;

        for (BlockPos p : BlockPos.betweenClosed(
                origin.offset(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),
                origin.offset( SCAN_RADIUS,  SCAN_RADIUS,  SCAN_RADIUS))) {

            BlockState st = level.getBlockState(p);
            // sadece elder olive parçaları
            if (st.is(ReverieModBlocks.ELDER_OLIVE_HEART.get())
                    || st.is(ReverieModBlocks.ELDER_OLIVE_NOSE_UP.get())
                    || st.is(ReverieModBlocks.ELDER_OLIVE_NOSE_DOWN.get())
                    || st.is(ReverieModBlocks.ELDER_OLIVE_RIGHT_EYE.get())
                    || st.is(ReverieModBlocks.ELDER_OLIVE_LEFT_EYE.get())
                    || st.is(ReverieModBlocks.ELDER_OLIVE_MOUTH.get())
                    || st.is(ReverieModBlocks.ELDER_OLIVE_LOG.get())
                    || st.is(ReverieModBlocks.SHINY_ELDER_OLIVE_LOG.get())) {

                level.setBlock(p,
                        ReverieModBlocks.DEAD_LOG.get().defaultBlockState(),
                        3
                );
            }
        }
    }
}
