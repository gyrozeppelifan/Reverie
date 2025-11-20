package net.eris.reverie.mixins;

import net.eris.reverie.entity.PossessionPuppetEntity;
import net.eris.reverie.init.ReverieModMobEffects;
import net.eris.reverie.possess.MeleeUtil;
import net.eris.reverie.possess.PossessionPathManager;
import net.eris.reverie.possess.Targeting;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    private static final String NBT_JUMP_CD      = "reverie.possession.jump_cd";
    private static final String NBT_STEP_FLAG    = "reverie.possession.step_flag"; // 0 reset, 1 boosted(base)
    private static final String NBT_STUCK_T      = "reverie.possession.stuck_ticks";
    private static final String NBT_LAST_BX      = "reverie.possession.last_bx";
    private static final String NBT_LAST_BZ      = "reverie.possession.last_bz";
    private static final String NBT_JUMP_ARM     = "reverie.possession.jump_arm";   // armalı zıplama
    private static final String NBT_STEPBOOST_T  = "reverie.possession.step_boost"; // geçici step
    private static final String NBT_SOFTFALL_T   = "reverie.possession.soft_fall";  // düşüş iptali
    private static final String NBT_NOPROG_T     = "reverie.possession.noprog_t";   // ilerleme yok sayacı
    private static final String NBT_LAST_PUP_D   = "reverie.possession.last_puppet_d";
    private static final int    NOPROG_LIMIT     = 30; // ≈1.5s @20tps

    @Inject(method = "doTick", at = @At("TAIL"))
    private void reverie$possession_driver(CallbackInfo ci){
        ServerPlayer sp = (ServerPlayer)(Object)this;

        boolean active = sp.hasEffect(ReverieModMobEffects.POSSESSION.get());
        if (!active) {
            applyStep(sp, 0.6f, 0);
            resetStuck(sp);
            if (PossessionPathManager.has(sp)) PossessionPathManager.stop(sp);
            return;
        }
        if (sp.isSpectator() || sp.isCreative() || sp.isPassenger()) return;

        // 0) soft-fall aktifse düşüş mesafesini sıfırla
        tickSoftFall(sp);

        // 1) step + sprint
        applyStep(sp, 1.12f, 1);
        tickStepBoost(sp);
        sp.setSprinting(true);

        // 2) puppet / path
        PossessionPathManager.ensure(sp);
        PossessionPathManager.updateTargetAndPath(sp);

        // 3) steer → itiş
        Vec3 steer = PossessionPathManager.steerPoint(sp);
        if (steer != null){
            Vec3 delta = steer.subtract(sp.position());
            if (delta.lengthSqr() > 0.0004){
                Vec3 dir = delta.normalize();
                Vec3 vel = sp.getDeltaMovement().scale(0.5).add(dir.x*0.25, 0, dir.z*0.25);
                sp.setDeltaMovement(vel);
                sp.hurtMarked = true;

                // === NO-PROGRESS VAULT (CD'den ÖNCE) ===
                if (precheckNoProgressVault(sp, delta)) return;

                tryAutoJumpSmart(sp, delta); // normal akış
            }
        }

        // 4) hedef → auto-equip & auto-attack / kiss
        LivingEntity target = Targeting.findPreferredTarget(sp, 16);
        if (target == null || target instanceof PossessionPuppetEntity || !sp.hasLineOfSight(target)) return;

        int best = MeleeUtil.bestHotbarSlot(sp, target);
        if (best >= 0 && sp.getInventory().selected != best){
            sp.getInventory().selected = best;
        }

        // bedeni hedefe çevir
        float yaw = (float)Math.toDegrees(Math.atan2(-(target.getX()-sp.getX()), (target.getZ()-sp.getZ())));
        float newYaw = lerpAngle(sp.getYRot(), yaw, 0.35f);
        sp.setYRot(newYaw); sp.yBodyRot = newYaw; sp.yHeadRot = newYaw;

        // menzildeyse: %5 öpücük, aksi halde normal saldırı
        double reach = 3.0;
        if (sp.distanceToSqr(target) <= reach*reach && sp.getAttackStrengthScale(0) >= 1.0f){

            if (sp.getRandom().nextFloat() < 0.05f) {
                // --- KISS MODE ---
                if (sp.level() instanceof ServerLevel sl) {
                    double hx = target.getX();
                    double hy = target.getY() + target.getBbHeight() * 0.75;
                    double hz = target.getZ();
                    sl.sendParticles(ParticleTypes.HEART, hx, hy, hz, 24, 0.6, 0.6, 0.6, 0.02);
                    sl.sendParticles(ParticleTypes.HEART, hx, hy + 0.5, hz, 18, 0.4, 0.4, 0.4, 0.02);
                }
                SoundEvent kiss = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("reverie","kissing"));
                if (kiss != null) {
                    sp.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            kiss, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
                sp.swing(sp.getUsedItemHand(), true);
                sp.resetAttackStrengthTicker();

                if (target instanceof ServerPlayer kissed) {
                    Advancement adv = sp.server.getAdvancements()
                            .getAdvancement(new ResourceLocation("reverie", "forbidden_fanfiction"));
                    if (adv != null) {
                        kissed.getAdvancements().award(adv, "forbidden_fanfiction_0");
                    }
                }

            } else {
                sp.attack(target);
                sp.resetAttackStrengthTicker();
                sp.swing(sp.getUsedItemHand(), true);
            }
        }
    }

    // === Step / Soft-fall helpers ===
    private static void applyStep(ServerPlayer sp, float value, int flag){
        var tag = sp.getPersistentData();
        if (tag.getInt(NBT_STEP_FLAG) != flag){
            sp.setMaxUpStep(value);
            tag.putInt(NBT_STEP_FLAG, flag);
        }
    }
    private static void tickStepBoost(ServerPlayer sp){
        var tag = sp.getPersistentData();
        int t = tag.getInt(NBT_STEPBOOST_T);
        if (t > 0){
            tag.putInt(NBT_STEPBOOST_T, t - 1);
            sp.setMaxUpStep(1.35f);
        } else {
            if (tag.getInt(NBT_STEP_FLAG) == 1){
                sp.setMaxUpStep(1.12f);
            }
        }
    }
    private static void boostStep(ServerPlayer sp, int ticks){
        var tag = sp.getPersistentData();
        tag.putInt(NBT_STEPBOOST_T, Math.max(tag.getInt(NBT_STEPBOOST_T), ticks));
        sp.setMaxUpStep(1.35f);
    }
    private static void tickSoftFall(ServerPlayer sp){
        var tag = sp.getPersistentData();
        int t = tag.getInt(NBT_SOFTFALL_T);
        if (t > 0){
            tag.putInt(NBT_SOFTFALL_T, t - 1);
            sp.resetFallDistance();
        }
    }

    private static void resetStuck(ServerPlayer sp){
        var tag = sp.getPersistentData();
        tag.remove(NBT_STUCK_T);
        tag.remove(NBT_LAST_BX);
        tag.remove(NBT_LAST_BZ);
        tag.remove(NBT_JUMP_CD);
        tag.remove(NBT_JUMP_ARM);
        tag.remove(NBT_STEPBOOST_T);
        tag.remove(NBT_SOFTFALL_T);
        tag.remove(NBT_NOPROG_T);
        tag.remove(NBT_LAST_PUP_D);
    }

    /** CD'den önce çalışır: 1.5s ilerleme yoksa zorunlu vault (engel || nodeUp) ve uçurum değilse. */
    private static boolean precheckNoProgressVault(ServerPlayer sp, Vec3 steerDelta){
        var tag = sp.getPersistentData();

        // kuklaya mevcut uzaklık (yatay)
        double curD = Math.sqrt(steerDelta.x*steerDelta.x + steerDelta.z*steerDelta.z);
        double lastD = tag.getDouble(NBT_LAST_PUP_D);
        if (lastD <= 0) lastD = curD;

        // yatay hız
        Vec3 v = sp.getDeltaMovement();
        double speedH = Math.sqrt(v.x*v.x + v.z*v.z);

        boolean progressed = (lastD - curD) > 0.12D || speedH >= 0.08D;

        int t = tag.getInt(NBT_NOPROG_T);
        t = progressed ? 0 : Math.min(NOPROG_LIMIT+5, t + 1);
        tag.putInt(NBT_NOPROG_T, t);
        tag.putDouble(NBT_LAST_PUP_D, curD);

        if (t >= NOPROG_LIMIT){
            boolean puppetFar = (steerDelta.x*steerDelta.x + steerDelta.z*steerDelta.z) > 36.0D; // 6+ blok
            double probe = puppetFar ? 1.0D : 0.6D;

            boolean obstacle = hasObstacleAheadWithHeadroom(sp, steerDelta.x, steerDelta.z, probe, /*requireMoving=*/false);
            boolean needJump = obstacle || nextNodeRequiresJump(sp, puppetFar);

            if (needJump && !isCliffAhead(sp, steerDelta.x, steerDelta.z, probe, 2)){
                // zemin şüpheliyse force hop, yoksa normal jump
                if (sp.onGround()) {
                    doJumpForward(sp, steerDelta, puppetFar);
                } else {
                    doForcedHopForward(sp, steerDelta, puppetFar);
                }
                // soft-fall + kısa CD
                tag.putInt(NBT_SOFTFALL_T, Math.max(tag.getInt(NBT_SOFTFALL_T), puppetFar ? 16 : 12));
                tag.putInt(NBT_JUMP_CD, puppetFar ? 4 : 5);
                tag.putInt(NBT_NOPROG_T, 0);
                tag.putDouble(NBT_LAST_PUP_D, curD);
                return true;
            } else {
                // koşullar tutmadıysa sayacı biraz geriye al ki sık denemesin
                tag.putInt(NBT_NOPROG_T, Math.max(0, t - 8));
            }
        }
        return false;
    }

    /** Akıllı zıplama akışı (CD burada devreye girer). */
    private static void tryAutoJumpSmart(ServerPlayer sp, Vec3 steerDelta){
        var tag = sp.getPersistentData();

        int cd = tag.getInt(NBT_JUMP_CD);
        if (cd > 0){ tag.putInt(NBT_JUMP_CD, cd-1); return; }

        if (!sp.onGround() || sp.isInWaterOrBubble() || sp.isInLava()) { trackStuck(sp, false); return; }

        boolean puppetFar = (steerDelta.x*steerDelta.x + steerDelta.z*steerDelta.z) > 36.0D;

        // Armed: engel + headroom ve uçurum değilse → zıpla
        int armed = tag.getInt(NBT_JUMP_ARM);
        if (armed > 0){
            tag.putInt(NBT_JUMP_ARM, armed - 1);
            double probe = puppetFar ? 1.0D : 0.6D;
            if (hasObstacleAheadWithHeadroom(sp, steerDelta.x, steerDelta.z, probe, /*requireMoving=*/false)
                    && !isCliffAhead(sp, steerDelta.x, steerDelta.z, probe, 2)){
                doJumpForward(sp, steerDelta, puppetFar);
                tag.putInt(NBT_JUMP_CD, puppetFar ? 3 : 5);
                return;
            }
        }

        boolean jumped = false;

        // --- Node temelli ---
        Node n = PossessionPathManager.nextNode(sp);
        if (n != null){
            int footY = Mth.floor(sp.getY());
            boolean nodeUp = (n.y >= footY + 1);

            double nx = (n.x + 0.5) - sp.getX();
            double nz = (n.z + 0.5) - sp.getZ();
            double nodeDistSq = nx*nx + nz*nz;

            double nearThresh = puppetFar ? 1.44D : 0.80D;
            double aheadProbe  = puppetFar ? 1.0D  : 0.6D;

            if (nodeUp && nodeDistSq < nearThresh){
                if (armJumpIfTooSlow(sp, nx, nz, puppetFar)) return;

                if (hasObstacleAheadWithHeadroom(sp, nx, nz, aheadProbe)
                        && !isCliffAhead(sp, nx, nz, aheadProbe, 2)){
                    doJumpForward(sp, new Vec3(nx,0,nz), puppetFar);
                    tag.putInt(NBT_JUMP_CD, puppetFar ? 3 : 5);
                    jumped = true;
                }
            }
        }

        // --- Çarpışma bazlı ---
        if (!jumped){
            double[] probes = puppetFar ? new double[]{0.6, 0.9, 1.2} : new double[]{0.6, 0.9};
            for (double d : probes){
                if (armJumpIfTooSlow(sp, steerDelta.x, steerDelta.z, puppetFar)) return;

                if (hasObstacleAheadWithHeadroom(sp, steerDelta.x, steerDelta.z, d)
                        && !isCliffAhead(sp, steerDelta.x, steerDelta.z, d, 2)){
                    doJumpForward(sp, steerDelta, puppetFar);
                    tag.putInt(NBT_JUMP_CD, puppetFar ? 3 : 5);
                    jumped = true;
                    break;
                }
            }
        }

        // --- Fallback: step-boost ---
        if (!jumped){
            if (hasObstacleAheadWithHeadroom(sp, steerDelta.x, steerDelta.z,
                    puppetFar ? 1.2D : 0.9D, /*requireMoving=*/false)){
                boostStep(sp, puppetFar ? 12 : 8);
            }
        }

        // --- Stuck kurtarma ---
        if (!jumped){
            boolean forced = trackStuck(sp, true);
            if (forced){
                boolean okToJump = hasObstacleAheadWithHeadroom(sp, steerDelta.x, steerDelta.z, 0.6D, /*requireMoving=*/false)
                        && !isCliffAhead(sp, steerDelta.x, steerDelta.z, 0.6D, 2);
                if (okToJump){
                    doJumpForward(sp, steerDelta, puppetFar);
                    Vec3 fwd = new Vec3(steerDelta.x, 0, steerDelta.z);
                    if (fwd.lengthSqr() > 1e-6) {
                        fwd = fwd.normalize();
                        sp.setDeltaMovement(sp.getDeltaMovement().add(fwd.scale(puppetFar ? 0.14 : 0.10)));
                    }
                    tag.putInt(NBT_JUMP_CD, 6);
                }
            }
        }
    }

    // --- Yardımcılar ---

    // Node gerçekten "yukarı" istiyor mu? (engel tespiti şaşsa da no-progress için izin verelim)
    private static boolean nextNodeRequiresJump(ServerPlayer sp, boolean puppetFar){
        Node n = PossessionPathManager.nextNode(sp);
        if (n == null) return false;
        int footY = Mth.floor(sp.getY());
        if (n.y < footY + 1) return false;
        double nx = (n.x + 0.5) - sp.getX();
        double nz = (n.z + 0.5) - sp.getZ();
        double nodeDistSq = nx*nx + nz*nz;
        double nearThresh = puppetFar ? 1.44D : 1.00D; // no-progress'te biraz esnek ol
        return nodeDistSq < nearThresh;
    }

    // Obstacle/headroom check overloadları
    private static boolean hasObstacleAheadWithHeadroom(ServerPlayer sp, double dirX, double dirZ){
        return hasObstacleAheadWithHeadroom(sp, dirX, dirZ, 0.6D, true);
    }
    private static boolean hasObstacleAheadWithHeadroom(ServerPlayer sp, double dirX, double dirZ, double aheadDist){
        return hasObstacleAheadWithHeadroom(sp, dirX, dirZ, aheadDist, true);
    }
    private static boolean hasObstacleAheadWithHeadroom(ServerPlayer sp, double dirX, double dirZ, double aheadDist, boolean requireMoving){
        Level lvl = sp.level();
        Vec3 forward = new Vec3(dirX, 0, dirZ);
        if (forward.lengthSqr() < 1.0E-6D) {
            Vec3 look = sp.getLookAngle();
            forward = new Vec3(look.x, 0, look.z);
        }
        if (forward.lengthSqr() < 1.0E-6D) return false;
        forward = forward.normalize();

        Vec3 ahead = sp.position().add(forward.scale(aheadDist));
        BlockPos base = BlockPos.containing(ahead.x, sp.getY(), ahead.z);

        BlockState bsBase = lvl.getBlockState(base);
        boolean obstacle = !bsBase.getCollisionShape(lvl, base).isEmpty();

        BlockPos above1 = base.above();
        BlockPos above2 = above1.above();
        boolean headroom =
                lvl.getBlockState(above1).getCollisionShape(lvl, above1).isEmpty() &&
                        lvl.getBlockState(above2).getCollisionShape(lvl, above2).isEmpty();

        if (requireMoving){
            Vec3 vel = sp.getDeltaMovement();
            boolean moving = (vel.x*vel.x + vel.z*vel.z) > 0.0025D;
            if (!moving) return false;
        }
        return obstacle && headroom;
    }

    // Cliff guard
    private static boolean isCliffAhead(ServerPlayer sp, double dirX, double dirZ, double aheadDist, int dropDepth){
        Level lvl = sp.level();
        Vec3 forward = new Vec3(dirX, 0, dirZ);
        if (forward.lengthSqr() < 1.0E-6D) {
            Vec3 look = sp.getLookAngle();
            forward = new Vec3(look.x, 0, look.z);
        }
        if (forward.lengthSqr() < 1.0E-6D) return false;
        forward = forward.normalize();

        Vec3 ahead = sp.position().add(forward.scale(aheadDist));
        BlockPos feetAhead = BlockPos.containing(ahead.x, Math.floor(sp.getY()), ahead.z);

        for (int i = 1; i <= dropDepth; i++){
            BlockPos below = feetAhead.below(i);
            BlockState bs = lvl.getBlockState(below);
            if (!bs.getCollisionShape(lvl, below).isEmpty()){
                return false;
            }
        }
        return true;
    }

    // Stuck takibi
    private static boolean trackStuck(ServerPlayer sp, boolean count){
        var tag = sp.getPersistentData();
        BlockPos cur = sp.blockPosition();
        int lbx = tag.getInt(NBT_LAST_BX);
        int lbz = tag.getInt(NBT_LAST_BZ);

        if (!count){
            tag.putInt(NBT_STUCK_T, 0);
            tag.putInt(NBT_LAST_BX, cur.getX());
            tag.putInt(NBT_LAST_BZ, cur.getZ());
            return false;
        }

        if (cur.getX() == lbx && cur.getZ() == lbz){
            int t = tag.getInt(NBT_STUCK_T) + 1;
            tag.putInt(NBT_STUCK_T, t);
            if (t >= 8) {
                tag.putInt(NBT_STUCK_T, 0);
                return true;
            }
        } else {
            tag.putInt(NBT_STUCK_T, 0);
            tag.putInt(NBT_LAST_BX, cur.getX());
            tag.putInt(NBT_LAST_BZ, cur.getZ());
        }
        return false;
    }

    // hız düşükken bir tık ileri dürtüp zıplamayı "armala"
    private static boolean armJumpIfTooSlow(ServerPlayer sp, double dirX, double dirZ, boolean far){
        Vec3 vel = sp.getDeltaMovement();
        double speedSq = vel.x*vel.x + vel.z*vel.z;
        double minSq = 0.0100D;

        if (speedSq < minSq){
            Vec3 fwd = new Vec3(dirX, 0, dirZ);
            if (fwd.lengthSqr() < 1e-6){
                Vec3 look = sp.getLookAngle();
                fwd = new Vec3(look.x, 0, look.z);
            }
            if (fwd.lengthSqr() < 1e-6) return false;
            fwd = fwd.normalize();
            double impulse = far ? 0.20 : 0.14;
            sp.setDeltaMovement(sp.getDeltaMovement().add(fwd.scale(impulse)));
            sp.getPersistentData().putInt(NBT_JUMP_ARM, 2);
            return true;
        }
        return false;
    }

    // Normal zıplama (min Y hız garantisi) + kısa soft-fall
    private static void doJump(ServerPlayer sp){
        sp.jumpFromGround();
        Vec3 v = sp.getDeltaMovement();
        if (v.y < 0.42D) sp.setDeltaMovement(v.x, 0.42D, v.z);
        sp.hurtMarked = true;
        sp.getPersistentData().putInt(NBT_SOFTFALL_T, Math.max( sp.getPersistentData().getInt(NBT_SOFTFALL_T), 8 ));
    }

    // Zıplarken ileri dürtü + soft-fall
    private static void doJumpForward(ServerPlayer sp, Vec3 forwardHint, boolean far){
        Vec3 f = new Vec3(forwardHint.x, 0, forwardHint.z);
        if (f.lengthSqr() < 1e-6){
            Vec3 look = sp.getLookAngle();
            f = new Vec3(look.x, 0, look.z);
        }
        if (f.lengthSqr() > 1e-6){
            f = f.normalize();
            sp.setDeltaMovement(sp.getDeltaMovement().add(f.scale(far ? 0.16 : 0.12)));
        }
        doJump(sp);
        sp.getPersistentData().putInt(NBT_SOFTFALL_T, Math.max( sp.getPersistentData().getInt(NBT_SOFTFALL_T), far ? 16 : 12 ));
    }

    // Zemin "false" dese de doğal görünen zorunlu hop (no-progress için)
    private static void doForcedHopForward(ServerPlayer sp, Vec3 forwardHint, boolean far){
        Vec3 f = new Vec3(forwardHint.x, 0, forwardHint.z);
        if (f.lengthSqr() < 1e-6){
            Vec3 look = sp.getLookAngle();
            f = new Vec3(look.x, 0, look.z);
        }
        if (f.lengthSqr() > 1e-6){
            f = f.normalize();
            sp.setDeltaMovement(sp.getDeltaMovement().add(f.scale(far ? 0.16 : 0.12)));
        }
        // jumpFromGround kullanamıyorsak dikey ivmeyi biz verelim
        Vec3 v = sp.getDeltaMovement();
        double up = Math.max(v.y, 0.0D) + 0.42D;
        sp.setDeltaMovement(v.x, up, v.z);
        sp.hurtMarked = true;
        sp.getPersistentData().putInt(NBT_SOFTFALL_T, Math.max( sp.getPersistentData().getInt(NBT_SOFTFALL_T), far ? 16 : 12 ));
    }

    private static float lerpAngle(float a, float b, float t){
        float d = (float)(((b - a + 540f) % 360f) - 180f);
        return a + d * t;
    }
}
