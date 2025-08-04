package net.eris.reverie.entity.goal;

import net.eris.reverie.entity.BarrelGoblinEntity;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraftforge.registries.ForgeRegistries;

public class DefendOwnerGoal extends TargetGoal {
    private static final TagKey<EntityType<?>> GOBLINS_TAG =
            TagKey.create(
                    ForgeRegistries.ENTITY_TYPES.getRegistryKey(),
                    new ResourceLocation("reverie", "goblins")
            );

    private final BarrelGoblinEntity goblin;
    private LivingEntity owner;
    private LivingEntity attacker;

    public DefendOwnerGoal(BarrelGoblinEntity goblin) {
        super(goblin, false, true);
        this.goblin = goblin;
    }

    @Override
    public boolean canUse() {
        owner = goblin.getOwner();
        if (owner == null || !owner.isAlive()) return false;

        attacker = owner.getLastHurtByMob();
        // Eğer saldırgan bir goblinse, atla
        if (attacker != null && attacker.getType().is(GOBLINS_TAG)) {
            return false;
        }
        return attacker != null && attacker.isAlive();
    }

    @Override
    public void start() {
        goblin.setTarget(attacker);
        super.start();
    }
}
