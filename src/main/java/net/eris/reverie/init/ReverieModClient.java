package net.eris.reverie.init;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.gui.StitchedScreen;
import net.eris.reverie.client.model.ResonantGongModel; // Eklendi
import net.eris.reverie.client.model.SpiritOrbModel;
import net.eris.reverie.client.renderer.ElderOliveHeartBlockRenderer;
import net.eris.reverie.client.renderer.ResonantGongRenderer; // Eklendi
import net.eris.reverie.client.renderer.layer.SpiritOrbPigLayer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent; // Eklendi
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.eris.reverie.client.MeditationClientHandler;

@Mod.EventBusSubscriber(
        modid = ReverieMod.MODID,
        bus   = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ReverieModClient {

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // 1. Block Entity Renderer Kayıtları
            BlockEntityRenderers.register(
                    ReverieModBlockEntities.ELDER_OLIVE_HEART.get(),
                    ElderOliveHeartBlockRenderer::new
            );

            // --- GONG RENDERER KAYDI (EKLENDİ) ---
            BlockEntityRenderers.register(
                    ReverieModBlockEntities.RESONANT_GONG.get(),
                    ResonantGongRenderer::new
            );
            // -------------------------------------

            // 2. Menü/Ekran Kayıtları
            MenuScreens.register(ReverieModMenus.STITCHED_MENU.get(), StitchedScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(MeditationClientHandler.MEDITATION_KEY);
    }



    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // Diğer modellerinin yanına ekle:
        event.registerLayerDefinition(SpiritOrbModel.LAYER_LOCATION, SpiritOrbModel::createBodyLayer);
    }
    // --------------------------------------------------------------------

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.AddLayers event) {
        // Oyunun kendi Domuz Renderer'ını alıyoruz
        LivingEntityRenderer<Pig, PigModel<Pig>> pigRenderer = event.getRenderer(EntityType.PIG);

        if (pigRenderer != null) {
            // Bizim katmanı ekliyoruz
            pigRenderer.addLayer(new SpiritOrbPigLayer(pigRenderer));
        }
    }

    // Shader kayıt kodları buradan kalktı, ClientEventHandlers veya başka yerde yönetiliyor.
}