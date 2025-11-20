package net.eris.reverie.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.eris.reverie.init.ReverieModMobEffects;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.texture.TextureAtlas; // TextureAtlas'ı kullanıyoruz
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// Mixin'i doğru Mixin klasöründe tuttuğundan emin ol.
@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin {

    // Yöntem: MultiBufferSource'u saran ve Cutout tiplerini Translucent'a çeviren bir aracı oluşturma.
    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
    private MultiBufferSource reverie_forceTranslucentBuffer(MultiBufferSource buffer, PoseStack poseStack, MultiBufferSource originalBuffer, int light, LivingEntity entity) {

        // Eğer entity'nin üzerinde GİZLİLİK EFEKTİ varsa
        if (entity.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get())) {

            // Itemların doku atlası
            final ResourceLocation blockAtlas = TextureAtlas.LOCATION_BLOCKS;

            // --- BUFFER WRAPPER (ÇÖZÜM) ---
            // Oyun bizden hangi RenderType'ı isterse istesin, biz Translucent (Şeffaf) buffer'ı döndüreceğiz.
            return type -> {
                // Eğer gelen tip opak (Cutout) ise
                if (type.toString().contains("cutout")) {
                    // Zorla Translucent Buffer'ı kullan
                    return buffer.getBuffer(RenderType.itemEntityTranslucentCull(blockAtlas));
                }
                // Diğer tipleri (Translucent, Solid) olduğu gibi geri ver
                return buffer.getBuffer(type);
            };
        }

        // Efekt yoksa orijinal buffer'ı kullan
        return buffer;
    }
}