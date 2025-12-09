package net.eris.reverie.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.eris.reverie.client.model.HogModel;
import net.eris.reverie.entity.HogEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;

public class HogRiderLayer extends RenderLayer<HogEntity, HogModel> {

    public HogRiderLayer(RenderLayerParent<HogEntity, HogModel> pRenderer) {
        super(pRenderer);
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, HogEntity pHog, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        if (!pHog.isVehicle()) return;

        for (Entity passenger : pHog.getPassengers()) {
            // Kendi oyuncumuzu (First Person) çizmeyelim, kafa karışmasın
            if (passenger == Minecraft.getInstance().player && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                continue;
            }

            pPoseStack.pushPose();

            // 1. Modelin "root" kemiğinin pozisyonunu al
            ModelPart root = this.getParentModel().root;
            root.translateAndRotate(pPoseStack);

            // 2. "mountplace" kemiğinin pozisyonunu al (Root'un çocuğu olduğu için üstüne eklenir)
            ModelPart mount = this.getParentModel().mountplace;
            mount.translateAndRotate(pPoseStack);

            // 3. Oyuncuyu Düzelt
            // Blockbench koordinat sistemiyle Minecraft entity sistemi bazen ters düşer.
            // Oyuncu ters duruyorsa buradaki 180 dereceyi sil veya değiştir.
            pPoseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

            // Hafif yukarı/aşağı ayar (Eğer domuzun içine gömülürse Y değerini oyna)
            pPoseStack.translate(0.0D, -0.5D, 0.0D);

            // 4. Yolcuyu Çiz
            renderPassenger(passenger, pPartialTick, pPoseStack, pBuffer, pPackedLight);

            pPoseStack.popPose();
        }
    }

    private <E extends Entity> void renderPassenger(E entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        EntityRenderer<? super E> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        if (renderer != null) {
            // Oyuncunun kendi rotasyonunu sıfırla ki domuza göre dönsün
            renderer.render(entity, 0.0F, partialTick, poseStack, buffer, packedLight);
        }
    }
}