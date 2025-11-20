package net.eris.reverie.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.eris.reverie.client.render.ClientRenderTypes;
import net.eris.reverie.client.shader.ClientShaders;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class DrunkenRageGlowLayer<T extends LivingEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {

    // Komutun eklediği scoreboard tag. Komut sınıfında da bunu kullan: DrunkenRageGlowLayer.DEBUG_GLOW_TAG
    public static final String DEBUG_GLOW_TAG = "reverie_glow";

    private final RenderLayerParent<T, M> parent;

    public DrunkenRageGlowLayer(RenderLayerParent<T, M> parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {

        // Shader yüklenmemişse ya da tag yoksa çık
        if (ClientShaders.RED_GLOW == null) return;
        if (!entity.getTags().contains(DEBUG_GLOW_TAG)) return;

        var u = ClientShaders.RED_GLOW.getUniform("Time");
        if (u != null) {
            u.set((entity.level().getGameTime() + partialTicks) / 20f);
        }

        // Entity’nin kendi dokusu üzerinden ikinci pass
        ResourceLocation tex = parent.getTextureLocation(entity);
        var vb = buffers.getBuffer(ClientRenderTypes.redGlow(tex));

        this.getParentModel().renderToBuffer(
                pose, vb, 0xF000F0, OverlayTexture.NO_OVERLAY,
                1f, 1f, 1f, 1f
        );
    }
}
