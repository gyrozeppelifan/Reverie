package net.eris.reverie.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class WildFireParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    // Yapıcı metod (Constructor)
    protected WildFireParticle(ClientLevel pLevel, double pX, double pY, double pZ,
                               double pXSpeed, double pYSpeed, double pZSpeed, SpriteSet sprites) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        this.sprites = sprites;

        this.xd = pXSpeed;
        this.yd = pYSpeed + (Math.random() * 0.05D);
        this.zd = pZSpeed;
        this.gravity = 0.0F;
        this.friction = 0.96F;
        this.quadSize *= 1.5F;
        this.lifetime = 40 + this.random.nextInt(20);
        this.rCol = 1.0f;
        this.gCol = 0.95f;
        this.bCol = 0.8f;

        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);

        if (this.age < this.lifetime * 0.7) {
            this.quadSize += 0.001F;
        }

        float fadeStart = this.lifetime * 0.6F;
        if (this.age > fadeStart) {
            this.alpha = 1.0F - ((this.age - fadeStart) / (this.lifetime - fadeStart));
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    // --- KRİTİK EKLEME: Statik Provider Metodu ---
    // Senin kullandığın kayıt sisteminin aradığı metod bu.
    public static ParticleProvider<SimpleParticleType> provider(SpriteSet sprites) {
        return new Provider(sprites);
    }

    // Fabrika (Factory/Provider) İç Sınıfı
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel,
                                       double pX, double pY, double pZ,
                                       double pXSpeed, double pYSpeed, double pZSpeed) {
            return new WildFireParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, this.sprites);
        }
    }
}