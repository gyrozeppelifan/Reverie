// ... package ve importlar (aynen)
package net.eris.reverie.entity;

import net.eris.reverie.entity.goal.*;
import net.eris.reverie.fx.ScreenShake;
import net.eris.reverie.init.ReverieModSounds;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.Difficulty;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;
import net.eris.reverie.config.ReverieCommonConfig;

import net.eris.reverie.init.ReverieModEntities;
import net.eris.reverie.entity.ShooterGoblinEntity;
import net.eris.reverie.util.GoblinReputation;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import static net.eris.reverie.entity.goal.GrabPlayerGoal.SEARCH_RADIUS;

// NEW: LOS import
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;

public class GoblinBruteEntity extends Monster {
	public enum BruteState {
		IDLE, SEEK_PLAYER, CARRY_PLAYER, SEEK_SHOOTER, CARRY_SHOOTER,
		CRYING, ROARING, CHARGING, ANGRY
	}

	public boolean thrownOnce = false; // artık kullanılmıyor ama kalsın
	public int grabCooldown = 0;

	private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(
			GoblinBruteEntity.class, EntityDataSerializers.INT
	);

	private long getFireThrowCooldownTicks() {
		try { return ReverieCommonConfig.GOBLIN_BRUTE_FIRE_THROW_COOLDOWN_TICKS.get(); }
		catch (Throwable t) { return 800L; } // güvenli fallback
	}

	private int attackTimer = 0;
	private static final int FIRE_CHECK_RADIUS = 32;
	private int stateTimer = 0;

	// ---------- Fire scan tuning ----------
	private static final int FIRE_SCAN_COOLDOWN_IDLE  = 40;
	private static final int FIRE_SCAN_COOLDOWN_CARRY = 5;
	private static final int FIRE_CACHE_TTL           = 60; // ticks
	private static final int FIRE_Y_RANGE             = 1;  // +/- Y
	private static final int MAX_FIRE_PATHCHECKS_PER_SCAN = 8;
	private static final double EARLY_ACCEPT_DIST_SQR = 8 * 8;

	// NEW: Fire-throw cooldown (40s)
	private static final long FIRE_THROW_COOLDOWN = 40L * 20L; // 800 tick
	private long nextFireThrowAllowed = 0L;

	// Throttle ve cache için
	private int fireScanCooldown = 0;
	private int fireCacheAge = 0;
	private FireTarget cachedFireTarget = null;

	// Yeni: pending throw sistemi
	private static class ThrowData {
		public final Player player;
		public final Vec3 dest;
		public int ticksLeft;
		public ThrowData(Player player, Vec3 dest, int ticks) {
			this.player = player;
			this.dest = dest;
			this.ticksLeft = ticks;
		}
	}
	private final List<ThrowData> pendingThrows = new ArrayList<>();

	// Animasyon state objeleri
	public final AnimationState animationState0 = new AnimationState();
	public final AnimationState animationState2 = new AnimationState();
	public final AnimationState animationState3 = new AnimationState();
	public final AnimationState animationState5 = new AnimationState();
	public final AnimationState animationState6 = new AnimationState();

	// Carry-state housekeeping
	private int carryTicks = 0;
	private int navRepathCooldown = 0;
	private double lastDistToStand = Double.MAX_VALUE;
	private int noProgressTicks = 0;

	public GoblinBruteEntity(PlayMessages.SpawnEntity packet, Level world) {
		this(ReverieModEntities.GOBLIN_BRUTE.get(), world);
	}

	public GoblinBruteEntity(EntityType<GoblinBruteEntity> type, Level world) {
		super(type, world);
		setMaxUpStep(1.0f);
		xpReward = 20;
		setNoAi(false);
		this.lookControl = new LookControl(this);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(STATE, BruteState.IDLE.ordinal());
	}

	public BruteState getState() {
		return BruteState.values()[this.entityData.get(STATE)];
	}

	public void setState(BruteState newState) {
		this.entityData.set(STATE, newState.ordinal());
		this.stateTimer = 0;

		// carry housekeeping reset
		if (newState != BruteState.CARRY_PLAYER) {
			carryTicks = 0;
			navRepathCooldown = 0;
			noProgressTicks = 0;
			lastDistToStand = Double.MAX_VALUE;
		}
	}

	public int getAttackTimer() {
		return this.attackTimer;
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	protected void registerGoals() {
		this.targetSelector.addGoal(1, new GoblinBruteTargetPlayerGoal(this));
		this.targetSelector.addGoal(2, new GoblinHurtByTargetGoal(this));
		this.goalSelector.addGoal(0, new ShooterMountGoal(this) {});
		this.goalSelector.addGoal(1, new GrabPlayerGoal(this) {});
		this.goalSelector.addGoal(2, new ConditionalBruteMeleeAttackGoal(this, 1.2D, true));
		this.goalSelector.addGoal(4, new GoblinBruteWanderGoal(this, 1.3) {});
		this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new FloatGoal(this));
	}

	@Override
	public MobType getMobType() {
		return MobType.UNDEFINED;
	}

	@Override
	public SoundEvent getHurtSound(DamageSource ds) {
		return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.hurt"));
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		this.playSound(SoundEvents.RAVAGER_STEP, 0.15F, 1.0F);
	}

	@Override
	public SoundEvent getDeathSound() {
		return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.death"));
	}

	// PUBLIC: goal için cooldown sorgusu
	public boolean isFireThrowOffCooldown() {
		return level().getGameTime() >= nextFireThrowAllowed;
	}

	@Override
	public void tick() {
		super.tick();

		// pendingThrows
		Iterator<ThrowData> it = pendingThrows.iterator();
		while (it.hasNext()) {
			ThrowData d = it.next();
			if (d.ticksLeft > 0) {
				Vec3 curr = d.player.position();
				Vec3 step = d.dest.subtract(curr).scale(1.0 / d.ticksLeft);
				d.player.setPos(curr.x + step.x, curr.y + step.y, curr.z + step.z);
				d.player.fallDistance = 0.0F;
				d.ticksLeft--;
			} else {
				d.player.teleportTo(d.dest.x, d.dest.y, d.dest.z);
				d.player.fallDistance = 0.0F;
				it.remove();
			}
		}

		if (grabCooldown > 0) grabCooldown--;

		// CLIENT anim
		if (level().isClientSide) {
			if (attackTimer > 0) {
				attackTimer--;
				if (attackTimer == 0) animationState5.stop();
			}
			animationState0.animateWhen(true, tickCount);
			animationState2.animateWhen(getState() == BruteState.CRYING, tickCount);
			animationState3.animateWhen(getState() == BruteState.ROARING, tickCount);
			animationState6.animateWhen(getState() == BruteState.CARRY_PLAYER, tickCount);
		}

		// SERVER
		if (!level().isClientSide) {
			if (getState() == BruteState.CARRY_PLAYER
					&& getPassengers().stream().noneMatch(e -> e instanceof Player)) {
				setState(BruteState.IDLE);
			}
			if (getState() == BruteState.CARRY_SHOOTER
					&& getPassengers().stream().noneMatch(e -> e instanceof ShooterGoblinEntity)) {
				setState(BruteState.IDLE);
			}

			stateTimer++;
			switch (getState()) {
				case CRYING:
					if (stateTimer > 60) {
						setState(BruteState.ROARING);
						this.playSound(ReverieModSounds.GOBLIN_BRUTE_ROAR.get(), 2.0F, 0.7F);

						// YENİ: yakın oyunculara 40 tick, 1.25f şiddetinde ekran sarsıntısı
						if (level() instanceof ServerLevel s) {
							ScreenShake.broadcast(s, this.position(), 20.0, 60, 1.0f);
						}
					}
					break;
				case ROARING:
					if (stateTimer > 40) setState(BruteState.CHARGING);
					break;
				default:
					break;
			}

			// SEEK_SHOOTER ön kontrol
			if (getState() == BruteState.IDLE) {
				List<ShooterGoblinEntity> shooters = level().getEntitiesOfClass(
						ShooterGoblinEntity.class,
						getBoundingBox().inflate(10.0, 5.0, 10.0)
				);
				if (!shooters.isEmpty()) setState(BruteState.SEEK_SHOOTER);
			}

			// SEEK_PLAYER'e geçiş: cooldown + ateş varlığı
			if (getState() == BruteState.IDLE && grabCooldown == 0 && isFireThrowOffCooldown()) {
				FireTarget fireTarget = getCachedFireTarget();
				if (fireTarget != null) {
					List<Player> players = level().getEntitiesOfClass(
							Player.class,
							getBoundingBox().inflate(SEARCH_RADIUS, 3, SEARCH_RADIUS),
							p -> {
								if (p.isCreative() || p.isSpectator()) return false;
								var rep = GoblinReputation.getState(p);
								if (rep == GoblinReputation.State.AGGRESSIVE) return true;
								return rep == GoblinReputation.State.NEUTRAL
										&& getLastHurtByMob() == p;
							}
					);
					if (!players.isEmpty()) setState(BruteState.SEEK_PLAYER);
				}
			}

			// CARRY_PLAYER
			if (getState() == BruteState.CARRY_PLAYER && !getPassengers().isEmpty()) {
				if (carryTicks == 0) {
					navRepathCooldown = 0;
					noProgressTicks = 0;
					lastDistToStand = Double.MAX_VALUE;
				}
				carryTicks++;

				FireTarget target = getCachedFireTarget();

				// ateş söndüyse hızlı reset
				if (target != null && !isFireStillValid(target.firePos)) {
					cachedFireTarget = null;
					fireCacheAge = FIRE_CACHE_TTL + 1;
					fireScanCooldown = 0;
					target = null;
				}

				if (target != null) {
					double distToStand = this.position().distanceTo(Vec3.atCenterOf(target.standPos));
					boolean canSeeFire = hasLineOfSightTo(target.firePos);

					boolean closeEnough = distToStand <= 2.25;
					boolean closeWithDonePath = getNavigation().isDone() && distToStand <= 4.0;
					boolean forcedThrow = (carryTicks > 120 && canSeeFire && distToStand <= 6.5);

					if (closeEnough || closeWithDonePath || forcedThrow) {
						Vec3 dest = computeThrowDestFor(target.firePos);
						for (Entity passenger : List.copyOf(getPassengers())) {
							if (passenger instanceof Player player) {
								player.stopRiding();
								player.fallDistance = 0.0F;
								pendingThrows.add(new ThrowData(player, dest, 5));
							}
						}
						// NEW: cooldown başlat
						nextFireThrowAllowed = level().getGameTime() + getFireThrowCooldownTicks();

						grabCooldown = 40;
						setState(BruteState.IDLE);
						return;
					}

					// Repath throttling
					if (navRepathCooldown > 0) {
						navRepathCooldown--;
					} else {
						getNavigation().moveTo(
								target.standPos.getX(),
								target.standPos.getY(),
								target.standPos.getZ(),
								1.2
						);
						navRepathCooldown = 10;
					}

					// stuck tespiti
					if (distToStand + 0.05 >= lastDistToStand) noProgressTicks++;
					else noProgressTicks = 0;
					lastDistToStand = distToStand;

					if (noProgressTicks > 80 || carryTicks > 240) {
						for (Entity passenger : getPassengers()) {
							if (passenger instanceof Player p) {
								p.stopRiding();
								p.fallDistance = 0.0F;
								p.setDeltaMovement(getLookAngle().scale(7.5D).add(0, 0.3, 0));
							}
						}
						getNavigation().stop();
						setState(BruteState.IDLE);
					}
				} else {
					for (Entity passenger : getPassengers()) {
						if (passenger instanceof Player p) {
							p.stopRiding();
							p.fallDistance = 0.0F;
							p.setDeltaMovement(getLookAngle().scale(7.5D).add(0, 0.3, 0));
						}
					}
					getNavigation().stop();
					setState(BruteState.IDLE);
				}
			}

			// CHARGING -> saldırı
			if (getState() == BruteState.CHARGING) {
				LivingEntity victim = getTarget();
				if (victim != null && victim.isAlive()) {
					getNavigation().moveTo(victim, 2.0);
					double reach = this.getBbWidth() + victim.getBbWidth();
					if (position().distanceTo(victim.position()) <= reach) {
						swing(InteractionHand.MAIN_HAND);
						victim.hurt(damageSources().mobAttack(this), 20.0F);
						float dx = (float)(getX() - victim.getX());
						float dz = (float)(getZ() - victim.getZ());
						victim.knockback(4.0F, dx, dz);
						setState(BruteState.ANGRY);
					}
				}
			}

			// ANGRY -> IDLE
			if (getState() == BruteState.ANGRY) {
				LivingEntity tgt = getTarget();
				if (tgt == null || !tgt.isAlive()) setState(BruteState.IDLE);
			}

			// SHOOTER carry transition + sfx/particles (aynen)
			boolean hasShooter = getPassengers().stream()
					.anyMatch(e -> e instanceof ShooterGoblinEntity);
			if (hasShooter && getState() != BruteState.CARRY_SHOOTER) {
				setState(BruteState.CARRY_SHOOTER);

				ServerLevel serverLevel = (ServerLevel) level();
				ClientboundSoundPacket soundPkt = new ClientboundSoundPacket(
						Holder.direct(ReverieModSounds.SHOOTER_BRUTE_TEAMUP.get()),
						SoundSource.PLAYERS,
						this.getX(), this.getY(), this.getZ(),
						1.2F, 1.0F,
						serverLevel.getRandom().nextLong()
				);
				for (ServerPlayer sp : serverLevel.getServer().getPlayerList().getPlayers()) {
					sp.connection.send(soundPkt);
				}

				ClientboundLevelParticlesPacket partPkt = new ClientboundLevelParticlesPacket(
						ParticleTypes.CAMPFIRE_COSY_SMOKE,
						true,
						(float)this.getX(), (float)(this.getY()+1.2), (float)this.getZ(),
						0.6f, 0.6f, 0.6f,
						0.05f,
						30
				);
				for (ServerPlayer sp : serverLevel.getServer().getPlayerList().getPlayers()) {
					sp.connection.send(partPkt);
				}
			}

			if (!hasShooter && getState() == BruteState.CARRY_SHOOTER) setState(BruteState.CRYING);
			if (getState() == BruteState.CRYING || getState() == BruteState.ROARING) {
				setDeltaMovement(0, getDeltaMovement().y, 0);
				getNavigation().stop();
				setYRot(yRotO);
				setXRot(0);
			}
		}
	}

	@Override
	public void aiStep() {
		super.aiStep();
		LivingEntity tgt = getTarget();
		if (tgt != null && tgt.isAlive()) {
			this.lookControl.setLookAt(tgt, 30.0F, 30.0F);
		}
	}

	@Override
	public boolean canAddPassenger(Entity passenger) {
		boolean hasShooter = getPassengers().stream().anyMatch(p -> p instanceof ShooterGoblinEntity);
		boolean hasPlayer  = getPassengers().stream().anyMatch(p -> p instanceof Player);
		if (passenger instanceof ShooterGoblinEntity)
			return getState() == BruteState.SEEK_SHOOTER && !hasShooter && !hasPlayer;
		if (passenger instanceof Player)
			return getState() == BruteState.SEEK_PLAYER && !hasPlayer && !hasShooter;
		return super.canAddPassenger(passenger);
	}

	@Override
	public void removePassenger(Entity passenger) {
		try { super.removePassenger(passenger); } catch (IllegalStateException ignored) {}
		if (passenger instanceof ShooterGoblinEntity && getState() == BruteState.CARRY_SHOOTER) {
			LivingEntity killer = ((ShooterGoblinEntity) passenger).getLastHurtByMob();
			if (killer != null) setTarget(killer);
			setState(BruteState.CRYING);
		}
	}

	@Override
	public boolean doHurtTarget(Entity targetEntity) {
		boolean result = super.doHurtTarget(targetEntity);
		if (result && !level().isClientSide) level().broadcastEntityEvent(this, (byte)4);
		return result;
	}

	@Override
	public void handleEntityEvent(byte id) {
		super.handleEntityEvent(id);
		if (id == 4) {
			animationState5.animateWhen(true, tickCount);
			attackTimer = (int)(0.700 * 20);
		}
	}

	public static void init() {
		SpawnPlacements.register(
				ReverieModEntities.GOBLIN_BRUTE.get(),
				SpawnPlacements.Type.ON_GROUND,
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				(type, world, reason, pos, rand) ->
						world.getDifficulty() != Difficulty.PEACEFUL &&
								Monster.isDarkEnoughToSpawn(world, pos, rand) &&
								Mob.checkMobSpawnRules(type, world, reason, pos, rand)
		);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MOVEMENT_SPEED, 0.3)
				.add(Attributes.MAX_HEALTH,     80.0)
				.add(Attributes.ARMOR,          1.0)
				.add(Attributes.ATTACK_DAMAGE,  6.0)
				.add(Attributes.FOLLOW_RANGE,   64.0);
	}

	// FireTarget ve cached scan metodları
	private static class FireTarget {
		public final BlockPos firePos;
		public final BlockPos standPos;
		public FireTarget(BlockPos firePos, BlockPos standPos) {
			this.firePos = firePos;
			this.standPos = standPos;
		}
	}

	public FireTarget getCachedFireTarget() {
		if (fireScanCooldown > 0) {
			fireScanCooldown--;
		} else {
			boolean invalid = (cachedFireTarget == null)
					|| (fireCacheAge > FIRE_CACHE_TTL)
					|| !isFireStillValid(cachedFireTarget.firePos);

			if (invalid) {
				cachedFireTarget = findNearestReachableFireTarget(FIRE_CHECK_RADIUS);
				fireCacheAge = 0;
			} else {
				fireCacheAge++;
			}

			fireScanCooldown = (getState() == BruteState.CARRY_PLAYER)
					? FIRE_SCAN_COOLDOWN_CARRY
					: FIRE_SCAN_COOLDOWN_IDLE;
		}
		return cachedFireTarget;
	}

	private boolean isFireStillValid(BlockPos pos) {
		return pos != null && level().getBlockState(pos).is(Blocks.FIRE);
	}

	public FireTarget findNearestReachableFireTarget(int radius) {
		final BlockPos start = blockPosition();
		FireTarget best = null;

		int[] pathChecksRef = new int[]{0};

		for (int r = 1; r <= radius; r++) {
			for (int dy = -FIRE_Y_RANGE; dy <= FIRE_Y_RANGE; dy++) {
				int y = start.getY() + dy;

				// üst/alt kenarlar
				for (int x = start.getX() - r; x <= start.getX() + r; x++) {
					if (pathChecksRef[0] >= MAX_FIRE_PATHCHECKS_PER_SCAN) return best;
					best = tryFireAt(new BlockPos(x, y, start.getZ() - r), start, best, pathChecksRef);
					if (best != null && start.distSqr(best.standPos) <= EARLY_ACCEPT_DIST_SQR) return best;

					if (pathChecksRef[0] >= MAX_FIRE_PATHCHECKS_PER_SCAN) return best;
					best = tryFireAt(new BlockPos(x, y, start.getZ() + r), start, best, pathChecksRef);
					if (best != null && start.distSqr(best.standPos) <= EARLY_ACCEPT_DIST_SQR) return best;
				}

				// sol/sağ kenarlar
				for (int z = start.getZ() - r + 1; z <= start.getZ() + r - 1; z++) {
					if (pathChecksRef[0] >= MAX_FIRE_PATHCHECKS_PER_SCAN) return best;
					best = tryFireAt(new BlockPos(start.getX() - r, y, z), start, best, pathChecksRef);
					if (best != null && start.distSqr(best.standPos) <= EARLY_ACCEPT_DIST_SQR) return best;

					if (pathChecksRef[0] >= MAX_FIRE_PATHCHECKS_PER_SCAN) return best;
					best = tryFireAt(new BlockPos(start.getX() + r, y, z), start, best, pathChecksRef);
					if (best != null && start.distSqr(best.standPos) <= EARLY_ACCEPT_DIST_SQR) return best;
				}
			}
		}
		return best;
	}

	private FireTarget tryFireAt(BlockPos fire, BlockPos start, FireTarget currentBest, int[] pathChecksRef) {
		if (!level().getBlockState(fire).is(Blocks.FIRE)) return currentBest;

		for (Direction d : Direction.Plane.HORIZONTAL) {
			BlockPos stand = fire.relative(d);

			if (!level().getBlockState(stand).getCollisionShape(level(), stand).isEmpty()) continue;
			BlockPos head = stand.above();
			if (!level().getBlockState(head).getCollisionShape(level(), head).isEmpty()) continue;

			if (pathChecksRef[0] >= MAX_FIRE_PATHCHECKS_PER_SCAN) return currentBest;
			pathChecksRef[0]++;

			Path path = this.getNavigation().createPath(stand, 0);
			if (path != null && path.canReach()) {
				double dist = stand.distSqr(start);
				if (currentBest == null || dist < start.distSqr(currentBest.standPos)) {
					return new FireTarget(fire.immutable(), stand.immutable());
				}
			}
		}
		return currentBest;
	}

	// LOS helper
	private boolean hasLineOfSightTo(BlockPos pos) {
		var from = this.getEyePosition();
		var to = Vec3.atCenterOf(pos).add(0, 0.5, 0);
		var hit = level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
		return hit.getType() == HitResult.Type.MISS;
	}

	// güvenli atış noktası
	private Vec3 computeThrowDestFor(BlockPos firePos) {
		for (int i = 1; i <= 2; i++) {
			BlockPos check = firePos.above(i);
			if (level().getBlockState(check).getCollisionShape(level(), check).isEmpty()) {
				return Vec3.atCenterOf(check);
			}
		}
		return Vec3.atCenterOf(firePos.above());
	}

	@Override
	public void positionRider(Entity passenger, MoveFunction moveFunction) {
		if (passenger instanceof LivingEntity && this.getState() == BruteState.CARRY_PLAYER) {
			double offsetX = 0.0, offsetY = 1.1, offsetZ = 1.5;
			float yawDeg = this.getYRot();
			double yawRad = Math.toRadians(yawDeg);
			double dx = offsetX * Math.cos(yawRad) - offsetZ * Math.sin(yawRad);
			double dz = offsetX * Math.sin(yawRad) + offsetZ * Math.cos(yawRad);

			moveFunction.accept(
					passenger,
					this.getX() + dx,
					this.getY() + offsetY,
					this.getZ() + dz
			);

			passenger.setYRot(yawDeg);
			if (passenger instanceof LivingEntity living) {
				living.setYBodyRot(yawDeg);
				living.setYHeadRot(yawDeg);
				living.yRotO     = yawDeg;
				living.yHeadRotO = yawDeg;
			}
		} else {
			super.positionRider(passenger, moveFunction);
		}
	}
}
