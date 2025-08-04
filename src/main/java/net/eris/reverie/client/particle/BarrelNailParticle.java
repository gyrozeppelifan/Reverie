package net.eris.reverie.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class BarrelNailParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;

    public BarrelNailParticle(ClientLevel world, double x, double y, double z,
                                double vx, double vy, double vz, SpriteSet spriteSet) {
        super(world, x, y, z, vx, vy, vz);  // <---- ARTIK ÇARPAN YOK!
        this.spriteSet = spriteSet;
        this.gravity = 0.06F;
        this.quadSize *= 2.5F;
        this.lifetime = 16 + this.random.nextInt(16);
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        // Önce yükselme + düşme
        this.yd -= this.gravity;
        this.move(this.xd, this.yd, this.zd);

        // Sürüklenme
        this.xd *= 0.98D;
        this.yd *= 0.98D;
        this.zd *= 0.98D;

        // Yere çarptıysa ekstra yavaşlat
        if (this.onGround) {
            this.xd *= 0.7D;
            this.zd *= 0.7D;
        }

        // Yaşa göre sprite’ı güncelle
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    /** RegisterParticleProvidersEvent sırasında bunu kullan */
    public static ParticleProvider<SimpleParticleType> provider(SpriteSet spriteSet) {
        return new Factory(spriteSet);
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;
        public Factory(SpriteSet spriteSet) { this.spriteSet = spriteSet; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new BarrelShard1Particle(world, x, y, z, vx, vy, vz, spriteSet);
        }
    }
}
