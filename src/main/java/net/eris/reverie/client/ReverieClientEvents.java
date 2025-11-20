package net.eris.reverie.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.renderer.layer.AncientCloakLayer; // Layer sınıfını import ettik
import net.eris.reverie.init.ReverieModItems;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent; // Layer kaydı için gerekli
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ReverieClientEvents {

    // 1. Goblin Flag Shader Referansı
    public static ShaderInstance goblinFlagGlowShader;

    // 2. Ancient Cloak (Aqua Aura) Shader Referansı (YENİ)
    public static ShaderInstance ancientCloakShader;

    // --- SHADER KAYITLARI ---
    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {

        // A) Goblin Flag Shader (Zaten vardı)
        try {
            event.registerShader(new ShaderInstance(
                    event.getResourceProvider(),
                    new ResourceLocation(ReverieMod.MODID, "goblin_radiation"),
                    DefaultVertexFormat.POSITION_COLOR_TEX
            ), shaderInstance -> {
                goblinFlagGlowShader = shaderInstance;
                System.out.println("Reverie: Goblin Radiation Shader yüklendi.");
            });
        } catch (IOException e) {
            System.err.println("Reverie: Goblin Shader yüklenirken hata oluştu!");
            e.printStackTrace();
        }

        // B) Ancient Cloak (Aqua Aura) Shader (YENİ EKLENDİ)
        try {
            event.registerShader(new ShaderInstance(
                    event.getResourceProvider(),
                    new ResourceLocation(ReverieMod.MODID, "ancient_cloak_aura"),
                    DefaultVertexFormat.POSITION_COLOR_TEX
            ), shaderInstance -> {
                ancientCloakShader = shaderInstance;
                System.out.println("Reverie: Ancient Cloak Aura Shader yüklendi.");
            });
        } catch (IOException e) {
            System.err.println("Reverie: Ancient Cloak Shader yüklenirken hata oluştu!");
            e.printStackTrace();
        }
    }

    // --- ITEM ANİMASYONLARI ---
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {

            // Ancient Crossbow: Clean Property
            ItemProperties.register(ReverieModItems.ANCIENT_CROSSBOW.get(),
                    new ResourceLocation(ReverieMod.MODID, "clean"),
                    (stack, level, entity, id) -> stack.hasTag() && stack.getTag().getBoolean("IsClean") ? 1.0F : 0.0F
            );

            // Ancient Crossbow: Recoil Property
            ItemProperties.register(ReverieModItems.ANCIENT_CROSSBOW.get(),
                    new ResourceLocation(ReverieMod.MODID, "recoil"),
                    (stack, level, entity, id) -> stack.hasTag() && stack.getTag().getInt("RecoilTicks") > 0 ? 1.0F : 0.0F
            );
        });
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.AddLayers event) {
        for (String skinType : event.getSkins()) {
            var renderer = event.getSkin(skinType);

            // Renderer'ın LivingEntityRenderer olup olmadığını kontrol et (Güvenlik için)
            if (renderer instanceof LivingEntityRenderer) {

                // DÜZELTME BURADA:
                // RenderLayerParent yerine LivingEntityRenderer'a cast ediyoruz.
                // Çünkü addLayer metodu burada!
                LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> livingRenderer =
                        (LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>) renderer;

                // Katmanı ekle
                livingRenderer.addLayer(new AncientCloakLayer(livingRenderer));
            }
        }
    }
}