package net.eris.reverie.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.model.Stitched;
import net.eris.reverie.client.model.animations.StitchedAnimation;
import net.eris.reverie.entity.StitchedEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class StitchedRenderer extends MobRenderer<StitchedEntity, Stitched<StitchedEntity>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(ReverieMod.MODID, "textures/entities/stitched.png");
    private static final ResourceLocation TEXTURE_SKELETON = new ResourceLocation(ReverieMod.MODID, "textures/entities/stitched_skeleton.png");

    // --- ALEX'S CAVES MANTIĞI: SHADER VE LİSTE BURADA ---
    public static final ResourceLocation ELECTRIC_CHAIN_LOCATION = new ResourceLocation(ReverieMod.MODID, "shaders/post/entity_outline_electric.json");
    private static final Set<StitchedEntity> electricEntitiesOnScreen = new HashSet<>();

    public StitchedRenderer(EntityRendererProvider.Context context) {
        super(context, new AnimatedModel(context.bakeLayer(Stitched.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public void render(StitchedEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        // Eğer çarpılıyorsa (State 1), bu karede outline çizilecekler listesine ekle
        if (entity.getState() == 1) {
            electricEntitiesOnScreen.add(entity);
        }
    }

    // --- BATCH RENDER (TOPLU ÇİZİM) ---
    // Bu metodu ElectricPostProcessor çağıracak
    public static void renderElectricBatch(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, Camera camera) {
        if (electricEntitiesOnScreen.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();

        for (StitchedEntity entity : electricEntitiesOnScreen) {
            // Kameraya göre pozisyonu ayarla (Buffer içine doğru yere çizmek için)
            double x = entity.getX() - camera.getPosition().x;
            double y = entity.getY() - camera.getPosition().y;
            double z = entity.getZ() - camera.getPosition().z;

            poseStack.pushPose();
            poseStack.translate(x, y, z);

            // Entity'i çiz (Full Işık ile)
            mc.getEntityRenderDispatcher().render(entity, 0, 0, 0, 0, partialTick, poseStack, bufferSource, 15728640);

            poseStack.popPose();
        }

        // Listeyi temizle ki sonraki karede tekrar dolsun
        electricEntitiesOnScreen.clear();
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