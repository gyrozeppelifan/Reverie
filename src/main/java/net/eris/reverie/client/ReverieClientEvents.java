package net.eris.reverie.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.renderer.layer.AncientCloakLayer;
import net.eris.reverie.init.ReverieModItems;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ReverieClientEvents {

    public static ShaderInstance goblinFlagGlowShader;
    public static ShaderInstance ancientCloakShader;

    // --- 1. SHADER KAYDI ---
    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(
                    event.getResourceProvider(),
                    new ResourceLocation(ReverieMod.MODID, "goblin_radiation"),
                    DefaultVertexFormat.POSITION_COLOR_TEX
            ), shaderInstance -> goblinFlagGlowShader = shaderInstance);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            event.registerShader(new ShaderInstance(
                    event.getResourceProvider(),
                    new ResourceLocation(ReverieMod.MODID, "ancient_cloak_aura"),
                    DefaultVertexFormat.POSITION_COLOR_TEX
            ), shaderInstance -> ancientCloakShader = shaderInstance);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- 2. ITEM ANİMASYONLARI ---
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ReverieModItems.ANCIENT_CROSSBOW.get(),
                    new ResourceLocation(ReverieMod.MODID, "clean"),
                    (stack, level, entity, id) -> stack.hasTag() && stack.getTag().getBoolean("IsClean") ? 1.0F : 0.0F
            );

            ItemProperties.register(ReverieModItems.ANCIENT_CROSSBOW.get(),
                    new ResourceLocation(ReverieMod.MODID, "recoil"),
                    (stack, level, entity, id) -> stack.hasTag() && stack.getTag().getInt("RecoilTicks") > 0 ? 1.0F : 0.0F
            );
        });
    }

    // --- 3. KATMAN KAYDI (DÜZELTİLDİ) ---
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.AddLayers event) {

        // A) OYUNCU SKINLERI
        for (String skinType : event.getSkins()) {
            var renderer = event.getSkin(skinType);

            // HATA 1 ÇÖZÜMÜ: Pattern matching (instanceof X x) yerine klasik cast kullanıyoruz.
            if (renderer instanceof LivingEntityRenderer) {
                LivingEntityRenderer livingRenderer = (LivingEntityRenderer) renderer;
                livingRenderer.addLayer(new AncientCloakLayer(livingRenderer));
            }
        }

        // B) TÜM MOBLAR
        for (EntityType<?> entityType : ForgeRegistries.ENTITY_TYPES) {
            try {
                // HATA 2 ÇÖZÜMÜ: "Unchecked Cast" ile türü zorla LivingEntity yapıyoruz.
                // Bu sayede getRenderer() metodu hata vermiyor.
                @SuppressWarnings("unchecked")
                EntityType<? extends LivingEntity> livingType = (EntityType<? extends LivingEntity>) entityType;

                var renderer = event.getRenderer(livingType);

                if (renderer instanceof LivingEntityRenderer) {
                    @SuppressWarnings("unchecked")
                    LivingEntityRenderer livingRenderer = (LivingEntityRenderer) renderer;

                    livingRenderer.addLayer(new AncientCloakLayer(livingRenderer));
                }
            } catch (Exception e) {
                // Ok, Tekne gibi LivingEntity olmayan şeyler buraya düşer, sorun yok.
            }
        }
    }
}