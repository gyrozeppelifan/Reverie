package net.eris.reverie.event;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.init.ReverieModBlocks;
import net.eris.reverie.init.ReverieModParticles; // BU IMPORT ÇOK ÖNEMLİ
import net.eris.reverie.particle.custom.WildFireParticle;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ReverieModClientEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Wild Torch'un şeffaf kısımlarını keserek renderla
            ItemBlockRenderTypes.setRenderLayer(ReverieModBlocks.WILD_TORCH.get(), RenderType.cutout());
        });
    }

    // Partikül Sağlayıcılarını Kaydetme Eventi
    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        // Kendi partikül tipimizi (ReverieModParticles.WILD_FIRE),
        // kendi fabrikamıza (WildFireParticle.Provider::new) bağlıyoruz.
        // Hatanın çıktığı satır burasıydı, şimdi düzelmiş olmalı.
        event.registerSpriteSet(ReverieModParticles.WILD_FIRE.get(), WildFireParticle.Provider::new);
    }
}