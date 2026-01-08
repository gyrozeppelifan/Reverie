package net.eris.reverie.entity.ai;

import net.eris.reverie.entity.custom.FolkEntity;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;

public class FolkLookAtTradingPlayerGoal extends LookAtPlayerGoal {
    private final FolkEntity mob;

    public FolkLookAtTradingPlayerGoal(FolkEntity mob) {
        super(mob, Player.class, 8.0F);
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        if (this.mob.getTradingPlayer() != null) {
            this.lookAt = this.mob.getTradingPlayer(); // Bakılacak hedef ticaret yapılan oyuncudur
            return true;
        } else {
            return false;
        }
    }
}