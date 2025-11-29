package net.eris.reverie.init;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.gui.StitchedScreen;
import net.eris.reverie.client.renderer.ElderOliveHeartBlockRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

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

            // 2. Menü/Ekran Kayıtları (BURAYI EKLEDİK)
            // Stitched menüsü açılınca StitchedScreen ekranını göster diyoruz.
            MenuScreens.register(ReverieModMenus.STITCHED_MENU.get(), StitchedScreen::new);
        });
    }

    // Shader kayıt kodları buradan kalktı, ClientEventHandlers veya başka yerde yönetiliyor.
}