package net.eris.reverie.entity;

import net.eris.reverie.entity.goal.GoblinBruteTargetPlayerGoal;
import net.eris.reverie.entity.goal.GoblinBruteWanderGoal;
import net.eris.reverie.init.ReverieModSounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.network.NetworkHooks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
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

import net.eris.reverie.init.ReverieModEntities;
import net.eris.reverie.entity.ShooterGoblinEntity;
import net.eris.reverie.entity.goal.GrabPlayerGoal;
import net.eris.reverie.entity.goal.ShooterMountGoal;
import net.eris.reverie.util.GoblinReputation;

import java.util.List;

import static net.eris.reverie.entity.goal.GrabPlayerGoal.SEARCH_RADIUS;

public class GoblinBruteEntity extends Monster {
	public enum BruteState {
		IDLE, SEEK_PLAYER, CARRY_PLAYER, SEEK_SHOOTER, CARRY_SHOOTER,
		CRYING, ROARING, CHARGING, ANGRY
	}

	// --- Yeni eklenen: grab arasına koyma soğutması
	public boolean thrownOnce = false; // <--- yeni
	public int grabCooldown = 0;

	private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(
			GoblinBruteEntity.class, EntityDataSerializers.INT
	);

	private int attackTimer = 0;
	private static final int FIRE_CHECK_RADIUS = 32;
	private int stateTimer = 0;

	// Orijinal animasyon state objeleri
	public final AnimationState animationState0 = new AnimationState();
	public final AnimationState animationState2 = new AnimationState();
	public final AnimationState animationState3 = new AnimationState();
	public final AnimationState animationState5 = new AnimationState();
	public final AnimationState animationState6 = new AnimationState();

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


// 0. slot’taki GrabPlayerGoal’u ekleyelim:


		this.targetSelector.addGoal(1, new GoblinBruteTargetPlayerGoal(this));    // → hedefine kilitlen
		this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
		this.goalSelector.addGoal(1, new GrabPlayerGoal(this) {});
	    this.goalSelector.addGoal(3, new GoblinBruteWanderGoal(this, 1.3) {});
		this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F)); // → baktığını koru
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
		// Volume ve pitch kendi zevkine göre ayarla
		this.playSound(SoundEvents.RAVAGER_STEP, 0.15F, 1.0F);
	}
	@Override
	public SoundEvent getDeathSound() {
		return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.death"));
	}

	@Override
	public void tick() {
		super.tick();

		// grabCooldown geri sayımı
		if (grabCooldown > 0) {
			grabCooldown--;
		}

		// --- CLIENT: animasyon update ---
		if (level().isClientSide) {
			if (attackTimer > 0) {
				attackTimer--;
				if (attackTimer == 0) {
					animationState5.stop();
				}
			}
			animationState0.animateWhen(true, tickCount);
			animationState2.animateWhen(getState() == BruteState.CRYING, tickCount);
			animationState3.animateWhen(getState() == BruteState.ROARING, tickCount);
			animationState6.animateWhen(getState() == BruteState.CARRY_PLAYER, tickCount);
		}

		// --- SERVER: state machine işlemleri ---
		if (!level().isClientSide) {
			stateTimer++;
			switch (getState()) {
				case CRYING:
					if (stateTimer > 60) {
						setState(BruteState.ROARING);
						// ← Buraya ekle:
						this.playSound(ReverieModSounds.GOBLIN_BRUTE_ROAR.get(), 1.8F, 0.7F);
					}
					break;
				case ROARING:
					if (stateTimer > 40) setState(BruteState.CHARGING);
					break;
				default:
					break;
			}




			// --------------- SEEK_PLAYER geçişi ---------------
			if (getState() == BruteState.IDLE
					&& grabCooldown == 0
					&& !thrownOnce) {

				// 0) Önce 32 blok içinde ateş var mı?
				BlockPos nearbyFire = findNearestFire(level(), blockPosition(), FIRE_CHECK_RADIUS);
				if (nearbyFire == null) {
					// Ateş yok, SEEK_PLAYER’a geçme
				} else {
					// 1) Ateş bulunduysa oyuncu ara
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
					// 2) Bulduysan SEEK_PLAYER state’ine geç
					if (!players.isEmpty()) {
						setState(BruteState.SEEK_PLAYER);
					}
				}
			}

			// CARRY_PLAYER davranışı (ateşe atma / klasik atma)
			if (getState() == BruteState.CARRY_PLAYER && !getPassengers().isEmpty()) {
				BlockPos firePos = findNearestFire(level(), blockPosition(), 20);
				if (firePos != null) {
					getNavigation().moveTo(firePos.getX(), firePos.getY(), firePos.getZ(), 1.2);
					if (position().distanceTo(Vec3.atCenterOf(firePos)) < 2.5) {
						// indirme ve tp / stop
						for (Entity passenger : List.copyOf(getPassengers())) {
							if (passenger instanceof Player player) {
								player.stopRiding();
								player.teleportTo(
										firePos.getX() + 0.5,
										firePos.getY(),
										firePos.getZ() + 0.5
								);
								player.setDeltaMovement(Vec3.ZERO);
							}
						}
						thrownOnce = true;
						grabCooldown = 40;
						setState(BruteState.IDLE);
						return;
					}
				} else {
					// klasik fırlatma
					for (Entity passenger : getPassengers()) {
						if (passenger instanceof Player) {
							passenger.stopRiding();
							passenger.setDeltaMovement(
									getLookAngle().scale(7.5D).add(0, 0.3, 0)
							);
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
					if (distanceTo(victim) <= reach) {
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
				if (tgt == null || !tgt.isAlive()) {
					setState(BruteState.IDLE);
				}
			}

			// SHOOTER taşıma & ağlama
			boolean hasShooter = getPassengers().stream()
					.anyMatch(e -> e instanceof ShooterGoblinEntity);
			if (hasShooter && getState() != BruteState.CARRY_SHOOTER) {
				setState(BruteState.CARRY_SHOOTER);
			}
			if (!hasShooter && getState() == BruteState.CARRY_SHOOTER) {
				setState(BruteState.CRYING);

			}
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
		boolean hasShooter = getPassengers().stream()
				.anyMatch(p -> p instanceof ShooterGoblinEntity);
		boolean hasPlayer  = getPassengers().stream()
				.anyMatch(p -> p instanceof Player);
		if (passenger instanceof ShooterGoblinEntity)
			return getState() == BruteState.SEEK_SHOOTER && !hasShooter && !hasPlayer;
		if (passenger instanceof Player)
			return getState() == BruteState.SEEK_PLAYER && !hasPlayer && !hasShooter;
		return super.canAddPassenger(passenger);
	}

	@Override
	public void removePassenger(Entity passenger) {
		try {
			super.removePassenger(passenger);
		} catch (IllegalStateException ignored) {}
		if (passenger instanceof ShooterGoblinEntity
				&& getState() == BruteState.CARRY_SHOOTER) {
			LivingEntity killer = ((ShooterGoblinEntity) passenger).getLastHurtByMob();
			if (killer != null) setTarget(killer);
			setState(BruteState.CRYING);
		}
	}

	@Override
	public boolean doHurtTarget(Entity targetEntity) {
		boolean result = super.doHurtTarget(targetEntity);
		if (result && !level().isClientSide) {
			level().broadcastEntityEvent(this, (byte)4);
		}
		return result;
	}

	@Override
	public void handleEntityEvent(byte id) {
		super.handleEntityEvent(id);
		if (id == 4) {
			animationState5.animateWhen(true, tickCount);
			attackTimer = (int)(1.2083 * 20);
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

	private BlockPos findNearestFire(Level level, BlockPos start, int radius) {
		BlockPos nearest = null;
		double closest = Double.MAX_VALUE;
		for (BlockPos pos : BlockPos.betweenClosed(
				start.offset(-radius, -2, -radius),
				start.offset(radius,  2,  radius))
		) {
			if (level.getBlockState(pos).getBlock() == Blocks.FIRE) {
				double dist = pos.distSqr(start);
				if (dist < closest) {
					closest = dist;
					nearest = pos.immutable();
				}
			}
		}
		return nearest;
	}

	@Override
	public void positionRider(Entity passenger, MoveFunction moveFunction) {
		if (passenger instanceof LivingEntity
				&& this.getState() == BruteState.CARRY_PLAYER) {
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
