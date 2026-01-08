package net.eris.reverie.entity.ai;

import net.eris.reverie.entity.custom.FolkEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class FolkTradeWithPlayerGoal extends Goal {
    private final FolkEntity mob;

    public FolkTradeWithPlayerGoal(FolkEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isAlive()) {
            return false;
        } else if (this.mob.isInWater()) {
            return false;
        } else if (!this.mob.onGround()) {
            return false;
        } else {
            return this.mob.getTradingPlayer() != null; // Ticaret yaptığı biri varsa çalış
        }
    }

    @Override
    public void start() {
        this.mob.getNavigation().stop(); // Yürümeyi durdur
    }

    @Override
    public void stop() {
        this.mob.setTradingPlayer((Player)null); // Ticaret bitince oyuncuyu unut
    }
}