package net.eris.reverie.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.renderer.layer.AncientCloakLayer;
import net.eris.reverie.init.ReverieModItems;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
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

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(
                    event.getResourceProvider(),
                    new ResourceLocation(ReverieMod.MODID, "goblin_radiation"),
                    DefaultVertexFormat.POSITION_COLOR_TEX
            ), shaderInstance -> {
                goblinFlagGlowShader = shaderInstance;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            event.registerShader(new ShaderInstance(
                    event.getResourceProvider(),
                    new ResourceLocation(ReverieMod.MODID, "ancient_cloak_aura"),
                    DefaultVertexFormat.POSITION_COLOR_TEX
            ), shaderInstance -> {
                ancientCloakShader = shaderInstance;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    // --- DÜZELTİLMİŞ KATMAN KAYDI ---
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.AddLayers event) {

        // 1. OYUNCULAR
        for (String skinType : event.getSkins()) {
            var renderer = event.getSkin(skinType);
            // Hata 1 Çözümü: Pattern matching (instanceof ... renderer) yerine klasik if ve cast kullanıyoruz.
            if (renderer instanceof LivingEntityRenderer) {
                LivingEntityRenderer livingRenderer = (LivingEntityRenderer) renderer;
                livingRenderer.addLayer(new AncientCloakLayer(livingRenderer));
            }
        }

        // 2. MOBLAR
        for (EntityType<?> entityType : ForgeRegistries.ENTITY_TYPES) {
            try {
                // Hata 2 Çözümü: EntityType<?> türünü zorla (EntityType<? extends LivingEntity>) türüne çeviriyoruz.
                // Bu "Unchecked Cast" uyarısı verir ama çalışır.
                EntityType<? extends LivingEntity> livingType = (EntityType<? extends LivingEntity>) entityType;

                var renderer = event.getRenderer(livingType);
                if (renderer instanceof LivingEntityRenderer) {
                    LivingEntityRenderer livingRenderer = (LivingEntityRenderer) renderer;
                    livingRenderer.addLayer(new AncientCloakLayer(livingRenderer));
                }
            } catch (Exception e) {
                // Eğer entity bir LivingEntity değilse (örn: Ok, Tekne) buraya düşer, sorun yok devam et.
            }
        }
    }
}