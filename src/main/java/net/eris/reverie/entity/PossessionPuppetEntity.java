package net.eris.reverie.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;

public class PossessionPuppetEntity extends PathfinderMob {

    public PossessionPuppetEntity(EntityType<? extends PossessionPuppetEntity> type, Level level) {
        super(type, level);
        this.setInvisible(true);
        this.setSilent(true);
        this.setNoAi(false);
        this.noPhysics = false;
        this.xpReward = 0;
    }

    // 2) setCustomClientFactory(...) için gerekli olan ctor
    public PossessionPuppetEntity(PlayMessages.SpawnEntity packet, Level level) {
        this(net.eris.reverie.init.ReverieModEntities.POSSESSION_PUPPET.get(), level);
    }

    // === Registry helper’ların çağıracağı kancalar ===
    public static void init() { /* goal yok; no-op */ }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D); // nav hız referansı
    }

    // === Navigation ===
    @Override
    protected PathNavigation createNavigation(Level level) {
        return new GroundPathNavigation(this, level);
    }

    // === Tamamen temas dışı / zarar görmez ===
    @Override public boolean isPushable() { return false; }
    @Override protected void doPush(Entity e) {}
    @Override public boolean canBeCollidedWith() { return false; }
    @Override public boolean isAttackable() { return false; }
    @Override public boolean isInvulnerableTo(DamageSource src) { return true; }
    @Override public boolean isAffectedByPotions() { return false; }
    @Override protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {}
    @Override protected void playStepSound(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {}

    // === Spawn packet (setCustomClientFactory kullandığımız için şart) ===
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

}
