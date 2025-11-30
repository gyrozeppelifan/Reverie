package net.eris.reverie.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.eris.reverie.client.particle.lightning.CosmeticLightningBolt;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.List;

public class StitchedZapParticle extends Particle {
    private final CosmeticLightningBolt bolt;

    // xd, yd, zd: Hedefin başlangıça göre konumu (Vector)
    protected StitchedZapParticle(ClientLevel world, double x, double y, double z, double xd, double yd, double zd) {
        super(world, x, y, z);
        this.lifetime = 3; // Çok kısa yaşasın (pırpır etsin diye sürekli yenisini spawnlayacağız)
        this.hasPhysics = false;

        Vec3 start = new Vec3(0, 0, 0); // Partikülün kendi merkezi
        Vec3 end = new Vec3(xd, yd, zd); // Hedef nokta

        this.bolt = new CosmeticLightningBolt(start, end, world.random);
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        // Vanilla Lightning Render Type kullanıyoruz (Parlama efekti için)
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer lightningBuffer = bufferSource.getBuffer(RenderType.lightning());

        Vec3 camPos = camera.getPosition();
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - camPos.x);
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - camPos.y);
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - camPos.z);

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(x, y, z);

        Matrix4f matrix = poseStack.last().pose();
        List<Vec3> points = bolt.getSegments();
        Vector4f color = bolt.color;

        // Çizgi kalınlığı
        float width = 0.05F;

        // Segmentleri çiz
        for (int i = 0; i < points.size() - 1; i++) {
            Vec3 p1 = points.get(i);
            Vec3 p2 = points.get(i + 1);

            // Basit bir Quad (Dörtgen) çizimi
            drawLine(lightningBuffer, matrix, p1, p2, width, color);
            // Çapraz Quad (3D görünüm için)
            drawLine(lightningBuffer, matrix, p1.add(width, 0, width), p2.add(width, 0, width), width, color);
        }

        bufferSource.endBatch(RenderType.lightning()); // Çizimi bitir
        poseStack.popPose();
    }

    private void drawLine(VertexConsumer buffer, Matrix4f matrix, Vec3 start, Vec3 end, float width, Vector4f color) {
        // Basitleştirilmiş yıldırım geometrisi
        buffer.vertex(matrix, (float)start.x, (float)start.y, (float)start.z).color(color.x, color.y, color.z, color.w).endVertex();
        buffer.vertex(matrix, (float)end.x, (float)end.y, (float)end.z).color(color.x, color.y, color.z, color.w).endVertex();
        // Kalınlık için yan noktalar eklenebilir ama RenderType.lightning zaten kalınlaştırıyor.
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double xd, double yd, double zd) {
            return new StitchedZapParticle(world, x, y, z, xd, yd, zd);
        }
    }
}