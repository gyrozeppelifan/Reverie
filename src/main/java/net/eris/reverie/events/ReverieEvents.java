package net.eris.reverie.events;

import com.mojang.blaze3d.systems.RenderSystem;
import net.eris.reverie.init.ReverieModMobEffects;
import net.eris.reverie.item.AncientCrossbowItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent; // LivingHurtEvent
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "reverie", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ReverieEvents {

    // --- 1. SAVAŞ MANTIKLARI: AZAMİ CANA GÖRE HASAR (TITAN SLAYER) ---

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        LivingEntity target = event.getEntity();

        // Sadece ok atan bir oyuncuysa devam et
        if (attacker instanceof Player player) {
            ItemStack heldItem = player.getMainHandItem().getItem() instanceof AncientCrossbowItem ? player.getMainHandItem() : player.getOffhandItem();

            if (heldItem.getItem() instanceof AncientCrossbowItem) {

                // Titan Slayer Mantığı
                float maxHealth = target.getMaxHealth();

                // 1. ANA HASAR: 2.0F olarak zorla (1 Kalp)
                float desiredBaseDamage = 1.0F;

                // 2. BONUS HESABI: Azami Canın %10'u kadar bonus hasar
                float bonusDamage = maxHealth * 0.2F;
                if (bonusDamage > 50) bonusDamage = 50;

                // Yeni Hasar = Sabit Base + Bonus
                float finalDamage = desiredBaseDamage + bonusDamage;

                // Hasarın negatif olmasını engelle
                if (finalDamage < 0) finalDamage = 0;

                // Ek Özellik: Gizliyken Vurma (Ambush)
                AncientCrossbowItem crossbowItem = (AncientCrossbowItem) heldItem.getItem();
                if (crossbowItem.isClean(heldItem) && player.isInvisible()) {
                    // Görünmezken vurunca %50 ekstra hasar (Ambush bonusu)
                    finalDamage *= 1.2F;
                }

                // Hasarı uygula
                event.setAmount(finalDamage);
            }
        }
    }


    // --- 2. GİZLİLİK MANTIKLARI (AI, RELOAD, VISUAL) ---

    @SubscribeEvent
    public static void onVisibilityCheck(LivingEvent.LivingVisibilityEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get())) {
                event.modifyVisibility(0.0D);
            }
        }
    }

    @SubscribeEvent
    public static void onTargetSet(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getNewTarget();
        if (newTarget instanceof Player player && player.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Mob mob) {
            LivingEntity target = mob.getTarget();
            if (target instanceof Player player && player.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get())) {
                mob.setTarget(null);
            }
        }
    }

    @SubscribeEvent
    public static void onItemUseTick(LivingEntityUseItemEvent.Tick event) {
        if (event.getEntity() instanceof Player player) {
            if (player.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get()) &&
                    event.getItem().getItem() instanceof AncientCrossbowItem) {
                event.setDuration(event.getDuration() - 1);
            }
        }
    }

    // --- 3. GÖRSEL RENDER (CLIENT) ---

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        if (event.getEntity() instanceof Player player) {
            if (player.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get())) {
                // Hayalet Modu Başlat
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.35F);
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        if (event.getEntity() instanceof Player player) {
            if (player.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get())) {
                // Hayalet Modu Bitir / Rengi Sıfırla
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.disableBlend();
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onRenderHand(net.minecraftforge.client.event.RenderHandEvent event) {
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