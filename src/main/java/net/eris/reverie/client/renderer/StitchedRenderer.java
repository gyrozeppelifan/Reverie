package net.eris.reverie.client.renderer;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.model.Stitched;
import net.eris.reverie.client.model.animations.StitchedAnimation;
import net.eris.reverie.entity.StitchedEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class StitchedRenderer extends MobRenderer<StitchedEntity, Stitched<StitchedEntity>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(ReverieMod.MODID, "textures/entities/stitched.png");
    private static final ResourceLocation TEXTURE_SKELETON = new ResourceLocation(ReverieMod.MODID, "textures/entities/stitched_skeleton.png");

    // StitchedRenderer.java constructor'ı:
    public StitchedRenderer(EntityRendererProvider.Context context) {
        super(context, new AnimatedModel(context.bakeLayer(Stitched.LAYER_LOCATION)), 0.5f);


    }

    @Override
    public ResourceLocation getTextureLocation(StitchedEntity entity) {
        // Sadece Çarpılma (State 1) anında Yanıp Sönme (Flicker) yap
        if (entity.getState() == 1) {
            // Her 2 tick'te bir dokuyu değiştir (Hızlı yanıp sönme)
            if (entity.tickCount % 2 == 0) {
                return TEXTURE_SKELETON;
            }
        }
        return TEXTURE;
    }

    // --- ANIMASYON YÖNETİCİSİ (INNER CLASS) ---
    // Model dosyasına (Stitched.java) dokunmadan animasyonları oynatmak için bu yapıyı kullanıyoruz.
    private static final class AnimatedModel extends Stitched<StitchedEntity> {
        private final ModelPart root;

        // Animasyonları hesaplayacak olan görünmez yardımcı model
        private final HierarchicalModel<StitchedEntity> animator = new HierarchicalModel<StitchedEntity>() {
            @Override
            public ModelPart root() { return root; }

            @Override
            public void setupAnim(StitchedEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
                this.root().getAllParts().forEach(ModelPart::resetPose);

                // State 0 (Passive) veya 4 (Waiting) -> Yerde Yatış
                if (entity.getState() == 0 || entity.getState() == 4) {
                    this.animate(entity.passiveState, StitchedAnimation.passive, ageInTicks, 1f);
                } else {
                    // Fallback (Güvenlik önlemi)
                    this.animate(entity.passiveState, StitchedAnimation.passive, ageInTicks, 1f);
                }

                // Diğer durumlarda kendi animasyonlarını oynat
                this.animate(entity.electrocutedState, StitchedAnimation.electrocuted, ageInTicks, 1f);
                this.animate(entity.standupState, StitchedAnimation.standup, ageInTicks, 1f);

                // Yürüme (Eğer eklendiyse)
                // this.animate(entity.walkState, StitchedAnimation.walk, ageInTicks, 1f);
            }
        };

        public AnimatedModel(ModelPart root) {
            super(root);
            this.root = root; // Kök parçayı yakalıyoruz (Stitched.java'daki 'root' metodunu kullanır)
        }

        @Override
        public void setupAnim(StitchedEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            // 1. Önce Animator ile kemik animasyonlarını hesapla
            animator.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

            // 2. Sonra ana modelin (Stitched.java) kafa dönüşü işlemlerini yap
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }
}