package net.eris.reverie.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.HierarchicalModel;

import net.eris.reverie.entity.SpikedLogEntity;
import net.eris.reverie.client.model.animations.spiked_logAnimation;
import net.eris.reverie.client.model.Modelspiked_log;

import java.util.Set;  // EKLENDİ

public class SpikedLogRenderer extends MobRenderer<SpikedLogEntity, Modelspiked_log<SpikedLogEntity>> {
    // — EKLENDİ: Texture sabitleri
    private static final ResourceLocation DEFAULT =
            new ResourceLocation("reverie:textures/entities/spiked_log.png");
    private static final ResourceLocation DRESSED =
            new ResourceLocation("reverie:textures/entities/dressed_spiked_log.png");
    // — EKLENDİ: Özel skin alacak beta tester’lar
    private static final Set<String> DRESSED_TESTERS = Set.of(
            "yomai68",
            "Steve"
            // … başkalarını buraya ekle
    );

    public SpikedLogRenderer(EntityRendererProvider.Context context) {
        super(context, new AnimatedModel(context.bakeLayer(Modelspiked_log.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(SpikedLogEntity entity) {
        // Eğer ownerName listeye dahilse, özel skin’i döndür; aksi halde default
        String owner = entity.getOwnerName();
        if (DRESSED_TESTERS.contains(owner)) {
            return DRESSED;
        }
        return DEFAULT;
    }

    private static final class AnimatedModel extends Modelspiked_log<SpikedLogEntity> {
        private final ModelPart root;
        private final HierarchicalModel<SpikedLogEntity> animator = new HierarchicalModel<>() {
            @Override
            public ModelPart root() {
                return root;
            }

            @Override
            public void setupAnim(SpikedLogEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
                // Her frame önce pozları sıfırla
                this.root().getAllParts().forEach(ModelPart::resetPose);

                // Animasyon sadece isRolling == true iken
                if (entity.isRolling()) {
                    this.animate(entity.animationState0, spiked_logAnimation.roll, ageInTicks, 0.7f);
                    // jump animasyonunuz varsa:
                    // this.animate(entity.animationState0, spiked_logAnimation.jump, ageInTicks, 1f);
                }
            }
        };

        public AnimatedModel(ModelPart root) {
            super(root);
            this.root = root;
        }

        @Override
        public void setupAnim(SpikedLogEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            // Sadece bizim animator’ü çalıştır; super.setupAnim çağrısı yok
            animator.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }
}
