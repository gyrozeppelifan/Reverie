package net.eris.reverie.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.eris.reverie.client.renderer.ReverieRenderTypes;
import net.eris.reverie.init.ReverieModAttributes;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Pig;

public class SpiritualPigLayer extends RenderLayer<Pig, PigModel<Pig>> {

    private static final ResourceLocation PIG_LOCATION = new ResourceLocation("textures/entity/pig/pig.png");

    public SpiritualPigLayer(RenderLayerParent<Pig, PigModel<Pig>> pRenderer) {
        super(pRenderer);
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, Pig pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        // Kontroller
        if (ReverieModAttributes.SPIRITUALITY != null && pLivingEntity.getAttributeValue(ReverieModAttributes.SPIRITUALITY) > 0.5D) {

            if (pLivingEntity.isInvisible()) return;

            pPoseStack.pushPose();

            // --- TEMİZLİK YAPILDI ---
            // Artık burada Scale veya Translate yapmamıza gerek YOK!
            // İşin zor kısmını Vertex Shader (GPU) hallediyor.
            // Sadece modeli olduğu gibi, (1.0) boyutunda çiziyoruz.
            // Aura kendiliğinden şişip modele yapışacak.

            this.getParentModel().prepareMobModel(pLivingEntity, pLimbSwing, pLimbSwingAmount, pPartialTick);
            this.getParentModel().setupAnim(pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);

            VertexConsumer vertexConsumer = pBuffer.getBuffer(ReverieRenderTypes.getSpiritualAura(PIG_LOCATION));

            this.getParentModel().renderToBuffer(pPoseStack, vertexConsumer,
                    0xF000F0,
                    OverlayTexture.NO_OVERLAY,
                    1.0F, 1.0F, 1.0F, 1.0F);

            pPoseStack.popPose();
        }
    }
}