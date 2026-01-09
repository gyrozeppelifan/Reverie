package net.eris.reverie.entity.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.eris.reverie.entity.custom.FolkEntity;
import net.eris.reverie.init.ReverieActivities; // Bunu import etmeyi unutma!
import net.eris.reverie.init.ReveriePoiTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Optional;
import java.util.function.Predicate;

@SuppressWarnings({"unchecked", "rawtypes"})
public class FolkBrain {

    public static Brain<FolkEntity> create(Brain<FolkEntity> brain) {
        addCoreActivities(brain);
        addIdleActivities(brain);
        addWorkActivities(brain);
        addRestActivities(brain);
        addFightActivities(brain);
        addPanicActivities(brain);
        addTradeActivities(brain);
        addMeetActivities(brain); // GOSSIP İÇİN BULUŞMA

        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void addCoreActivities(Brain<FolkEntity> brain) {
        brain.addActivity(Activity.CORE, ImmutableList.of(
                Pair.of(0, new Swim(0.8F)),
                // Kapı açma davranışı (DOORS_TO_CLOSE hafızası şart)
                Pair.of(0, InteractWithDoor.create()),
                Pair.of(0, new LookAtTargetSink(45, 90)),
                Pair.of(0, new MoveToTargetSink())
        ));
    }

    private static void addTradeActivities(Brain<FolkEntity> brain) {
        // ReverieActivities.TRADE.get() kullanıyoruz (Sabit Durma)
        brain.addActivity(ReverieActivities.TRADE.get(), ImmutableList.of(
                Pair.of(0, new RunOne<FolkEntity>(ImmutableList.of(
                        Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 1)
                )))
        ));
    }

    // --- BULUŞMA & DEDİKODU (GOSSIP) ---
    private static void addMeetActivities(Brain<FolkEntity> brain) {
        brain.addActivity(ReverieActivities.MEET.get(), ImmutableList.of(
                Pair.of(0, new RunOne<FolkEntity>(ImmutableList.of(
                        // 1. Buluşma Noktasına Git
                        Pair.of(BehaviorBuilder.<FolkEntity>create(context ->
                                context.group(context.present(MemoryModuleType.MEETING_POINT)).apply(context, (meetMem) ->
                                        (world, entity, time) -> {
                                            GlobalPos gp = context.get(meetMem);
                                            if (gp.dimension() != world.dimension()) { meetMem.erase(); return true; }
                                            BlockPos pos = gp.pos();

                                            // Uzaksan Yürü
                                            if (pos.distSqr(entity.blockPosition()) > 6) {
                                                entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, 0.5F, 2));
                                            }
                                            // Yakındaysan Dedikodu Yap
                                            else {
                                                entity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent(mobs -> {
                                                    mobs.findClosest(e -> e instanceof FolkEntity && e.distanceToSqr(entity) < 9).ifPresent(target -> {
                                                        entity.getLookControl().setLookAt(target);
                                                        // %5 ihtimalle dedikodu paylaş
                                                        if (world.random.nextFloat() < 0.05F) {
                                                            entity.getGossipManager().shareGossipWith((FolkEntity) target);
                                                        }
                                                    });
                                                });
                                            }
                                            return true;
                                        }
                                )
                        ), 10),
                        // 2. Etrafta dolan
                        Pair.of(StrollAroundPoi.create(MemoryModuleType.MEETING_POINT, 0.7F, 15), 5)
                )))
        ));
    }

    private static void addIdleActivities(Brain<FolkEntity> brain) {
        brain.addActivity(Activity.IDLE, ImmutableList.of(
                Pair.of(0, new RunOne<FolkEntity>(ImmutableList.of(
                        Pair.of(RandomStroll.stroll(0.6F), 5),
                        Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 2),
                        Pair.of(new DoNothing(30, 60), 1)
                )))
        ));
    }

    private static void addRestActivities(Brain<FolkEntity> brain) {
        brain.addActivity(Activity.REST, ImmutableList.of(
                Pair.of(0, BehaviorBuilder.<FolkEntity>create(context ->
                        context.group(context.present(MemoryModuleType.HOME)).apply(context, (homeMem) ->
                                (world, entity, time) -> {
                                    GlobalPos homePos = context.get(homeMem);
                                    if (homePos.dimension() != world.dimension()) { homeMem.erase(); return true; }
                                    BlockPos pos = homePos.pos();
                                    if (pos.distSqr(entity.blockPosition()) > 4) entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, 1.0F, 1));
                                    else if (!entity.isSleeping()) entity.startSleeping(pos);
                                    return true;
                                }
                        )
                ))
        ));
    }

    private static void addWorkActivities(Brain<FolkEntity> brain) {
        brain.addActivity(Activity.WORK, ImmutableList.of(
                Pair.of(0, new RunOne<FolkEntity>(ImmutableList.of(
                        Pair.of(BehaviorBuilder.<FolkEntity>create(context ->
                                context.group(context.present(MemoryModuleType.JOB_SITE)).apply(context, (jobSiteMem) ->
                                        (world, entity, time) -> {
                                            GlobalPos globalPos = context.get(jobSiteMem);
                                            if (globalPos.dimension() != world.dimension()) { jobSiteMem.erase(); return true; }
                                            BlockPos pos = globalPos.pos();
                                            if (pos.distSqr(entity.blockPosition()) > 4) {
                                                entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, 1.0F, 1));
                                            } else {
                                                entity.getLookControl().setLookAt(pos.getX(), pos.getY(), pos.getZ());
                                                if (entity.getWorkingTicks() <= 0 && world.random.nextFloat() < 0.02F && entity.tryRestockTrades()) {
                                                    entity.setWorkingTicks(80); entity.playSound(SoundEvents.VILLAGER_WORK_LEATHERWORKER, 1.0F, 1.0F); world.broadcastEntityEvent(entity, (byte) 14);
                                                }
                                                if (entity.getProfessionId() != 1 && entity.getWorkingTicks() <= 0 && world.random.nextFloat() < 0.05F) return false;
                                            }
                                            return true;
                                        }
                                )
                        ), 10),
                        Pair.of(StrollAroundPoi.create(MemoryModuleType.JOB_SITE, 0.4F, 5), 5)
                )))
        ));
    }

    private static void addFightActivities(Brain<FolkEntity> brain) {
        brain.addActivity(Activity.FIGHT, ImmutableList.of(
                Pair.of(0, StopAttackingIfTargetInvalid.<FolkEntity>create(target -> target instanceof Player p && (p.isCreative() || p.isSpectator()))),
                Pair.of(1, SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.35F)),
                Pair.of(2, BehaviorBuilder.<FolkEntity>create(context -> context.group(context.present(MemoryModuleType.ATTACK_TARGET), context.absent(MemoryModuleType.ATTACK_COOLING_DOWN)).apply(context, (targetMem, cooldownMem) -> (world, entity, time) -> {
                    LivingEntity target = context.get(targetMem);
                    if (entity.isWithinMeleeAttackRange(target)) { entity.doHurtTarget(target); entity.swing(net.minecraft.world.InteractionHand.MAIN_HAND); cooldownMem.setWithExpiry(true, 20L); return true; }
                    return false;
                })))
        ));
    }

    private static void addPanicActivities(Brain<FolkEntity> brain) {
        brain.addActivity(Activity.PANIC, ImmutableList.of(Pair.of(0, SetWalkTargetAwayFrom.entity(MemoryModuleType.HURT_BY_ENTITY, 1.75F, 12, true))));
    }

    // --- TICK METODU ---
    public static void tick(FolkEntity folk) {
        Brain<FolkEntity> brain = folk.getBrain();
        ServerLevel level = (ServerLevel) folk.level();
        PoiManager poiManager = level.getPoiManager();

        // 1. İŞ, EV VE BULUŞMA NOKTASI ARAMA
        if (folk.tickCount % 20 == 0) {
            // A) İş Bulma
            if (!brain.hasMemoryValue(MemoryModuleType.JOB_SITE)) {
                if (folk.getProfessionId() == 0) {
                    // İşsizse her şeyi ara
                    Optional<BlockPos> poi = poiManager.take(holder ->
                                    holder.is(ReveriePoiTypes.BARKEEPER_POI.getKey()) || holder.is(ReveriePoiTypes.GUNSMITH_POI.getKey()) ||
                                            holder.is(ReveriePoiTypes.TAILOR_POI.getKey()) || holder.is(ReveriePoiTypes.STABLE_MASTER_POI.getKey()) ||
                                            holder.is(ReveriePoiTypes.BANKER_POI.getKey()) || holder.is(ReveriePoiTypes.BOUNTY_CLERK_POI.getKey()) ||
                                            holder.is(ReveriePoiTypes.UNDERTAKER_POI.getKey()),
                            (holder, pos) -> true, folk.blockPosition(), 48);

                    poi.ifPresent(blockPos -> {
                        brain.setMemory(MemoryModuleType.JOB_SITE, GlobalPos.of(level.dimension(), blockPos));
                        Optional<Holder<PoiType>> type = poiManager.getType(blockPos);
                        if (type.isPresent()) {
                            if (type.get().is(ReveriePoiTypes.BARKEEPER_POI.getKey())) folk.setProfessionId(1);
                            else if (type.get().is(ReveriePoiTypes.GUNSMITH_POI.getKey())) folk.setProfessionId(2);
                            else if (type.get().is(ReveriePoiTypes.TAILOR_POI.getKey())) folk.setProfessionId(3);
                            else if (type.get().is(ReveriePoiTypes.STABLE_MASTER_POI.getKey())) folk.setProfessionId(4);
                            else if (type.get().is(ReveriePoiTypes.BANKER_POI.getKey())) folk.setProfessionId(5);
                            else if (type.get().is(ReveriePoiTypes.BOUNTY_CLERK_POI.getKey())) folk.setProfessionId(6);
                            else if (type.get().is(ReveriePoiTypes.UNDERTAKER_POI.getKey())) folk.setProfessionId(7);
                            folk.playSound(SoundEvents.VILLAGER_WORK_MASON, 1.0F, 1.0F); level.broadcastEntityEvent(folk, (byte) 14);
                        }
                    });
                } else {
                    // Mesleği varsa sadece kendi masasını ara (Meslek Koruma)
                    Predicate<Holder<PoiType>> targetPoi = switch (folk.getProfessionId()) {
                        case 1 -> h -> h.is(ReveriePoiTypes.BARKEEPER_POI.getKey());
                        case 2 -> h -> h.is(ReveriePoiTypes.GUNSMITH_POI.getKey());
                        case 3 -> h -> h.is(ReveriePoiTypes.TAILOR_POI.getKey());
                        case 4 -> h -> h.is(ReveriePoiTypes.STABLE_MASTER_POI.getKey());
                        case 5 -> h -> h.is(ReveriePoiTypes.BANKER_POI.getKey());
                        case 6 -> h -> h.is(ReveriePoiTypes.BOUNTY_CLERK_POI.getKey());
                        case 7 -> h -> h.is(ReveriePoiTypes.UNDERTAKER_POI.getKey());
                        default -> h -> false;
                    };
                    Optional<BlockPos> existingPoi = poiManager.take(targetPoi, (holder, pos) -> true, folk.blockPosition(), 48);
                    existingPoi.ifPresent(blockPos -> {
                        brain.setMemory(MemoryModuleType.JOB_SITE, GlobalPos.of(level.dimension(), blockPos));
                        folk.playSound(SoundEvents.VILLAGER_WORK_MASON, 1.0F, 1.0F); level.broadcastEntityEvent(folk, (byte) 14);
                    });
                }
            }

            // B) Ev Bulma
            if (!brain.hasMemoryValue(MemoryModuleType.HOME)) {
                Optional<BlockPos> bed = poiManager.take(holder -> holder.is(PoiTypes.HOME), (holder, pos) -> true, folk.blockPosition(), 48);
                bed.ifPresent(pos -> { brain.setMemory(MemoryModuleType.HOME, GlobalPos.of(level.dimension(), pos)); level.broadcastEntityEvent(folk, (byte) 14); });
            }

            // C) Buluşma Noktası (Altın Blok) Bulma
            if (!brain.hasMemoryValue(MemoryModuleType.MEETING_POINT)) {
                Optional<BlockPos> meetPos = poiManager.take(holder -> holder.is(ReveriePoiTypes.MEETING_POI.getKey()), (holder, pos) -> true, folk.blockPosition(), 64);
                meetPos.ifPresent(pos -> brain.setMemory(MemoryModuleType.MEETING_POINT, GlobalPos.of(level.dimension(), pos)));
            }
        }

        // 2. POI GEÇERLİLİK KONTROLÜ
        if (folk.tickCount % 100 == 0) {
            // İş yeri kırıldı mı?
            if (brain.hasMemoryValue(MemoryModuleType.JOB_SITE)) {
                GlobalPos gp = brain.getMemory(MemoryModuleType.JOB_SITE).get();
                boolean isValid = gp.dimension() == level.dimension() && level.getPoiManager().exists(gp.pos(), holder ->
                        holder.is(ReveriePoiTypes.BARKEEPER_POI.getKey()) || holder.is(ReveriePoiTypes.GUNSMITH_POI.getKey()) ||
                                holder.is(ReveriePoiTypes.TAILOR_POI.getKey()) || holder.is(ReveriePoiTypes.STABLE_MASTER_POI.getKey()) ||
                                holder.is(ReveriePoiTypes.BANKER_POI.getKey()) || holder.is(ReveriePoiTypes.BOUNTY_CLERK_POI.getKey()) ||
                                holder.is(ReveriePoiTypes.UNDERTAKER_POI.getKey()));

                if (!isValid) {
                    brain.eraseMemory(MemoryModuleType.JOB_SITE);
                    // XP yoksa işi bırak, varsa işi koru
                    if (folk.getFolkXp() == 0) folk.setProfessionId(0);
                }
            }
            // Ev kırıldı mı?
            if (brain.hasMemoryValue(MemoryModuleType.HOME)) {
                GlobalPos hp = brain.getMemory(MemoryModuleType.HOME).get();
                if (hp.dimension() == level.dimension() && !level.getPoiManager().exists(hp.pos(), holder -> holder.is(PoiTypes.HOME))) {
                    brain.eraseMemory(MemoryModuleType.HOME);
                }
            }
            // Buluşma noktası kırıldı mı?
            if (brain.hasMemoryValue(MemoryModuleType.MEETING_POINT)) {
                GlobalPos mp = brain.getMemory(MemoryModuleType.MEETING_POINT).get();
                if (mp.dimension() == level.dimension() && !level.getPoiManager().exists(mp.pos(), holder -> holder.is(ReveriePoiTypes.MEETING_POI.getKey()))) {
                    brain.eraseMemory(MemoryModuleType.MEETING_POINT);
                }
            }
        }

        // 3. TİCARET KONTROLÜ (Sabit Durma)
        if (folk.isTrading()) {
            Player customer = folk.getTradingPlayer();
            if (customer == null || customer.distanceToSqr(folk) > 16.0D || !customer.isAlive()) {
                folk.setTradingPlayer(null);
            } else {
                brain.eraseMemory(MemoryModuleType.WALK_TARGET);
                brain.eraseMemory(MemoryModuleType.PATH);
                if (!brain.isActive(ReverieActivities.TRADE.get())) brain.setActiveActivityIfPossible(ReverieActivities.TRADE.get());
                folk.getLookControl().setLookAt(customer.getX(), customer.getEyeY(), customer.getZ());
                return; // Diğer işlere bakma
            }
        }

        // 4. SAVAŞ & PANİK
        boolean isThreatened = false;
        if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();
            if ((target instanceof Player player && (player.isCreative() || player.isSpectator())) || folk.distanceToSqr(target) > 400.0D) {
                brain.eraseMemory(MemoryModuleType.ATTACK_TARGET); brain.eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
            }
        }
        if (brain.hasMemoryValue(MemoryModuleType.HURT_BY_ENTITY) && !brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            Optional<LivingEntity> attacker = brain.getMemory(MemoryModuleType.HURT_BY_ENTITY);
            if (attacker.isPresent()) {
                LivingEntity enemy = attacker.get();
                if (folk.distanceToSqr(enemy) > 256.0D || !enemy.isAlive()) brain.eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
                else if (folk.isHoldingWeapon() && !(enemy instanceof Player player && player.isCreative())) brain.setMemory(MemoryModuleType.ATTACK_TARGET, enemy);
            }
        }
        if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            if (!brain.isActive(Activity.FIGHT)) brain.setActiveActivityIfPossible(Activity.FIGHT); isThreatened = true;
        } else if (brain.hasMemoryValue(MemoryModuleType.HURT_BY_ENTITY)) {
            if (!brain.isActive(Activity.PANIC)) brain.setActiveActivityIfPossible(Activity.PANIC); isThreatened = true;
        }
        if (!isThreatened && (brain.isActive(Activity.PANIC) || brain.isActive(Activity.FIGHT))) {
            brain.setActiveActivityIfPossible(Activity.IDLE);
        }

        // 5. VARDİYA SİSTEMİ (06:00 Uyan, 08:00 İş, 17:00 Buluşma, 22:00 Uyku)
        if (!isThreatened && !folk.isTrading()) {
            long time = level.getDayTime() % 24000;
            int profession = folk.getProfessionId();

            // Sabah uyanma
            if (folk.isSleeping() && time < 16000 && time > 0) folk.stopSleeping();

            // İŞ VAKTİ (08:00 - 17:00)
            if (profession != 0 && time > 2000 && time < 11000) {
                if (!brain.isActive(Activity.WORK)) brain.setActiveActivityIfPossible(Activity.WORK);
            }
            // BULUŞMA VAKTİ (17:00 - 22:00)
            else if (time >= 11000 && time < 16000) {
                if (brain.hasMemoryValue(MemoryModuleType.MEETING_POINT)) {
                    if (!brain.isActive(ReverieActivities.MEET.get())) brain.setActiveActivityIfPossible(ReverieActivities.MEET.get());
                } else {
                    // Buluşma noktası yoksa boş gez
                    if (!brain.isActive(Activity.IDLE)) brain.setActiveActivityIfPossible(Activity.IDLE);
                }
            }
            // UYKU VAKTİ (22:00 sonrası)
            else if (time >= 16000 && brain.hasMemoryValue(MemoryModuleType.HOME)) {
                if (!brain.isActive(Activity.REST)) brain.setActiveActivityIfPossible(Activity.REST);
            }
            else {
                if (!brain.isActive(Activity.IDLE)) brain.setActiveActivityIfPossible(Activity.IDLE);
            }
        }
    }
}