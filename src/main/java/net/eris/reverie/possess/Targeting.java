package net.eris.reverie.possess;

import net.eris.reverie.entity.PossessionPuppetEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class Targeting {
    private static final TagKey<EntityType<?>> GOBLINS =
            TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("reverie","goblins"));

    public static LivingEntity findPreferredTarget(Player p, double r){
        List<LivingEntity> list = p.level().getEntitiesOfClass(
                LivingEntity.class,
                p.getBoundingBox().inflate(r),
                e -> e.isAlive()
                        && e != p
                        && !(e instanceof PossessionPuppetEntity) // ❗ kuklayı asla hedefleme
                        // alternatif: && e.getType() != ReverieModEntities.POSSESSION_PUPPET.get()
                        && !e.getType().is(GOBLINS)
        );
        return list.stream()
                .sorted(Comparator
                        .<LivingEntity>comparingInt(e -> (e instanceof Player)?0:(e instanceof TamableAnimal)?1:2)
                        .thenComparingDouble(e -> e.distanceToSqr(p)))
                .findFirst().orElse(null);
    }

    public static BlockPos lookPoint(Player p, double dist){
        Vec3 from = p.getEyePosition();
        Vec3 to   = from.add(p.getViewVector(1.0f).scale(dist));

        HitResult hit = p.level().clip(new ClipContext(
                from, to,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                p
        ));

        Vec3 end = (hit.getType() != HitResult.Type.MISS) ? hit.getLocation() : to;
        return BlockPos.containing(end);
    }
}
