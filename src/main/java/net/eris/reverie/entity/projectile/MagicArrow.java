package net.eris.reverie.entity.projectile;

import net.eris.reverie.init.ReverieModEntities;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class MagicArrow extends Arrow {

    public MagicArrow(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
        configureMagicArrow();
    }

    public MagicArrow(Level level, double x, double y, double z) {
        super(level, x, y, z);
        configureMagicArrow();
    }

    public MagicArrow(Level level, LivingEntity shooter) {
        super(level, shooter);
        configureMagicArrow();
    }

    private void configureMagicArrow() {
        this.setNoGravity(true);
        this.setPierceLevel((byte) 127);
        this.noPhysics = true;

        // DÜZELTME: OKUN ALINMASINI ENGELLE (Disallowed)
        // Bu sayede doğduğu an oyuncu onu "yerden alamaz".
        this.pickup = Pickup.DISALLOWED;
    }

    @Override
    public EntityType<?> getType() {
        return ReverieModEntities.MAGIC_ARROW.get();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        if (this.level().isClientSide) {
            Vec3 nextPos = this.position().add(this.getDeltaMovement());
            this.setPos(nextPos.x, nextPos.y, nextPos.z);
            return;
        }

        // Ömür (40 tick = 2 sn)
        if (this.tickCount > 40) {
            this.discard();
            return;
        }

        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            this.onHit(hitResult);
        }

        Vec3 nextPos = this.position().add(this.getDeltaMovement());
        this.setPos(nextPos.x, nextPos.y, nextPos.z);

        this.checkInsideBlocks();
        this.tickCount++;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();

        if (target.is(this.getOwner()) && this.tickCount < 5) return;

        float damage = (float) (this.getBaseDamage() * this.getDeltaMovement().length());
        if (this.isCritArrow()) {
            damage += this.random.nextInt((int) (damage / 2 + 2));
        }

        target.hurt(this.damageSources().arrow(this, this.getOwner()), damage);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        // Blok içinden geç
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (entity.isSpectator()) return false;
        if (entity.is(this.getOwner()) && this.tickCount < 5) return false;
        return super.canHitEntity(entity);
    }
}