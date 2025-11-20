package net.eris.reverie.entity.goal;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.eris.reverie.util.GoblinReputation;

import java.util.EnumSet;

public class GoblinTargetPlayerGoal extends TargetGoal {
    private static final TagKey<EntityType<?>> GOBLINS =
            TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("reverie", "goblins"));

    private final Mob mob;
    private Player targetPlayer;

    public GoblinTargetPlayerGoal(Mob mob) {
        super(mob, false, true);
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    private static boolean isGoblin(Entity e) {
        return e != null && e.getType().is(GOBLINS);
    }

    // Creative/Spectator güvenli ve rep koşulu net
    private boolean isValidHostilePlayer(Player p) {
        if (p == null || !p.isAlive()) return false;
        if (p.isCreative() || p.isSpectator()) return false; // 1.20.1 için doğru kullanım

        var rep = GoblinReputation.getState(p);
        if (rep == GoblinReputation.State.AGGRESSIVE) return true;
        if (rep == GoblinReputation.State.NEUTRAL) {
            // Nötr halde sadece gerçekten bu oyuncu son vuransa
            return mob.getLastHurtByMob() == p;
        }
        return false;
    }

    @Override
    public boolean canUse() {
        // Son vuran goblinse, bu goal tetiklenmesin (goblin→goblin olayı yüzünden)
        if (isGoblin(mob.getLastHurtByMob())) return false;

        for (Player p : mob.level().getEntitiesOfClass(Player.class, mob.getBoundingBox().inflate(16))) {
            if (isValidHostilePlayer(p)) {
                this.targetPlayer = p;
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        if (isValidHostilePlayer(this.targetPlayer)) {
            mob.setTarget(this.targetPlayer);
        } else {
            this.targetPlayer = null;
        }
        super.start();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity current = mob.getTarget();
        if (!(current instanceof Player p)) return false;
        return isValidHostilePlayer(p); // Creative’a geçerse/spectator olursa/rep düşerse bırak
    }

    @Override
    public void stop() {
        this.targetPlayer = null;
        super.stop();
    }
}
