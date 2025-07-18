package net.eris.reverie.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GlassShardsParticle extends TextureSheetParticle {
    public static GlassShardsParticleProvider provider(SpriteSet spriteSet) {
        return new GlassShardsParticleProvider(spriteSet);
    }

    public static class GlassShardsParticleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;
        public GlassShardsParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }
        @Override
        public Particle createParticle(SimpleParticleType typeIn,
                                       ClientLevel worldIn,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new GlassShardsParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
        }
    }

    private final SpriteSet spriteSet;
    private final float angularVel;

    protected GlassShardsParticle(ClientLevel world,
                                  double x, double y, double z,
                                  double vx, double vy, double vz,
                                  SpriteSet spriteSet) {
        super(world, x, y, z);
        this.spriteSet = spriteSet;

        // Çarpışma kutusu
        this.setSize(0.1f, 0.1f);
        // Görsel ölçek
        this.quadSize = 0.4f + this.random.nextFloat() * 0.2f; // 0.4–0.6 arası
        // Ömür
        this.lifetime = 40 + this.random.nextInt(20);        // 40–59 tik
        // Daha yavaş düşüş
        this.gravity = 0.2f;
        this.hasPhysics = true;
        // Başlangıç hızı handler’dan gelen
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        // Daha yavaş dönsün
        this.angularVel = (this.random.nextFloat() - 0.5f) * 0.05f;
        // Sprite
        this.pickSprite(spriteSet);
    }

    @Override
    public void tick() {
        super.tick();

        // Hızları her tick biraz azalt (damping)
        this.xd *= 0.96f;
        this.zd *= 0.96f;

        // Yere çarpınca zıplama
        if (this.onGround) {
            this.xd *= 0.6f;
            this.zd *= 0.6f;
            if (this.yd < 0) {
                this.yd = -this.yd * 0.5f;
            }
        }

        // Yavaş dönüş
        this.roll += this.angularVel;

        // Son 10 tik içinde fade-out
        int fadeStart = this.lifetime - 10;
        if (this.age >= fadeStart) {
            this.alpha = 1.0f - (float)(this.age - fadeStart) / 10f;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }
}
