package net.eris.reverie.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class SpiritualBeamParticle extends TextureSheetParticle {

    // Hedef Koordinatlar (Hız değişkenlerini hedef olarak kullanıyoruz)
    private final double targetX;
    private final double targetY;
    private final double targetZ;

    protected SpiritualBeamParticle(ClientLevel level, double x, double y, double z, double tx, double ty, double tz, SpriteSet spriteSet) {
        super(level, x, y, z, 0, 0, 0);

        // Stitched mantığı: Velocity (Hız) değişkenlerini "Hedef Nokta" olarak kullanıyoruz.
        // x,y,z = Başlangıç (Domuz)
        // tx,ty,tz = Bitiş (Uzaydaki rastgele nokta)
        this.targetX = tx;
        this.targetY = ty;
        this.targetZ = tz;

        this.pickSprite(spriteSet);
        this.lifetime = 10; // Işın 10 tick (yarım saniye) ekranda kalsın
        this.gravity = 0.0F;
        this.quadSize = 0.3F; // Işının kalınlığı

        // RENK: Spiritüel (Camgöbeği/Mor karışımı)
        this.rCol = 0.2F;
        this.gCol = 0.9F;
        this.bCol = 1.0F;
        this.alpha = 1.0F;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        // İki nokta arasındaki vektör
        Vec3 start = new Vec3(this.x, this.y, this.z);
        Vec3 end = new Vec3(this.targetX, this.targetY, this.targetZ);
        Vec3 diff = end.subtract(start);

        double length = diff.length();
        if (length < 0.1) return;

        // Render Hazırlığı
        Vec3 cameraPos = camera.getPosition();
        float x = (float)(Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float)(Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float)(Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        // Dönüş (Rotation) Hesaplama: Işını hedefe çevir
        Vector3f vector = new Vector3f((float)diff.x, (float)diff.y, (float)diff.z);
        vector.normalize();

        // Basit bir billboarding yerine ışın çizimi
        // Burada StitchedZapParticle'daki render mantığını simüle ediyoruz:
        // Başlangıçtan Bitişe uzanan bir şerit (Quad) çiziyoruz.

        // ... (Burada karmaşık Quaternion hesabı yerine senin StitchedZap'taki gibi basit çizim yapıyoruz) ...

        // Işının yönüne göre Quaternion (Dönüş) hesapla
        Quaternionf quaternion = new Quaternionf().setAngleAxis(0.0F, 1.0F, 0.0F, 0.0F); // Varsayılan
        // Not: Gerçek Stitched kodunda muhtemelen "lookAt" benzeri bir vektör matematiği var.
        // Biz burada basitçe iki nokta arasına çizgi çeken standart bir yöntem kullanacağız.

        this.renderBeam(buffer, camera, x, y, z, (float)diff.x, (float)diff.y, (float)diff.z);
    }

    // ÖZEL BEAM RENDER METODU
    private void renderBeam(VertexConsumer consumer, Camera camera, float x, float y, float z, float dx, float dy, float dz) {
        // Kameraya dik bakması için Cross Product
        Vec3 look = camera.getPosition().subtract(this.x, this.y, this.z).normalize();
        Vec3 dir = new Vec3(dx, dy, dz).normalize();
        Vec3 up = look.cross(dir).normalize().scale(this.quadSize); // Işının genişliği

        float x1 = (float)up.x;
        float y1 = (float)up.y;
        float z1 = (float)up.z;

        // UV Koordinatları
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();

        int light = 15728880; // Full Bright

        // 4 Köşe (Dikdörtgen) - Başlangıçtan Bitişe
        // Start + Up
        consumer.vertex(x + x1, y + y1, z + z1).uv(u1, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
        // Start - Up
        consumer.vertex(x - x1, y - y1, z - z1).uv(u1, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
        // End - Up
        consumer.vertex(x + dx - x1, y + dy - y1, z + dz - z1).uv(u0, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
        // End + Up
        consumer.vertex(x + dx + x1, y + dy + y1, z + dz + z1).uv(u0, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; // Veya LIT
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double tx, double ty, double tz) {
            // tx, ty, tz -> Hız değil, HEDEF KOORDİNATLAR
            return new SpiritualBeamParticle(level, x, y, z, tx, ty, tz, this.spriteSet);
        }
    }
}