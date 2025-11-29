package net.eris.reverie.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.ReverieClientEvents;
import net.eris.reverie.client.model.Stitched;
import net.eris.reverie.client.model.animations.StitchedAnimation;
import net.eris.reverie.entity.StitchedEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class StitchedRenderer extends MobRenderer<StitchedEntity, Stitched> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(ReverieMod.MODID, "textures/entities/stitched.png");
    private static final ResourceLocation TEXTURE_SKELETON = new ResourceLocation(ReverieMod.MODID, "textures/entities/stitched_skeleton.png");

    public StitchedRenderer(EntityRendererProvider.Context context) {
        super(context, new AnimatedModel(context.bakeLayer(Stitched.LAYER_LOCATION)), 0.5f);

        // --- OUTLINE LAYER (Inverted Hull & Flash) ---
        this.addLayer(new RenderLayer<StitchedEntity, Stitched>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, StitchedEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                if (entity.getState() == 1) {
                    if (ReverieClientEvents.stitchedFlashShader != null) {
                        ReverieClientEvents.stitchedFlashShader.safeGetUniform("FlashColor").set(0.0F, 1.0F, 1.0F, 1.0F);
                    }

                    RenderType flashType = ReverieRenderTypes.getStitchedFlash(getTextureLocation(entity));
                    VertexConsumer vertexConsumer = buffer.getBuffer(flashType);

                    // Görünürlük ayarını burada da çağırıyoruz ki doğru parça parlasın
                    this.getParentModel().setupVisibility(entity.getHeadItem().is(Items.LIGHTNING_ROD));

                    this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
                }
            }
        });
    }

    // --- ANA RENDER METODU ---
    @Override
    public void render(StitchedEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Stitched model = this.getModel();

        // Kafa modülünü kontrol et ve görünürlüğü ayarla
        boolean hasRod = entity.getHeadItem().is(Items.LIGHTNING_ROD);
        model.setupVisibility(hasRod);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    // --- KARANLIKTA PARLAMA ---
    @Override
    protected int getBlockLightLevel(StitchedEntity entity, BlockPos pos) {
        // Çarpılma anında (State 1) full parlaklık
        if (entity.getState() == 1) {
            return 15;
        }
        return super.getBlockLightLevel(entity, pos);
    }

    @Override
    public ResourceLocation getTextureLocation(StitchedEntity entity) {
        if (entity.getState() == 1 && entity.tickCount % 2 == 0) {
            return TEXTURE_SKELETON;
        }
        return TEXTURE;
    }

    // --- ANİMASYON YÖNETİCİSİ ---
    private static final class AnimatedModel extends Stitched {
        private final ModelPart root;
        private final HierarchicalModel<StitchedEntity> animator = new HierarchicalModel<StitchedEntity>() {
            @Override public ModelPart root() { return root; }
            @Override public void setupAnim(StitchedEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
                this.root().getAllParts().forEach(ModelPart::resetPose);

                int state = entity.getState();

                // 1. PASSIVE (Doğduğunda yerde yatış)
                if (state == 0) {
                    this.animate(entity.passiveState, StitchedAnimation.passive, ageInTicks, 1f);
                }

                // 2. ELECTROCUTED (Dirilme Anı)
                else if (state == 1) {
                    this.animate(entity.electrocutedState, StitchedAnimation.electrocuted, ageInTicks, 1.4f);
                }

                // 3. STANDUP (Yatıştan Oturuşa)
                else if (state == 2) {
                    this.animate(entity.standupState, StitchedAnimation.standup, ageInTicks, 0.7f);
                }

                // 4. SIT ROARING (Oturarak Kükreme)
                else if (state == 5) {
                    this.animate(entity.sitRoaringState, StitchedAnimation.sitroaring, ageInTicks, 1.0f);
                }

                // 5. ALIVE (Canlı)
                else if (state == 3) {
                    boolean isMoving = entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6;
                    boolean isAttacking = entity.swinging; // Vuruyor mu?

                    // -- YÜRÜME MANTIĞI --
                    if (isMoving) {
                        if (isAttacking) {
                            // Hem yürüyor hem saldırıyorsa -> KOLSUZ YÜRÜME
                            // (Böylece saldırı animasyonuyla kollar çakışmaz)
                            this.animate(entity.walkNoArmsState, StitchedAnimation.walkingwithoutarms, ageInTicks, 1.0f);
                        } else {
                            // Sadece yürüyorsa -> NORMAL YÜRÜME
                            this.animate(entity.walkState, StitchedAnimation.walking, ageInTicks, 1.0f);
                        }
                    } else {
                        // Duruyorsa -> IDLE
                        // Saldırı yoksa ve kükremiyorsa Idle oynat
                        if (!isAttacking && !entity.roaringState.isStarted()) {
                            this.animate(entity.idleState, StitchedAnimation.idle, ageInTicks, 1.0f);
                        }
                    }

                    // -- SALDIRI MANTIĞI (FIX) --
                    if (isAttacking) {
                        this.animate(entity.attackState, StitchedAnimation.attackingbase, ageInTicks, 1.0f);
                    } else {
                        // Vuruş bittiği an animasyonu durdur ki takılı kalmasın
                        entity.attackState.stop();
                    }

                    // -- YETENEK (Roar) --
                    this.animate(entity.roaringState, StitchedAnimation.roaring, ageInTicks, 1.0f);
                }
            }
        };

        public AnimatedModel(ModelPart root) {
            super(root);
            this.root = root;
        }

        @Override
        public void setupAnim(StitchedEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            animator.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }
}