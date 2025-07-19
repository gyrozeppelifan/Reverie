package net.eris.reverie.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.PlayMessages;

import net.eris.reverie.init.ReverieModEntities;
import net.eris.reverie.init.ReverieModItems;

public class BoneSpearProjectileEntity extends AbstractArrow implements ItemSupplier {
    public static final ItemStack PROJECTILE_ITEM = new ItemStack(ReverieModItems.BONE_SPEAR.get());

    public BoneSpearProjectileEntity(PlayMessages.SpawnEntity packet, Level world) {
        super(ReverieModEntities.BONE_SPEAR_PROJECTILE.get(), world);
    }

    // --- EKLENECEK KISIM ---
    public BoneSpearProjectileEntity(Level world, LivingEntity shooter, ItemStack stack) {
        super(ReverieModEntities.BONE_SPEAR_PROJECTILE.get(), shooter, world);

    }

    public BoneSpearProjectileEntity(EntityType<? extends BoneSpearProjectileEntity> type, Level world) {
        super(type, world);
    }

    public BoneSpearProjectileEntity(EntityType<? extends BoneSpearProjectileEntity> type, double x, double y, double z, Level world) {
        super(type, x, y, z, world);
    }

    public BoneSpearProjectileEntity(EntityType<? extends BoneSpearProjectileEntity> type, LivingEntity shooter, Level world) {
        super(type, shooter, world);
    }



    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(ReverieModItems.BONE_SPEAR.get());
    }



    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public ItemStack getItem() {
        return PROJECTILE_ITEM;
    }



    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        target.setArrowCount(target.getArrowCount() - 1);
    }

    @Override
    public void tick() {
        super.tick();
        // Titreme önleyici: gereksiz velocity sıfırlamadan kaçın
        if (inGround) {
            this.setDeltaMovement(0, 0, 0); // yere saplanınca sabitlen
        }
    }

    // --- En önemli kısım: shoot fonksiyonu ---
    public static BoneSpearProjectileEntity shoot(Level world, LivingEntity shooter, RandomSource rnd, float power) {
        BoneSpearProjectileEntity arrow =
                new BoneSpearProjectileEntity(ReverieModEntities.BONE_SPEAR_PROJECTILE.get(), shooter, world);

        // Bunu birebir vanilla Trident’ten kopya ettim
        // Vanilla’daki gibi “göz” seviyesinden tam baktığın yöne fırlat
        double px = shooter.getX();
        double py = shooter.getY() + shooter.getEyeHeight() - 0.1;
        double pz = shooter.getZ();
        arrow.setPos(px, py, pz);

        // power*2 vanilla trident ile aynı kuvvet; spread=1.0F
        arrow.shootFromRotation(shooter,
                shooter.getXRot(), shooter.getYRot(),
                0.0F, // pitch offset yok
                power * 2.5F, // vanilla Trident gibi kuvvetli gitsin
                1.0F // spread
        );

        arrow.setSilent(true);
        arrow.setCritArrow(true);
        arrow.setBaseDamage(3); // istersen damage'i arttırabilirsin
        arrow.setKnockback(1);

        if (shooter instanceof ServerPlayer sp && sp.getAbilities().instabuild) {
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        } else {
            arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        }

        world.addFreshEntity(arrow);
        world.playSound(null, px, py, pz,
                ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.arrow.shoot")),
                SoundSource.PLAYERS,
                1f,
                1f / (rnd.nextFloat() * 0.5f + 1f) + (power / 2f));
        return arrow;
    }
}
