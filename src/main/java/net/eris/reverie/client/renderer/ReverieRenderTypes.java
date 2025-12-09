package net.eris.reverie.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.eris.reverie.client.ReverieClientEvents;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ReverieRenderTypes extends RenderType {

    // RenderType sınıfından miras aldığımız için bu kurucu metoda (constructor) ihtiyacımız var, ama kullanmayacağız.
    public ReverieRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    // İşte aradığımız metod burada!
    // Bu sınıf RenderType'ı extend ettiği için NO_CULL, COLOR_WRITE gibi protected değişkenleri görebiliyor.
    public static RenderType getStitchedFlash(ResourceLocation texture) {
        return create("stitched_flash",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                false,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(() -> ReverieClientEvents.stitchedFlashShader))
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY) // Artık hata vermez!
                        .setCullState(NO_CULL) // Artık hata vermez!
                        .setWriteMaskState(COLOR_WRITE) // Artık hata vermez!
                        .createCompositeState(false));


    }

    // SPIRITUAL PIG AURA - GÜNCELLENDİ
    public static RenderType getSpiritualAura(ResourceLocation texture) {
        return create("spiritual_aura",
                DefaultVertexFormat.NEW_ENTITY, // Formatı düzelttik
                VertexFormat.Mode.QUADS,
                256,
                false,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(() -> ReverieClientEvents.spiritualAuraShader))
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setLightmapState(LIGHTMAP) // Işıklandırmayı açtık (shader'da kullanmasak bile veri akışı için şart)
                        .setOverlayState(OVERLAY) // Overlay verisi şart
                        .createCompositeState(false));
    }
}