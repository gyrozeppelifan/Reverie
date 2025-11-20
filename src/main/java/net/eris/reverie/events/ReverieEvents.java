package net.eris.reverie.events;

import com.mojang.blaze3d.systems.RenderSystem;
import net.eris.reverie.init.ReverieModMobEffects;
import net.eris.reverie.item.AncientCrossbowItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "reverie", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ReverieEvents {

    // --- 1. SAVAŞ: TITAN SLAYER HASARI ---
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        LivingEntity target = event.getEntity();

        if (attacker instanceof Player player) {
            ItemStack heldItem = player.getMainHandItem().getItem() instanceof AncientCrossbowItem ? player.getMainHandItem() : player.getOffhandItem();

            if (heldItem.getItem() instanceof AncientCrossbowItem) {
                float maxHealth = target.getMaxHealth();
                float desiredBaseDamage = 2.0F;
                float bonusDamage = maxHealth * 0.10F;
                if (bonusDamage > 50) bonusDamage = 50;

                float finalDamage = desiredBaseDamage + bonusDamage;
                AncientCrossbowItem crossbowItem = (AncientCrossbowItem) heldItem.getItem();
                if (crossbowItem.isClean(heldItem) && player.isInvisible()) {
                    finalDamage *= 1.5F;
                }
                event.setAmount(finalDamage);
            }
        }
    }

    // --- 2. GİZLİLİK: AI VE TARGET (Sunucu Tarafı) ---

    @SubscribeEvent
    public static void onVisibilityCheck(LivingEvent.LivingVisibilityEvent event) {
        if (event.getEntity().hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get())) {
            event.modifyVisibility(0.0D); // Moblar göremez
        }
    }

    @SubscribeEvent
    public static void onTargetSet(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getNewTarget();
        if (newTarget != null && newTarget.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get())) {
            event.setCanceled(true); // Hedef almayı iptal et
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Mob mob) {
            LivingEntity target = mob.getTarget();
            if (target != null && target.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get())) {
                mob.setTarget(null); // Takipten vazgeç
            }
        }
    }

    @SubscribeEvent
    public static void onItemUseTick(LivingEntityUseItemEvent.Tick event) {
        if (event.getEntity().hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get()) &&
                event.getItem().getItem() instanceof AncientCrossbowItem) {
            event.setDuration(event.getDuration() - 1); // Hızlı reload
        }
    }

    // --- 3. GÖRSEL: RENDER HACK (ZIRH SAKLAMA + TRANSPARANLIK) ---

    // Eşyaları geçici olarak saklamak için depo (Cache)
    private static final Map<UUID, Map<EquipmentSlot, ItemStack>> equipmentCache = new HashMap<>();

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();

        if (entity.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get())) {

            // A) SOYUNMA İŞLEMİ (Strip Hack)
            // setInvisible yerine, zırhları ve eldeki eşyaları söküyoruz.
            // Böylece entity görünür kalıyor (Render çalışıyor) ama üstü boş oluyor.

            Map<EquipmentSlot, ItemStack> savedGear = new HashMap<>();
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                // Sadece Zırh ve Eller
                if (slot.getType() == EquipmentSlot.Type.ARMOR || slot.getType() == EquipmentSlot.Type.HAND) {
                    ItemStack currentItem = entity.getItemBySlot(slot);
                    if (!currentItem.isEmpty()) {
                        savedGear.put(slot, currentItem.copy()); // Kopyala ve sakla
                        entity.setItemSlot(slot, ItemStack.EMPTY); // Slotu boşalt
                    }
                }
            }
            // Depoya at
            equipmentCache.put(entity.getUUID(), savedGear);

            // B) TRANSPARANLIK (VÜCUT İÇİN)
            // Artık entity görünür olduğu için bu çalışacak!
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.35F); // %35 Görünür (Hayalet)
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();

        if (entity.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get())) {

            // A) GİYİNME İŞLEMİ (Restore)
            // Render bitti, eşyaları geri ver ki oyun bozulmasın
            if (equipmentCache.containsKey(entity.getUUID())) {
                Map<EquipmentSlot, ItemStack> savedGear = equipmentCache.get(entity.getUUID());
                for (Map.Entry<EquipmentSlot, ItemStack> entry : savedGear.entrySet()) {
                    entity.setItemSlot(entry.getKey(), entry.getValue());
                }
                equipmentCache.remove(entity.getUUID());
            }

            // B) RENKLERİ SIFIRLA
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        }
    }

    // FPS Modu (Sadece kendi elin için)
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
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