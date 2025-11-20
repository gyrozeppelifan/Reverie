package net.eris.reverie.events;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.init.ReverieModMobEffects;
import net.eris.reverie.item.AncientCrossbowItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ReverieEvents {

    // --- 1. SAVAŞ MANTIKLARI: AZAMİ CANA GÖRE HASAR (TITAN SLAYER) ---
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        LivingEntity target = event.getEntity();

        if (attacker instanceof Player player) {
            ItemStack heldItem = player.getMainHandItem().getItem() instanceof AncientCrossbowItem ? player.getMainHandItem() : player.getOffhandItem();

            if (heldItem.getItem() instanceof AncientCrossbowItem) {

                float maxHealth = target.getMaxHealth();

                // Formül: Sabit 2 Hasar + Max Canın %10'u
                float desiredBaseDamage = 2.0F;
                float bonusDamage = maxHealth * 0.10F;

                // Hasar tavanı (50'den fazla vurmasın)
                if (bonusDamage > 50) bonusDamage = 50;

                float finalDamage = desiredBaseDamage + bonusDamage;

                // Ambush Bonusu (Görünmezken vurunca x1.5)
                AncientCrossbowItem crossbowItem = (AncientCrossbowItem) heldItem.getItem();
                if (crossbowItem.isClean(heldItem) && player.isInvisible()) {
                    finalDamage *= 1.5F;
                }

                event.setAmount(finalDamage);
            }
        }
    }

    // --- 2. GİZLİLİK MANTIKLARI (AI & HIZ) ---

    // Mobların entity'i görmesini engeller (AI Seviyesinde)
    @SubscribeEvent
    public static void onVisibilityCheck(LivingEvent.LivingVisibilityEvent event) {
        if (event.getEntity().hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get())) {
            event.modifyVisibility(0.0D);
        }
    }

    // Mobların hedef almasını engeller
    @SubscribeEvent
    public static void onTargetSet(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getNewTarget();
        if (newTarget != null && newTarget.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get())) {
            event.setCanceled(true);
        }
    }

    // Zaten kovalıyorsa takibi bıraktırır
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Mob mob) {
            LivingEntity target = mob.getTarget();
            if (target != null && target.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get())) {
                mob.setTarget(null);
            }
        }
    }

    // Hızlı Reload (2x Hız) - Machine Gun modunda hızlanmaya katkı sağlar
    @SubscribeEvent
    public static void onItemUseTick(LivingEntityUseItemEvent.Tick event) {
        if (event.getEntity().hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get()) &&
                event.getItem().getItem() instanceof AncientCrossbowItem) {
            event.setDuration(event.getDuration() - 1);
        }
    }
}