package net.eris.reverie.client.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.eris.reverie.client.shader.ClientShaders;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.InventoryMenu;
import org.lwjgl.opengl.GL11;

/**
 * 1.20.1’de RenderStateShard sabitlerinin çoğu protected olduğu için
 * hepsini builder üzerinden kendi shard’larımızla kuruyoruz.
 */
public final class ReverieRenderTypes {
    private ReverieRenderTypes() {}

    private static RenderType RED_GLOW;

    /** Yumuşak kırmızı parıltı: additive blend + derine yazma kapalı. */
    public static RenderType redGlow() {
        if (RED_GLOW == null) {
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    // Shader
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> ClientShaders.RED_GLOW))
                    // Item/Block atlası: shader'ın Sampler0'ı dokuyu okuyabilsin
                    .setTextureState(new RenderStateShard.TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, true))
                    // Additive transparanlık
                    .setTransparencyState(new RenderStateShard.TransparencyStateShard(
                            "reverie_additive",
                            () -> {
                                RenderSystem.enableBlend();
                                RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                            },
                            () -> {
                                RenderSystem.disableBlend();
                                RenderSystem.defaultBlendFunc();
                            }
                    ))
                    // Sırt yüzeyi kapatma (NO_CULL)
                    .setCullState(new RenderStateShard.CullStateShard(false))
                    // LEQUAL depth test
                    .setDepthTestState(new RenderStateShard.DepthTestStateShard("lequal", GL11.GL_LEQUAL))
                    // Renge yaz, derine yazma kapalı
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
                    // Işıklandırma/overlay kapalı
                    .setLightmapState(new RenderStateShard.LightmapStateShard(false))
                    .setOverlayState(new RenderStateShard.OverlayStateShard(false))
                    .createCompositeState(true);

            RED_GLOW = RenderType.create(
                    "reverie:red_glow",
                    DefaultVertexFormat.NEW_ENTITY,     // item/entity modelleri için uygun
                    VertexFormat.Mode.TRIANGLES,
                    256,
                    true,   // affectsCrumbling
                    true,   // sortOnUpload
                    state
            );
        }
        return RED_GLOW;
    }
}
