// src/main/java/net/eris/reverie/handlers/ReverieFeatureHandler.java
package net.eris.reverie.handlers;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.eris.reverie.registry.ReverieFeatures;

@Mod.EventBusSubscriber(modid = "reverie", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ReverieFeatureHandler {
    static {
        // Artık doğru registry (ForgeRegistries.FEATURES) kullanılıyor
        ReverieFeatures.FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
