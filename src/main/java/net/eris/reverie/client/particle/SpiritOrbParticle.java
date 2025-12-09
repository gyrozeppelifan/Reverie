package net.eris.reverie.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpiritOrbParticle extends TextureSheetParticle {

    // --- PROVIDER (FABRİKA) ---
    // Senin kayıt sistemine uygun Provider yapısı
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new SpiritOrbParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }

    private final SpriteSet spriteSet;
    private final double centerX, centerZ; // Dönüş merkezi

    protected SpiritOrbParticle(ClientLevel world,
                                double x, double y, double z,
                                double vx, double vy, double vz,
                                SpriteSet spriteSet) {
        super(world, x, y, z);
        this.spriteSet = spriteSet;

        // Domuzun merkezini (başlangıç noktasını) kaydet
        this.centerX = x;
        this.centerZ = z;

        // Görsel Ayarlar
        this.quadSize = 0.15F + (this.random.nextFloat() * 0.1F); // Boyut
        this.lifetime = 30 + this.random.nextInt(10); // Ömür
        this.gravity = 0.0F; // Yerçekimi yok, biz kontrol ediyoruz
        this.hasPhysics = false; // Bloklara takılmasın, içinden geçsin

        // Hız değerlerini "Dönüş Hızı" ve "Yarıçap" olarak kullanacağız
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;

        // RENK PALETİ: Monk Teması (%70 Akuamarin, %30 Altın)
        if (this.random.nextFloat() < 0.7f) {
            this.rCol = 0.6F; this.gCol = 1.0F; this.bCol = 0.84F; // #99ffd6 (Akuamarin)
        } else {
            this.rCol = 1.0F; this.gCol = 0.9F; this.bCol = 0.4F; // Altın
        }

        // Sprite seçimi
        this.pickSprite(spriteSet);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            // --- MONK EFEKTİ: SPİRAL YÖRÜNGE ---

            // 1. Yukarı süzülme
            double riseSpeed = 0.05D;
            this.y += riseSpeed;

            // 2. Dönüş Hızı (Zamanla yavaşlasın veya hızlansın)
            double rotationSpeed = 0.3D;
            double currentAngle = (this.age * rotationSpeed) + (this.xd * 10); // xd'yi rastgele açı ofseti olarak kullandık

            // 3. Yarıçap (Zamanla genişlesin)
            double radius = 0.6D + (this.age * 0.01D);

            // 4. Yeni Konumu Hesapla (Merkez + Sin/Cos)
            // centerX ve centerZ sabit kaldığı için domuz hareket edince arkada iz bırakır (Trail effect)
            this.x = this.centerX + Math.cos(currentAngle) * radius;
            this.z = this.centerZ + Math.sin(currentAngle) * radius;

            // Parlama Efekti (Fade Out)
            if (this.age > this.lifetime - 10) {
                this.alpha = 1.0F - ((float)(this.age - (this.lifetime - 10)) / 10.0F);
            } else {
                this.alpha = 1.0F;
            }
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        // PARTICLE_SHEET_LIT: Karanlıkta parlar (Glow efektinin sırrı bu)
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880; // Full parlaklık (BoozeBubbles ile aynı)
    }
}