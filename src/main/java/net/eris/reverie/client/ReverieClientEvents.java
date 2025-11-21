package net.eris.reverie.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.renderer.layer.AncientCloakLayer;
import net.eris.reverie.client.renderer.layer.DrunkenOutlineLayer;
import net.eris.reverie.client.renderer.layer.DrunkenTrailLayer;
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
    public static ShaderInstance drunkenRageShader;

    // --- 1. SHADER KAYDI ---
    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        // Goblin Flag
        try {
            event.registerShader(new ShaderInstance(
                    event.getResourceProvider(),
                    new ResourceLocation(ReverieMod.MODID, "goblin_radiation"),
                    DefaultVertexFormat.POSITION_COLOR_TEX
            ), shaderInstance -> goblinFlagGlowShader = shaderInstance);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Ancient Cloak
        try {
            event.registerShader(new ShaderInstance(
                    event.getResourceProvider(),
                    new ResourceLocation(ReverieMod.MODID, "ancient_cloak_aura"),
                    DefaultVertexFormat.POSITION_COLOR_TEX
            ), shaderInstance -> ancientCloakShader = shaderInstance);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Drunken Rage (GUI Shader)
        try {
            event.registerShader(new ShaderInstance(
                    event.getResourceProvider(),
                    new ResourceLocation(ReverieMod.MODID, "drunken_rage"),
                    DefaultVertexFormat.POSITION_TEX
            ), shaderInstance -> drunkenRageShader = shaderInstance);
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

    // --- 3. KATMAN (LAYER) KAYDI ---
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.AddLayers event) {

        // A) OYUNCU SKINLERI
        for (String skinType : event.getSkins()) {
            var renderer = event.getSkin(skinType);
            if (renderer instanceof LivingEntityRenderer) {
                @SuppressWarnings("unchecked")
                LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> livingRenderer =
                        (LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>) renderer;

                // Ancient Cloak
                livingRenderer.addLayer(new AncientCloakLayer<>(livingRenderer));
                // Drunken Rage (Varsa)
                livingRenderer.addLayer(new DrunkenOutlineLayer<>(livingRenderer));
                livingRenderer.addLayer(new DrunkenTrailLayer<>(livingRenderer));
            }
        }

        // B) TÜM MOBLAR
        for (EntityType<?> entityType : ForgeRegistries.ENTITY_TYPES) {
            try {
                // HATA ÇÖZÜMÜ: Unchecked Cast ile türü zorluyoruz.
                // EntityType<?> --> EntityType<? extends LivingEntity>
                @SuppressWarnings("unchecked")
                EntityType<? extends LivingEntity> livingEntityType = (EntityType<? extends LivingEntity>) entityType;

                var renderer = event.getRenderer(livingEntityType);

                if (renderer instanceof LivingEntityRenderer) {
                    @SuppressWarnings("unchecked")
                    LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> livingRenderer =
                            (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) renderer;

                    // Ancient Cloak
                    livingRenderer.addLayer(new AncientCloakLayer<>(livingRenderer));
                    // Drunken Rage (Varsa)
                    livingRenderer.addLayer(new DrunkenOutlineLayer<>(livingRenderer));
                    livingRenderer.addLayer(new DrunkenTrailLayer<>(livingRenderer));
                }
            } catch (Exception e) {
                // LivingEntity olmayan (Ok, Tekne vs.) rendererlar hata verebilir, önemsiz.
            }
        }
    }
}