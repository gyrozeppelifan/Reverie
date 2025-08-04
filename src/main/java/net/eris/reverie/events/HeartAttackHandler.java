// src/main/java/net/eris/reverie/events/HeartAttackHandler.java
package net.eris.reverie.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.init.ReverieModBlocks;
import net.eris.reverie.block.entity.ElderOliveHeartBlockEntity;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HeartAttackHandler {
    /**
     * Oyuncu kalp bloğuna kazmaya başladığında
     * hızlı nabız modunu tetiklemek için.
     */
    @SubscribeEvent
    public static void onLeftClickHeart(LeftClickBlock event) {
        LevelAccessor la = event.getLevel();
        if (!(la instanceof Level level) || level.isClientSide()) return;

        Player player = event.getEntity();
        if (player.isCreative()) return;

        BlockPos pos = event.getPos();
        // Sadece heart bloğuna tıklayınca
        if (!level.getBlockState(pos).is(ReverieModBlocks.ELDER_OLIVE_HEART.get())) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ElderOliveHeartBlockEntity heart) {
            heart.triggerFastPulse();
        }
    }
}
