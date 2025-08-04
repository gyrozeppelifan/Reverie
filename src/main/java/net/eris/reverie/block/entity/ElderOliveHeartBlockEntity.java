// src/main/java/net/eris/reverie/block/entity/ElderOliveHeartBlockEntity.java
package net.eris.reverie.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.eris.reverie.init.ReverieModBlockEntities;

public class ElderOliveHeartBlockEntity extends BlockEntity {
    private static final int NORMAL_INTERVAL = 60;
    private static final int FAST_INTERVAL   = 20;
    private static final int FAST_DURATION   = 200;

    private int pulseTimer     = 0;
    private int echoTimer      = 0;
    private int fastPulseTimer = 0;

    public ElderOliveHeartBlockEntity(BlockPos pos, BlockState state) {
        super(ReverieModBlockEntities.ELDER_OLIVE_HEART.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ElderOliveHeartBlockEntity be) {
        if (!(level instanceof ServerLevel)) return;

        int interval = be.fastPulseTimer > 0 ? FAST_INTERVAL : NORMAL_INTERVAL;
        if (be.fastPulseTimer > 0) be.fastPulseTimer--;

        be.pulseTimer++;
        if (be.pulseTimer >= interval) {
            be.pulseTimer = 0;
            level.playSound(null, pos, SoundEvents.WARDEN_HEARTBEAT, SoundSource.BLOCKS, 2.0f, 2.0f);
            be.echoTimer = interval / 3;
        }

        if (be.echoTimer > 0) {
            be.echoTimer--;
            if (be.echoTimer == 0) {
                level.playSound(null, pos, SoundEvents.WARDEN_HEARTBEAT, SoundSource.BLOCKS, 2.0f, 2.0f);
            }
        }
    }

    /** Hızlı moda geç */
    public void triggerFastPulse() {
        this.fastPulseTimer = FAST_DURATION;
    }

    /** Renderer için mevcut aralığı döndürür */
    public int getCurrentPulseInterval() {
        return this.fastPulseTimer > 0 ? FAST_INTERVAL : NORMAL_INTERVAL;
    }

    /** Renderer için BE içindeki sayaç */
    public int getPulseTimer() {
        return this.pulseTimer;
    }
}
