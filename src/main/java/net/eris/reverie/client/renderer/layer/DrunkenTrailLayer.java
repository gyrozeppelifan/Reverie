package net.eris.reverie.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.eris.reverie.util.IAncientCloakData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class DrunkenTrailLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    public DrunkenTrailLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        // 1. Efekt Kontrolü
        boolean hasRage = ((IAncientCloakData) entity).reverie$hasDrunkenRage();
        if (!hasRage) return;

        // 2. YÖN HESAPLAMA (DÜZELTİLDİ)
        Vec3 velocity = entity.getDeltaMovement();
        double horizontalSpeedSqr = velocity.x * velocity.x + velocity.z * velocity.z;

        Vec3 horizontalDir;

        if (horizontalSpeedSqr > 0.001) {
            // Hareket ediyorsa: Hız yönünü al
            horizontalDir = new Vec3(velocity.x, 0, velocity.z).normalize();
        } else {
            // DİKKAT: DURUYORSA BURASI ÇALIŞIR
            // Hareket etmiyorsa "Baktığı Yönü" (Body Yaw) hız vektörü gibi kabul et.
            // Böylece hayaletler dururken bile arkasında (sırtında) dizilir.
            float yRotRad = entity.yBodyRot * ((float)Math.PI / 180F);
            // Minecraft rotasyonundan vektör hesabı:
            horizontalDir = new Vec3(-Mth.sin(yRotRad), 0, Mth.cos(yRotRad)).normalize();
        }

        ResourceLocation skin = this.getTextureLocation(entity);
        this.getParentModel().copyPropertiesTo(this.getParentModel());

        // İnterpolasyonlu Vücut Açısı
        float bodyYaw = Mth.lerp(partialTick, entity.yBodyRotO, entity.yBodyRot);

        // --- HAYALET DÖNGÜSÜ (4 KOPYA) ---
        for (int i = 1; i <= 5; i++) {
            poseStack.pushPose();

            // A. World Space Dönüşümü
            poseStack.mulPose(Axis.YP.rotationDegrees(bodyYaw - 180.0F));

            // B. SABİT MESAFE
            // Her hayalet 0.6 blok arkada
            double fixedStep = 0.6D;
            double distance = i * fixedStep;

            // "direction" nereye bakıyorsa (hareket veya bakış), tersine ötele
            poseStack.translate(
                    -horizontalDir.x * distance,
                    0.0D,
                    -horizontalDir.z * distance
            );

            // C. Local Space Dönüşümü
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));

            // D. Görsel Ayarlar
            float scale = 1.0F - (i * 0.08F);
            poseStack.scale(scale, scale, scale);

            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(skin));

            // Silikleşme
            float alpha = 0.5F - (i * 0.1F);
            if (alpha < 0) alpha = 0;

            // Full Bright (Karanlıkta Parlar)
            this.getParentModel().renderToBuffer(poseStack, vertexConsumer, 0xF000F0,
                    OverlayTexture.NO_OVERLAY,
                    0.8F, 0.0F, 0.7F, alpha);

            poseStack.popPose();
        }
    }
}