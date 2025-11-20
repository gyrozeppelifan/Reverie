package net.eris.reverie.possess;

import net.eris.reverie.entity.PossessionPuppetEntity;
import net.eris.reverie.init.ReverieModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PossessionPathManager {
    private static final Map<UUID, PossessionPuppetEntity> ACTIVE = new HashMap<>();
    private static final int RECALC_TICKS = 10;

    public static void ensure(ServerPlayer sp){
        ACTIVE.computeIfAbsent(sp.getUUID(), id -> {
            PossessionPuppetEntity e = ReverieModEntities.POSSESSION_PUPPET.get().create(sp.level());
            e.moveTo(sp.getX(), sp.getY(), sp.getZ(), sp.getYRot(), sp.getXRot());
            ((ServerLevel)sp.level()).addFreshEntity(e);
            return e;
        });
    }

    public static boolean has(ServerPlayer sp){ return ACTIVE.containsKey(sp.getUUID()); }

    public static void stop(ServerPlayer sp){
        var e = ACTIVE.remove(sp.getUUID());
        if (e != null) e.discard();
    }

    public static void updateTargetAndPath(ServerPlayer sp){
        var puppet = ACTIVE.get(sp.getUUID());
        if (puppet == null) return;

        if (puppet.level() != sp.level() || puppet.isRemoved()){
            stop(sp); ensure(sp); puppet = ACTIVE.get(sp.getUUID());
        }
        if (sp.tickCount % RECALC_TICKS != 0) return;

        LivingEntity t = Targeting.findPreferredTarget(sp, 16);
        if (t != null && t.isAlive()){
            puppet.getNavigation().moveTo(t, 1.2);
        } else {
            BlockPos pos = Targeting.lookPoint(sp, 16);
            puppet.getNavigation().moveTo(pos.getX()+0.5, pos.getY(), pos.getZ()+0.5, 1.2);
        }
    }

    /** Oyuncunun yönelmesi gereken nokta (yoksa null). */
    public static Vec3 steerPoint(ServerPlayer sp){
        var puppet = ACTIVE.get(sp.getUUID());
        if (puppet == null) return null;

        Path path = puppet.getNavigation().getPath();
        if (path != null && !path.isDone()){
            Node n = path.getNextNode();
            if (n != null) return new Vec3(n.x+0.5, n.y, n.z+0.5);
        }
        if (puppet.position().distanceToSqr(sp.position()) < 0.25) return null; // çok yakınsa dürtme
        return puppet.position();
    }

    /** Sonraki path düğümü (zıplama kararı için). Null olabilir. */
    public static Node nextNode(ServerPlayer sp){
        var puppet = ACTIVE.get(sp.getUUID());
        if (puppet == null) return null;
        Path path = puppet.getNavigation().getPath();
        return (path != null && !path.isDone()) ? path.getNextNode() : null;
    }
}
