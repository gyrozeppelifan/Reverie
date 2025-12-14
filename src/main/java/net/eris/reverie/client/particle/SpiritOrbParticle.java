package net.eris.reverie.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.model.SpiritOrbModel;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import com.mojang.math.Axis;

public class SpiritOrbParticle extends Particle {

    private final SpiritOrbModel model;
    private static final ResourceLocation TEXTURE = new ResourceLocation(ReverieMod.MODID, "textures/particle/spirit_orb.png");

    private final float orbScale;

    protected SpiritOrbParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpiritOrbModel model) {
        super(level, x, y, z);
        this.model = model;

        // Hızları sıfırlıyoruz, çünkü Handler zaten her tick yeni pozisyona koyuyor.
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;

        // --- KRİTİK AYAR 1: ÖMÜR ---
        // Sadece 1 Tick yaşasın. Böylece arkada iz bırakmaz, binlerce olmaz.
        this.lifetime = 1;

        this.gravity = 0.0F;

        // --- KRİTİK AYAR 2: BOYUT ---
        // 0.5F küçüktü, 0.85F yaptık (Daha dolgun dursun)
        this.orbScale = 0.85F;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 camPos = camera.getPosition();
        float f = (float)(Mth.lerp(partialTicks, this.xo, this.x) - camPos.x());
        float f1 = (float)(Mth.lerp(partialTicks, this.yo, this.y) - camPos.y());
        float f2 = (float)(Mth.lerp(partialTicks, this.zo, this.z) - camPos.z());

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(f, f1, f2);

        poseStack.scale(this.orbScale, -this.orbScale, this.orbScale);

        // --- DÖNME MANTIĞI GÜNCELLENDİ ---
        // Partikül her tick yenilendiği için "this.age" hep 0 olur ve dönmez.
        // O yüzden Dünya Zamanını (GameTime) kullanarak döndürüyoruz.
        float time = (Minecraft.getInstance().level.getGameTime() + partialTicks) * 0.1F;
        float rot = time * 15.0F; // Dönüş hızı

        poseStack.mulPose(Axis.YP.rotationDegrees(rot));
        poseStack.mulPose(Axis.XP.rotationDegrees(rot * 0.5F)); // Hafif çapraz da dönsün

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucent(TEXTURE));

        this.model.renderToBuffer(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        bufferSource.endBatch();
        poseStack.popPose();
    }

    // ... (Geri kalan metotlar, getRenderType ve Factory aynı kalıyor) ...
    @Override
    public ParticleRenderType getRenderType() { return ParticleRenderType.CUSTOM; }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        public Factory() { }
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SpiritOrbModel model = new SpiritOrbModel(Minecraft.getInstance().getEntityModels().bakeLayer(SpiritOrbModel.LAYER_LOCATION));
            return new SpiritOrbParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, model);
        }
    }
}