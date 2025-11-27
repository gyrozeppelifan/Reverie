package net.eris.reverie.init;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.eris.reverie.init.ReverieModBlockEntities;
import net.eris.reverie.client.renderer.ElderOliveHeartBlockRenderer;
import net.eris.reverie.ReverieMod;

@Mod.EventBusSubscriber(
        modid = ReverieMod.MODID,
        bus   = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ReverieModClient {

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BlockEntityRenderers.register(
                    ReverieModBlockEntities.ELDER_OLIVE_HEART.get(),
                    ElderOliveHeartBlockRenderer::new
            );
        });
    }

    // Shader kayıt kodları BURADAN KALKTI.
}