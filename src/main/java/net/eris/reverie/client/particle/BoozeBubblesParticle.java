package net.eris.reverie.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BoozeBubblesParticle extends TextureSheetParticle {
    public static BoozeBubblesParticleProvider provider(SpriteSet spriteSet) {
        return new BoozeBubblesParticleProvider(spriteSet);
    }

    public static class BoozeBubblesParticleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public BoozeBubblesParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new BoozeBubblesParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }

    private final SpriteSet spriteSet;
    private float angularVelocity;
    private float angularAcceleration;

    protected BoozeBubblesParticle(ClientLevel world,
                                   double x, double y, double z,
                                   double vx, double vy, double vz,
                                   SpriteSet spriteSet) {
        super(world, x, y, z);
        this.spriteSet = spriteSet;

        // Çarpışma kutusu
        this.setSize(0.2f, 0.2f);
        // Render ölçeği
        this.quadSize = 0.6f;

        this.lifetime = 50;
        this.gravity = -0.1f;
        this.hasPhysics = true;

        this.xd = vx * 0.5;
        this.yd = vy * 0.5;
        this.zd = vz * 0.5;

        this.angularVelocity = 0.001f;
        this.angularAcceleration = 0f;

        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        super.tick();
        this.oRoll = this.roll;
        this.roll += this.angularVelocity;
        this.angularVelocity += this.angularAcceleration;
        if (!this.removed) {
            // Frame geçişi artık her 5 tikte bir gerçekleşecek
            int frame = (this.age / 5) % 18 + 1;
            this.setSprite(this.spriteSet.get(frame, 18));
        }
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }
}
