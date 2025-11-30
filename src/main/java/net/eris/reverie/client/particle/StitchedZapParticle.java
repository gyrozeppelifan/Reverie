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
import net.minecraft.client.particle.SpriteSet;
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

    protected StitchedZapParticle(ClientLevel world, double x, double y, double z, double xd, double yd, double zd) {
        super(world, x, y, z);
        this.lifetime = 3;
        this.hasPhysics = false;

        Vec3 start = new Vec3(0, 0, 0);
        Vec3 end = new Vec3(xd, yd, zd);
        this.bolt = new CosmeticLightningBolt(start, end, world.random);
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
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
        List<List<Vec3>> branches = bolt.getAllBranches();

        // --- AYAR 1: HALE İNCELDİ ---
        // Eskiden 0.22F idi, şimdi 0.12F (Daha kibar)
        Vector4f glowColor = bolt.color;
        float glowWidth = 0.12F;

        for (List<Vec3> branch : branches) {
            renderBranch3D(lightningBuffer, matrix, branch, glowWidth, glowColor);
        }

        // --- AYAR 2: ÇEKİRDEK İNCELDİ ---
        // Eskiden 0.08F idi, şimdi 0.05F (İğne gibi)
        Vector4f coreColor = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
        float coreWidth = 0.05F;

        for (List<Vec3> branch : branches) {
            renderBranch3D(lightningBuffer, matrix, branch, coreWidth, coreColor);
        }

        bufferSource.endBatch(RenderType.lightning());
        poseStack.popPose();
    }

    private void renderBranch3D(VertexConsumer buffer, Matrix4f matrix, List<Vec3> points, float width, Vector4f color) {
        for (int i = 0; i < points.size() - 1; i++) {
            Vec3 p1 = points.get(i);
            Vec3 p2 = points.get(i + 1);
            draw3DTube(buffer, matrix, p1, p2, width, color);
        }
    }

    private void draw3DTube(VertexConsumer buffer, Matrix4f matrix, Vec3 start, Vec3 end, float width, Vector4f color) {
        Vec3 diff = end.subtract(start);
        Vec3 dir = diff.normalize();
        Vec3 upRef = Math.abs(dir.y) > 0.95 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);

        Vec3 right = dir.cross(upRef).normalize().scale(width);
        Vec3 up = dir.cross(right).normalize().scale(width);

        Vec3 c1 = start.add(right).add(up);
        Vec3 c2 = start.add(right).subtract(up);
        Vec3 c3 = start.subtract(right).subtract(up);
        Vec3 c4 = start.subtract(right).add(up);

        Vec3 c1_End = end.add(right).add(up);
        Vec3 c2_End = end.add(right).subtract(up);
        Vec3 c3_End = end.subtract(right).subtract(up);
        Vec3 c4_End = end.subtract(right).add(up);

        drawQuad(buffer, matrix, c1, c1_End, c2_End, c2, color);
        drawQuad(buffer, matrix, c2, c2_End, c3_End, c3, color);
        drawQuad(buffer, matrix, c3, c3_End, c4_End, c4, color);
        drawQuad(buffer, matrix, c4, c4_End, c1_End, c1, color);
    }

    private void drawQuad(VertexConsumer buffer, Matrix4f matrix, Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4, Vector4f color) {
        float r = color.x();
        float g = color.y();
        float b = color.z();
        float a = color.w();

        buffer.vertex(matrix, (float)v1.x, (float)v1.y, (float)v1.z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float)v2.x, (float)v2.y, (float)v2.z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float)v3.x, (float)v3.y, (float)v3.z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float)v4.x, (float)v4.y, (float)v4.z).color(r, g, b, a).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public Provider(SpriteSet sprites) { this.sprites = sprites; }
        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new StitchedZapParticle(world, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}