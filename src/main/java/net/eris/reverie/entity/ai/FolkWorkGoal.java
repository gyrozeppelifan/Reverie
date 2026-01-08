package net.eris.reverie.entity.ai;

import net.eris.reverie.entity.custom.FolkEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;

public class FolkWorkGoal extends MoveToBlockGoal {
    private final FolkEntity folk;

    public FolkWorkGoal(FolkEntity mob, double speed) {
        super(mob, speed, 16);
        this.folk = mob;
    }

    // 1.20.1 Forge hatasını çözen override metodu
    @Override
    public double acceptedDistance() {
        return 2.0D; // Bloğun dibinde durur, üstüne çıkmaz
    }

    @Override
    public boolean canUse() {
        // Sadece tapulu barı varsa ve mesai saatiyse git
        return folk.getWorkstationPos() != null && folk.level().getDayTime() % 24000 < 16000 && super.canUse();
    }

    @Override
    protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
        // Sadece KENDİ rezerve ettiği koordinat geçerli bir hedeftir
        return pPos.equals(folk.getWorkstationPos());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isReachedTarget()) {
            this.folk.getLookControl().setLookAt(this.blockPos.getX() + 0.5, this.blockPos.getY(), this.blockPos.getZ() + 0.5);
            // Her 15 saniyede bir 3 saniye çalışma animasyonu
            if (this.folk.tickCount % 300 == 0 && this.folk.getWorkingTicks() <= 0) {
                this.folk.setWorkingTicks(60);
            }
        }
    }
}