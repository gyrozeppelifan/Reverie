package net.eris.reverie.entity.projectile;

import net.eris.reverie.init.ReverieModEntities;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
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

    // Ortak ayarlar
    private void configureMagicArrow() {
        // 1. DELME ÖZELLİĞİ (PIERCING)
        // 5 varlığın içinden hasar vererek geçer.
        // (Sonsuz yapmak istersen 127 yapabilirsin ama 5 dengelidir)
        this.setPierceLevel((byte) 5);

        // 2. YERÇEKİMİ YOK
        // Büyülü ok olduğu için aşağı düşmez, lazer gibi dümdüz gider.
        this.setNoGravity(true);
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
        super.tick();

        // 3. ÖMÜR KISITLAMASI
        // Yerçekimi olmadığı için sonsuza kadar gitmesin.
        // 60 tick (3 saniye) sonra yok olsun. Zaten çok hızlı olduğu için menzili yeterli olur.
        if (this.tickCount > 60 && !this.level().isClientSide) {
            this.discard();
        }

        // İstersen uçarken arkasından partikül de bırakabilir (Renderer zaten trail yapıyor ama bu ekstra olur)
        // if (this.level().isClientSide) {
        //     this.level().addParticle(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        // }
    }
}