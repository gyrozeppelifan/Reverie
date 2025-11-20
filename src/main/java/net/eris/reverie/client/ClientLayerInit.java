package net.eris.reverie.client;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.render.layer.DrunkenRageGlowLayer;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientLayerInit {
    private ClientLayerInit() {}

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers e) {
        // 1) Oyuncular
        for (String skin : e.getSkins()) {
            PlayerRenderer pr = e.getSkin(skin);
            pr.addLayer(new DrunkenRageGlowLayer<>(pr));
        }

        // 2) Diğer tüm LivingEntity türleri (player hariç)
        for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
            Class<?> base = type.getBaseClass();
            if (base == null || !LivingEntity.class.isAssignableFrom(base)) continue;
            if (type == EntityType.PLAYER) continue;

            // tür LivingEntity olduğu garanti, güvenli cast
            addToLiving(e, (EntityType<? extends LivingEntity>) type);
        }
    }

    // getRenderer imzasına uygun: <? extends LivingEntity>
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static <T extends LivingEntity>
    void addToLiving(EntityRenderersEvent.AddLayers e, EntityType<? extends T> type) {
        var r = e.getRenderer(type);                    // LivingEntityRenderer<T, ? extends EntityModel<T>>
        if (!(r instanceof LivingEntityRenderer<?, ?>)) return;

        // wildcard-capture sorununu raw ile aş
        LivingEntityRenderer renderer = (LivingEntityRenderer) r;
        renderer.addLayer(new DrunkenRageGlowLayer(renderer));
    }
}
