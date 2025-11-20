package net.eris.reverie.entity.goal;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.eris.reverie.util.GoblinReputation;

public class GoblinHurtByTargetGoal extends HurtByTargetGoal {
    private static final TagKey<EntityType<?>> GOBLINS =
            TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("reverie", "goblins"));

    private final PathfinderMob mob;

    public GoblinHurtByTargetGoal(PathfinderMob mob) {
        super(mob);
        this.mob = mob;
    }

    private static boolean isGoblin(Entity e) {
        return e != null && e.getType().is(GOBLINS);
    }

    private static boolean isHostilePlayer(Player p, LivingEntity self) {
        if (p == null || p.isCreative() || p.isSpectator()) return false;
        var rep = GoblinReputation.getState(p);
        // Sadece AGGRESSIVE ya da (NEUTRAL + gerçekten bu entity’i dövmüş) ise
        return rep == GoblinReputation.State.AGGRESSIVE ||
                (rep == GoblinReputation.State.NEUTRAL && self.getLastHurtByMob() == p);
    }

    @Override
    public boolean canUse() {
        LivingEntity attacker = mob.getLastHurtByMob();
        if (attacker == null) return false;

        // Goblin goblini tetiklemez
        if (isGoblin(attacker)) return false;

        if (attacker instanceof Player p) {
            return isHostilePlayer(p, mob);
        }
        // Oyuncu değilse (zombi vs.), normal davran
        return super.canUse();
    }

    @Override
    public void start() {
        LivingEntity attacker = mob.getLastHurtByMob();
        if (attacker == null || isGoblin(attacker) ||
                (attacker instanceof Player p && !isHostilePlayer(p, mob))) {
            mob.setTarget(null);
            this.stop();
            return;
        }
        super.start();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity tgt = mob.getTarget();
        if (tgt == null || !tgt.isAlive()) return false;

        // Hedef goblin çıktıysa anında bırak
        if (isGoblin(tgt)) return false;

        if (tgt instanceof Player p) {
            if (!isHostilePlayer(p, mob)) return false;
        }
        return super.canContinueToUse();
    }

    @Override
    protected boolean canAttack(LivingEntity target, net.minecraft.world.entity.ai.targeting.TargetingConditions conditions) {
        if (target == null) return false;
        if (isGoblin(target)) return false; // sigorta
        if (target instanceof Player p) {
            return isHostilePlayer(p, mob);
        }
        return super.canAttack(target, conditions);
    }
}
