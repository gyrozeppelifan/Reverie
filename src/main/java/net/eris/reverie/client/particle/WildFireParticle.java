package net.eris.reverie.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

// TextureSheetParticle, kendi doku dosyasını kullanan partiküller içindir.
public class WildFireParticle extends TextureSheetParticle {

    // Animasyon için gerekli sprite seti
    private final SpriteSet sprites;

    protected WildFireParticle(ClientLevel pLevel, double pX, double pY, double pZ,
                               double pXSpeed, double pYSpeed, double pZSpeed, SpriteSet sprites) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        this.sprites = sprites;

        // 1. Hız ve Hareket
        this.xd = pXSpeed; // X hızı (Genelde 0 veya çok az rastgele)
        this.yd = pYSpeed + (Math.random() * 0.05D); // Y hızı (Yukarı doğru, biraz rastgelelik ekle)
        this.zd = pZSpeed; // Z hızı
        this.gravity = 0.0F; // Yerçekimi yok, yukarı uçacak.
        this.friction = 0.96F; // Sürtünme, yükseldikçe yavaşlar.

        // 2. Görünüm
        this.quadSize *= 1.5F; // Boyut (Normalden biraz büyük başlasın)

        // 3. Ömür
        this.lifetime = 40 + this.random.nextInt(20); // 2-3 saniye yaşasın

        // 4. Renk (İstersen hafif turuncu/kırmızı ton verebilirsin, varsayılan beyazdır)
        this.rCol = 1.0f;
        this.gCol = 0.95f;
        this.bCol = 0.8f;

        // 5. Başlangıç Sprite'ını ve Animasyonu ayarla
        this.setSpriteFromAge(sprites);
    }

    // Her oyun döngüsünde (tick) ne yapacak?
    @Override
    public void tick() {
        super.tick();
        // Animasyonu yaşa göre güncelle (Bu olmazsa animasyon oynamaz!)
        this.setSpriteFromAge(this.sprites);

        // Yükseldikçe hafifçe büyüsün (Duman efekti için)
        if (this.age < this.lifetime * 0.7) {
            this.quadSize += 0.001F;
        }

        // Ömrünün sonuna doğru şeffaflaşarak kaybolsun (Fade out)
        float fadeStart = this.lifetime * 0.6F;
        if (this.age > fadeStart) {
            this.alpha = 1.0F - ((this.age - fadeStart) / (this.lifetime - fadeStart));
        }
    }

    // Bu partikül nasıl renderlanacak?
    @Override
    public ParticleRenderType getRenderType() {
        // PARTICLE_SHEET_LIT: Karanlıkta parlar (Ateş için en iyisi)
        // PARTICLE_SHEET_TRANSLUCENT: Yarı saydam olabilir ama karanlıkta parlamaz.
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    // --- FABRİKA (Factory/Provider) SINIFI ---
    // Oyunun bu partikülü nasıl oluşturacağını bilmesi için gerekli iç sınıf.
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
            // Yukarıdaki WildFireParticle'ı oluşturup döndürür.
            return new WildFireParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, this.sprites);
        }
    }
}