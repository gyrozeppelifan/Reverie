package net.eris.reverie.entity.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.eris.reverie.entity.custom.FolkEntity;
import net.eris.reverie.init.ReveriePoiTypes;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import java.util.Optional;

public class FolkBrain {

    @SuppressWarnings("unchecked")
    public static void init(FolkEntity folk, Brain<FolkEntity> brain) {

        // CORE: Sadece Yürüme Motoru kaldı (Diğerleri Goal'de)
        brain.addActivity(Activity.CORE, ImmutableList.of(
                Pair.of(0, (BehaviorControl<FolkEntity>) (Object) new MoveToTargetSink())
        ));

        // WORK: İşe gitme ve sahiplenme
        brain.addActivity(Activity.WORK, ImmutableList.of(
                Pair.of(0, (BehaviorControl<FolkEntity>) (Object) SetWalkTargetFromBlockMemory.create(
                        MemoryModuleType.JOB_SITE, 0.6F, 2, 100, 20)),
                Pair.of(1, (BehaviorControl<FolkEntity>) (Object) AcquirePoi.create(
                        holder -> holder.is(ReveriePoiTypes.BARKEEPER_POI.getKey()),
                        MemoryModuleType.JOB_SITE, MemoryModuleType.JOB_SITE, true, Optional.empty())),
                Pair.of(2, (BehaviorControl<FolkEntity>) (Object) RandomStroll.stroll(0.5F))
        ));

        // IDLE: Sadece gezme
        brain.addActivity(Activity.IDLE, ImmutableList.of(
                Pair.of(0, (BehaviorControl<FolkEntity>) (Object) RandomStroll.stroll(0.5F))
        ));

        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.setSchedule(net.minecraft.world.entity.schedule.Schedule.VILLAGER_DEFAULT);
        brain.useDefaultActivity();
    }

    public static void updateActivity(FolkEntity folk) {
        folk.getBrain().updateActivityFromSchedule(folk.level().getDayTime(), folk.level().getGameTime());
    }
}