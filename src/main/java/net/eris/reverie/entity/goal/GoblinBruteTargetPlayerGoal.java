package net.eris.reverie.entity.goal;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.eris.reverie.util.GoblinReputation;

import java.util.EnumSet;

public class GoblinBruteTargetPlayerGoal extends TargetGoal {
    private static final TagKey<EntityType<?>> GOBLINS =
            TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("reverie", "goblins"));

    private final Mob brute;
    private Player targetPlayer;

    public GoblinBruteTargetPlayerGoal(Mob brute) {
        super(brute, false, true);
        this.brute = brute;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    private static boolean isGoblin(Entity e) {
        return e != null && e.getType().is(GOBLINS);
    }

    @Override
    public boolean canUse() {
        // Eğer en son vuran goblinse, "NEUTRAL + lastHurtByMob" istisnasını devre dışı bırak
        LivingEntity lastHurt = brute.getLastHurtByMob();
        boolean lastIsGoblin = isGoblin(lastHurt);

        // 64 blok içinde saldırılabilir oyuncu ara
        for (Player p : brute.level().getEntitiesOfClass(Player.class, brute.getBoundingBox().inflate(64))) {
            if (p.isCreative() || p.isSpectator()) continue;
            // Oyuncu zaten goblin taglı olamaz ama yine de kural net olsun:
            if (isGoblin(p)) continue;

            var rep = GoblinReputation.getState(p);
            boolean hostile =
                    rep == GoblinReputation.State.AGGRESSIVE ||
                            (rep == GoblinReputation.State.NEUTRAL && brute.getLastHurtByMob() == p && !lastIsGoblin);

            if (hostile) {
                this.targetPlayer = p;
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        if (targetPlayer != null && targetPlayer.isAlive() && !isGoblin(targetPlayer)) {
            brute.setTarget(targetPlayer);
        }
        super.start();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity current = brute.getTarget();
        // Hedef goblinse asla devam etme (her ihtimale karşı)
        if (isGoblin(current)) return false;

        if (!(current instanceof Player p)) return false;
        if (p.isCreative() || p.isSpectator()) return false;
        if (!p.isAlive()) return false;

        // Rep değişmişse hedefi bırak
        var rep = GoblinReputation.getState(p);
        if (rep != GoblinReputation.State.AGGRESSIVE &&
                !(rep == GoblinReputation.State.NEUTRAL && brute.getLastHurtByMob() == p)) {
            return false;
        }
        return true;
    }

    @Override
    public void stop() {
        targetPlayer = null;
        super.stop();
    }
}
