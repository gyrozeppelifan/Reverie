// src/main/java/net/eris/reverie/client/ClientEventHandlers.java
package net.eris.reverie.client;

import net.eris.reverie.client.renderer.layer.DrunkenOutlineLayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent.AddLayers;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(
    modid = "reverie",
    bus   = Mod.EventBusSubscriber.Bus.MOD,
    value = Dist.CLIENT
)
public class ClientEventHandlers {

    @SuppressWarnings({"rawtypes","unchecked"})
    @SubscribeEvent
    public static void onAddLayers(AddLayers event) {
        for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
            try {
                EntityType<LivingEntity> livingType = (EntityType<LivingEntity>) type;
                var rendererObj = event.getRenderer(livingType);
                if (!(rendererObj instanceof LivingEntityRenderer<?, ?>)) continue;

                LivingEntityRenderer<LivingEntity, ?> renderer =
                    (LivingEntityRenderer<LivingEntity, ?>) rendererObj;

                renderer.addLayer((RenderLayer) new DrunkenOutlineLayer<>(renderer));
            } catch (ClassCastException ignored) {
                // atla
            }
        }
    }
}
