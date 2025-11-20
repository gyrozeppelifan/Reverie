package net.eris.reverie.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.eris.reverie.client.shader.ClientShaders;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class ClientRenderTypes {
    private ClientRenderTypes() {}

    /** Post-outline için MASKE çizen pass (blend yok, sadece color yazar). */
    public static RenderType redGlow(ResourceLocation texture) {
        var shader   = new RenderStateShard.ShaderStateShard(() -> ClientShaders.RED_GLOW);
        var tex      = new RenderStateShard.TextureStateShard(texture, false, false);

        var noBlend  = new RenderStateShard.TransparencyStateShard(
                "reverie_no_blend",
                RenderSystem::disableBlend,
                RenderSystem::disableBlend
        );
        var cullOff     = new RenderStateShard.CullStateShard(false);
        var lightmapOff = new RenderStateShard.LightmapStateShard(false);
        var overlayOn   = new RenderStateShard.OverlayStateShard(true);
        var colorOnly   = new RenderStateShard.WriteMaskStateShard(true, false); // depth yazma kapalı

        var state = RenderType.CompositeState.builder()
                .setShaderState(shader)
                .setTextureState(tex)
                .setTransparencyState(noBlend)
                .setCullState(cullOff)
                .setLightmapState(lightmapOff)
                .setOverlayState(overlayOn)
                .setWriteMaskState(colorOnly)
                // .setDepthTestState(...)  -> gerek yok, default LEQUAL
                .createCompositeState(true);

        return RenderType.create(
                "reverie_redglow_mask",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256, true, true, state
        );
    }
}
