package net.eris.reverie.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.init.ReverieModMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// BUS.FORGE OLDUĞUNA DİKKAT ET!
@Mod.EventBusSubscriber(modid = ReverieMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ReverieForgeClientEvents {

    private static final Map<UUID, Map<EquipmentSlot, ItemStack>> equipmentCache = new HashMap<>();

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get())) {
            // 1. ZIRH SAKLA
            Map<EquipmentSlot, ItemStack> savedGear = new HashMap<>();
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.ARMOR || slot.getType() == EquipmentSlot.Type.HAND) {
                    ItemStack currentItem = entity.getItemBySlot(slot);
                    if (!currentItem.isEmpty()) {
                        savedGear.put(slot, currentItem.copy());
                        entity.setItemSlot(slot, ItemStack.EMPTY);
                    }
                }
            }
            equipmentCache.put(entity.getUUID(), savedGear);

            // 2. TRANSPARANLIK
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.35F);
        }
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get())) {
            // 1. EŞYALARI GERİ VER
            if (equipmentCache.containsKey(entity.getUUID())) {
                Map<EquipmentSlot, ItemStack> savedGear = equipmentCache.get(entity.getUUID());
                for (Map.Entry<EquipmentSlot, ItemStack> entry : savedGear.entrySet()) {
                    entity.setItemSlot(entry.getKey(), entry.getValue());
                }
                equipmentCache.remove(entity.getUUID());
            }
            // 2. RENGİ SIFIRLA
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get())) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.35F);
        } else {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}