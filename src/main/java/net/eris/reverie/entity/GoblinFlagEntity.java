package net.eris.reverie.entity;

import net.eris.reverie.entity.GoblinBarrelEntity;
import net.eris.reverie.init.ReverieModEntities;
import net.eris.reverie.init.ReverieModSounds;
import net.eris.reverie.util.GoblinReputation;
import net.eris.reverie.util.GoblinReputation.State;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;

import java.util.UUID;

public class GoblinFlagEntity extends Entity {

    public final AnimationState animationState1 = new AnimationState();
    public final AnimationState animationState2 = new AnimationState();

    private static final EntityDataAccessor<String> DATA_OWNER_NAME =
            SynchedEntityData.defineId(GoblinFlagEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_DYING =
            SynchedEntityData.defineId(GoblinFlagEntity.class, EntityDataSerializers.BOOLEAN);
    // YENİ: Parlama Sayacı
    private static final EntityDataAccessor<Integer> DATA_GLOW_TICKS =
            SynchedEntityData.defineId(GoblinFlagEntity.class, EntityDataSerializers.INT);

    private static final int MAX_LIFE_TICKS = 45 * 20;
    private static final int MAX_USES = 3;
    private static final int SUMMON_COOLDOWN_TICKS = 10 * 20;

    private int hp = 6;
    private int lifeTicks = 0;
    private int usesLeft = MAX_USES;
    private int summonCooldown = 0;
    private UUID ownerUuid;

    public GoblinFlagEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ReverieModEntities.GOBLIN_FLAG.get(), world);
    }

    public GoblinFlagEntity(EntityType<?> type, Level world) {
        super(type, world);
        this.noPhysics = false;
        this.blocksBuilding = true;
    }

    public GoblinFlagEntity(EntityType<?> type, Level world, double x, double y, double z, Player owner) {
        this(type, world);
        this.setPos(x, y, z);
        this.setOwnerUUID(owner.getUUID());
        this.setOwnerName(owner.getName().getString());
        this.setYRot(owner.getYRot());
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_OWNER_NAME, "");
        this.entityData.define(DATA_DYING, false);
        this.entityData.define(DATA_GLOW_TICKS, 0); // Default 0
    }

    // Getter/Setter
    public void setGlowTicks(int ticks) { this.entityData.set(DATA_GLOW_TICKS, ticks); }
    public int getGlowTicks() { return this.entityData.get(DATA_GLOW_TICKS); }

    @Override
    public void tick() {
        super.tick();

        // Fizik
        if (!this.isNoGravity()) {
            Vec3 velocity = this.getDeltaMovement();
            this.setDeltaMovement(velocity.x, velocity.y - 0.04D, velocity.z);
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        float friction = 0.98F;
        if (this.onGround()) {
            friction = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement())
                    .getFriction(this.level(), this.getBlockPosBelowThatAffectsMyMovement(), this) * 0.98F;
        }
        this.setDeltaMovement(this.getDeltaMovement().multiply(friction, 0.98D, friction));

        // YENİ: Glow Logic
        int currentGlow = getGlowTicks();
        if (currentGlow > 0) {
            setGlowTicks(currentGlow - 1);
        }

        // Logic
        if (!level().isClientSide) {
            lifeTicks++;
            if (summonCooldown > 0) summonCooldown--;
            if (!isDying() && (lifeTicks > MAX_LIFE_TICKS || usesLeft <= 0)) startDying();
            if (isDying() && lifeTicks > MAX_LIFE_TICKS + 20) discard();
        } else {
            if (!isDying()) {
                animationState2.stop();
                animationState1.startIfStopped(this.tickCount);
            } else {
                animationState1.stop();
                animationState2.startIfStopped(this.tickCount);
            }
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (level().isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
        if (isDying()) return InteractionResult.FAIL;

        if (ownerUuid != null && !player.getUUID().equals(ownerUuid)) {
            sendMsg(serverPlayer, "message.reverie.banner_not_yours", ChatFormatting.DARK_RED);
            return InteractionResult.FAIL;
        }
        if (usesLeft <= 0) {
            sendMsg(serverPlayer, "message.reverie.banner_empty", ChatFormatting.YELLOW);
            return InteractionResult.FAIL;
        }
        if (summonCooldown > 0) {
            sendMsg(serverPlayer, "message.reverie.barrel_on_cooldown", ChatFormatting.YELLOW);
            return InteractionResult.FAIL;
        }
        State rep = GoblinReputation.getState(player);
        if (!(rep == State.FRIENDLY || rep == State.HELPFUL)) {
            playSoundToPlayer(serverPlayer, ReverieModSounds.GOBLIN_STAFF_DECLINE.get());
            sendMsg(serverPlayer, "message.reverie.barrel_rep_too_low", ChatFormatting.RED);
            return InteractionResult.FAIL;
        }
        if (!isSkyClearAbove()) {
            playSoundToPlayer(serverPlayer, ReverieModSounds.GOBLIN_STAFF_DECLINE.get());
            sendMsg(serverPlayer, "message.reverie.barrel_block_not_clear", ChatFormatting.RED);
            return InteractionResult.FAIL;
        }

        summonBarrel(serverPlayer);
        usesLeft--;
        summonCooldown = SUMMON_COOLDOWN_TICKS;

        // PARLAMAYI BAŞLAT (20 Tick = 1 Saniye)
        this.setGlowTicks(120);

        return InteractionResult.SUCCESS;
    }

    // --- Standard Methods ---
    @Override public EntityDimensions getDimensions(Pose pose) { return EntityDimensions.fixed(0.8F, 2.0F); }
    @Override public boolean isPickable() { return true; }
    @Override public boolean isPushable() { return true; }
    @Override public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide || isDying()) return false;
        this.hp -= amount;
        if (this.hp <= 0) startDying();
        return true;
    }

    // Getters/Setters & NBT
    public void setOwnerUUID(UUID uuid) { this.ownerUuid = uuid; }
    public UUID getOwnerUUID() { return this.ownerUuid; }
    public void setOwnerName(String name) { this.entityData.set(DATA_OWNER_NAME, name == null ? "" : name); }
    public String getOwnerName() { return this.entityData.get(DATA_OWNER_NAME); }
    public void setDying(boolean dying) { this.entityData.set(DATA_DYING, dying); }
    public boolean isDying() { return this.entityData.get(DATA_DYING); }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) this.ownerUuid = tag.getUUID("Owner");
        if (tag.contains("OwnerName")) setOwnerName(tag.getString("OwnerName"));
        this.hp = tag.getInt("HP");
        this.lifeTicks = tag.getInt("LifeTicks");
        this.usesLeft = tag.getInt("UsesLeft");
        this.summonCooldown = tag.getInt("SummonCooldown");
        setDying(tag.getBoolean("Dying"));
        setGlowTicks(tag.getInt("GlowTicks"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUuid != null) tag.putUUID("Owner", ownerUuid);
        tag.putString("OwnerName", getOwnerName());
        tag.putInt("HP", hp);
        tag.putInt("LifeTicks", lifeTicks);
        tag.putInt("UsesLeft", usesLeft);
        tag.putInt("SummonCooldown", summonCooldown);
        tag.putBoolean("Dying", isDying());
        tag.putInt("GlowTicks", getGlowTicks());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    // Helpers
    private void sendMsg(ServerPlayer player, String key, ChatFormatting color) {
        player.displayClientMessage(Component.translatable(key).withStyle(color), true);
    }
    private void playSoundToPlayer(ServerPlayer player, net.minecraft.sounds.SoundEvent sound) {
        player.connection.send(new ClientboundSoundPacket(Holder.direct(sound), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1.4F, 0.9F, player.level().getRandom().nextLong()));
    }
    private void startDying() {
        if (isDying() || level().isClientSide) return;
        setDying(true);
        level().playSound(null, getX(), getY(), getZ(), ReverieModSounds.SPIKED_LOG_CRASH.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        if (lifeTicks < MAX_LIFE_TICKS) lifeTicks = MAX_LIFE_TICKS;
    }
    private boolean isSkyClearAbove() {
        BlockPos base = this.blockPosition();
        int maxY = level().getMaxBuildHeight();
        for (int y = base.getY() + 2; y <= maxY; y++) {
            BlockState state = level().getBlockState(new BlockPos(base.getX(), y, base.getZ()));
            if (state.isSolidRender(level(), new BlockPos(base.getX(), y, base.getZ()))) return false;
        }
        return true;
    }
    private void summonBarrel(ServerPlayer activator) {
        if (!(level() instanceof ServerLevel server)) return;
        double spawnX = this.getX();
        double spawnY = this.getY() + 125.0;
        double spawnZ = this.getZ();
        UUID barrelOwner = (ownerUuid != null) ? ownerUuid : activator.getUUID();
        String barrelOwnerName = !getOwnerName().isEmpty() ? getOwnerName() : activator.getName().getString();
        GoblinBarrelEntity barrel = new GoblinBarrelEntity(level(), spawnX, spawnY, spawnZ, barrelOwner);
        barrel.setOwnerName(barrelOwnerName);
        barrel.setYRot(activator.getYRot());
        barrel.setXRot(activator.getXRot());
        server.addFreshEntity(barrel);
        level().playSound(null, this.getX(), this.getY(), this.getZ(), ReverieModSounds.GOBLIN_BARREL_SUMMON.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }
    public static void init() {}
}