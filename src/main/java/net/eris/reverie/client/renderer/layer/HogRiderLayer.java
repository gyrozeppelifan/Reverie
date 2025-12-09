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
            if (passenger == Minecraft.getInstance().player && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                continue;
            }

            pPoseStack.pushPose();

            // 1. Root pozisyonu
            ModelPart root = this.getParentModel().root;
            root.translateAndRotate(pPoseStack);

            // 2. Mount point pozisyonu
            ModelPart mount = this.getParentModel().mountplace;
            mount.translateAndRotate(pPoseStack);

            // 3. DÜZELTMELER
            // Blockbench modelleri genelde X ekseninde 180 derece dönük gelir
            pPoseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

            // Oyuncu ters duruyorsa bu satır onu düzeltir (Gerekirse aç/kapa)
            pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

            // MANUEL YÜKSELTME: Domuzun içine girmemesi için 0.8 blok yukarı alıyoruz
            pPoseStack.translate(0.0D, 0.8D, 0.0D);

            renderPassenger(passenger, pPartialTick, pPoseStack, pBuffer, pPackedLight);

            pPoseStack.popPose();
        }
    }

    private <E extends Entity> void renderPassenger(E entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        EntityRenderer<? super E> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        if (renderer != null) {
            renderer.render(entity, 0.0F, partialTick, poseStack, buffer, packedLight);
        }
    }
}