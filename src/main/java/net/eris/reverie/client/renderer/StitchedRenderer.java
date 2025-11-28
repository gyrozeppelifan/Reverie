package net.eris.reverie.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.model.Stitched;
import net.eris.reverie.client.model.animations.StitchedAnimation;
import net.eris.reverie.entity.StitchedEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.HashSet;
import java.util.Set;

public class StitchedRenderer extends MobRenderer<StitchedEntity, Stitched<StitchedEntity>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(ReverieMod.MODID, "textures/entities/stitched.png");
    private static final ResourceLocation TEXTURE_SKELETON = new ResourceLocation(ReverieMod.MODID, "textures/entities/stitched_skeleton.png");

    // Liste burada kalsın, buna erişeceğiz
    public static final Set<StitchedEntity> electricEntitiesOnScreen = new HashSet<>();

    public StitchedRenderer(EntityRendererProvider.Context context) {
        super(context, new AnimatedModel(context.bakeLayer(Stitched.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public void render(StitchedEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        // Listeye ekleme mantığı aynı
        if (entity.getState() == 1) {
            electricEntitiesOnScreen.add(entity);
        }
    }

    // --- YENİ RENDER METODU (Alex's Caves Mantığı) ---
    // Bu metod sadece modeli çizer, gereksiz hesaplamaları atlar.
    public void renderModelDirectly(StitchedEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Animasyon ve duruş ayarlarını yap (SetupAnim)
        // Interpolasyon (titremeyi önler)
        float lerpBodyRot = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float lerpHeadRot = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float netHeadYaw = lerpHeadRot - lerpBodyRot;
        float headPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());

        float limbSwing = Mth.lerp(partialTicks, entity.walkAnimation.position() - entity.walkAnimation.speed(), entity.walkAnimation.position());
        float limbSwingAmount = Mth.lerp(partialTicks, entity.walkAnimation.speedO, entity.walkAnimation.speed());
        float ageInTicks = entity.tickCount + partialTicks;

        // Modeli hazırla
        this.model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // Modeli RenderType ile buffer'a bas
        // Alex's Caves burada özel RenderType kullanıyor ama biz standart translucent kullanalım şimdilik
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(entity)));

        // Çizimi yap
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(StitchedEntity entity) {
        if (entity.getState() == 1) {
            if (entity.tickCount % 2 == 0) {
                return TEXTURE_SKELETON;
            }
        }
        return TEXTURE;
    }

    // --- ANIMASYON YÖNETİCİSİ (Aynen Kalıyor) ---
    private static final class AnimatedModel extends Stitched<StitchedEntity> {
        private final ModelPart root;
        private final HierarchicalModel<StitchedEntity> animator = new HierarchicalModel<StitchedEntity>() {
            @Override public ModelPart root() { return root; }
            @Override public void setupAnim(StitchedEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
                this.root().getAllParts().forEach(ModelPart::resetPose);
                if (entity.getState() == 0 || entity.getState() == 4) {
                    this.animate(entity.passiveState, StitchedAnimation.passive, ageInTicks, 1f);
                } else {
                    this.animate(entity.passiveState, StitchedAnimation.passive, ageInTicks, 1f);
                }
                this.animate(entity.electrocutedState, StitchedAnimation.electrocuted, ageInTicks, 1f);
                this.animate(entity.standupState, StitchedAnimation.standup, ageInTicks, 1f);
            }
        };
        public AnimatedModel(ModelPart root) { super(root); this.root = root; }
        @Override public void setupAnim(StitchedEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            animator.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }
}