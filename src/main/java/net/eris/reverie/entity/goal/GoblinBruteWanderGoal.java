package net.eris.reverie.entity.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import net.eris.reverie.entity.GoblinBruteEntity;

import java.util.EnumSet;
import java.util.Random;

public class GoblinBruteWanderGoal extends Goal {
    private final GoblinBruteEntity brute;
    private final double speed;
    private Vec3 targetPos;
    private final Random rand = new Random();

    public GoblinBruteWanderGoal(GoblinBruteEntity brute, double speed) {
        this.brute = brute;
        this.speed = speed;
        // Yalnızca hareket bayrağı
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // 1) Sadece server tarafı
        if (brute.level().isClientSide) return false;
        // 2) Kesinlikle sadece IDLE state’inde
        if (brute.getState() != GoblinBruteEntity.BruteState.IDLE) return false;
        // 3) Eğer navigation hâlâ devam ediyorsa (başka bir hareket goal’ü aktif) bekle
        if (!brute.getNavigation().isDone()) return false;
        // 4) %1 ihtimalle yeni bir pozisyon ara
        if (rand.nextInt(100) != 0) return false;
        // 5) Gerçekten yürünebilir bir nokta bul
        Vec3 pos = LandRandomPos.getPos(brute, 16, 7);
        if (pos == null) return false;

        targetPos = pos;
        return true;
    }

    @Override
    public void start() {
        // En başta da bir kez state kontrolü
        if (brute.getState() != GoblinBruteEntity.BruteState.IDLE) {
            this.stop();
            return;
        }
        brute.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, speed);
    }

    @Override
    public boolean canContinueToUse() {
        // 1) Sadece IDLE’de devam
        if (brute.getState() != GoblinBruteEntity.BruteState.IDLE) return false;
        // 2) Navigation hâlâ gidiyorsa devam et
        return !brute.getNavigation().isDone();
    }

    @Override
    public void tick() {
        // Her tick başı eğer state değiştiyse dur
        if (brute.getState() != GoblinBruteEntity.BruteState.IDLE) {
            this.stop();
            return;
        }
        // Aksi takdirde navigation zaten start’ta verildiği için devam edecek
    }

    @Override
    public void stop() {
        targetPos = null;
        brute.getNavigation().stop();
    }
}
