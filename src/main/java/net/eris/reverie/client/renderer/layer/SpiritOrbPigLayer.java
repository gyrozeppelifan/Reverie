package net.eris.reverie.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.model.SpiritOrbModel;
import net.eris.reverie.events.BoarMonkEvents;
import net.eris.reverie.init.ReverieModAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.animal.Pig;
import com.mojang.math.Axis;

public class SpiritOrbPigLayer extends RenderLayer<Pig, PigModel<Pig>> {

    private SpiritOrbModel cachedOrbModel;
    private static final ResourceLocation ORB_TEXTURE = new ResourceLocation(ReverieMod.MODID, "textures/particle/spirit_orb.png");

    public SpiritOrbPigLayer(RenderLayerParent<Pig, PigModel<Pig>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Pig pig, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        boolean shouldRender = false;

        if (pig.getPersistentData().contains(BoarMonkEvents.TAG_TRANSFORMING)) {
            shouldRender = true;
        }
        else {
            try {
                if (ReverieModAttributes.SPIRITUALITY != null) {
                    AttributeInstance attr = pig.getAttribute(ReverieModAttributes.SPIRITUALITY);
                    if (attr != null && attr.getValue() > 0.5D) {
                        shouldRender = true;
                    }
                }
            } catch (Exception ignored) {}
        }

        if (shouldRender) {
            float time = (float)pig.tickCount + partialTicks;
            renderPigOrbs(poseStack, buffer, packedLight, time, pig, partialTicks);
        }
    }

    private void renderPigOrbs(PoseStack poseStack, MultiBufferSource buffer, int packedLight, float time, Pig pig, float partialTicks) {
        if (cachedOrbModel == null) {
            cachedOrbModel = new SpiritOrbModel(Minecraft.getInstance().getEntityModels().bakeLayer(SpiritOrbModel.LAYER_LOCATION));
        }

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(ORB_TEXTURE));

        int orbCount = 6;
        float radius = 1.1F;
        float speed = 0.05F;

        float bodyRot = Mth.lerp(partialTicks, pig.yBodyRotO, pig.yBodyRot);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(bodyRot - 180.0F));

        for (int i = 0; i < orbCount; i++) {
            poseStack.pushPose();

            float angle = (time * speed) + (i * ((float)Math.PI * 2 / orbCount));
            float x = Mth.cos(angle) * radius;
            float z = Mth.sin(angle) * radius;

            // --- AYARLANDI: DAHA AŞAĞIDA (0.4F) ---
            float y = 0.4F + Mth.sin((time * 0.1F) + i) * 0.2F;

            poseStack.translate(x, y, z);

            // --- AYARLANDI: DAHA BÜYÜK (0.8F) ---
            float scale = 0.8F;
            poseStack.scale(scale, -scale, scale);

            float selfRot = time * 4.0F;
            poseStack.mulPose(Axis.YP.rotationDegrees(selfRot));

            cachedOrbModel.renderToBuffer(poseStack, vertexConsumer, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

            poseStack.popPose();
        }

        poseStack.popPose();
    }
}