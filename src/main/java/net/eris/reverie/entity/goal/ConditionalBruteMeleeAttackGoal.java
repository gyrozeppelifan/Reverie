package net.eris.reverie.entity.goal;

import net.eris.reverie.entity.GoblinBruteEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class ConditionalBruteMeleeAttackGoal extends MeleeAttackGoal {
    private final GoblinBruteEntity brute;

    public ConditionalBruteMeleeAttackGoal(GoblinBruteEntity brute, double speed, boolean useLongMemory) {
        super(brute, speed, useLongMemory);
        this.brute = brute;
    }

    private boolean isMeleeSuppressedState(GoblinBruteEntity.BruteState s) {
        // Melee’nin anlamsız veya çakıştığı tüm state’ler
        return s == GoblinBruteEntity.BruteState.SEEK_PLAYER
                || s == GoblinBruteEntity.BruteState.CARRY_PLAYER
                || s == GoblinBruteEntity.BruteState.CARRY_SHOOTER
                || s == GoblinBruteEntity.BruteState.CRYING
                || s == GoblinBruteEntity.BruteState.ROARING
                || s == GoblinBruteEntity.BruteState.CHARGING;
    }

    @Override
    public boolean canUse() {
        GoblinBruteEntity.BruteState state = brute.getState();
        if (isMeleeSuppressedState(state)) return false;
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        GoblinBruteEntity.BruteState state = brute.getState();
        if (isMeleeSuppressedState(state)) return false;
        return super.canContinueToUse();
    }
}
