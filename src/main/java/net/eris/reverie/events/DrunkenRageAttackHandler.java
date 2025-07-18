package net.eris.reverie.events;

import net.eris.reverie.entity.DrunkardEntity;
import net.eris.reverie.init.ReverieModMobEffects;
import net.eris.reverie.init.ReverieModParticleTypes;
import net.eris.reverie.init.ReverieModSounds;
import net.eris.reverie.init.ReverieModItems;
import net.eris.reverie.item.BoozeBottleItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "reverie")
public class DrunkenRageAttackHandler {
    private static final String READY_TAG = "drunken_ready";

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        DamageSource src = event.getSource();
        Entity direct = src.getEntity();
        if (!(direct instanceof LivingEntity attacker)) return;
        if (!(attacker.level() instanceof ServerLevel server)) return;

        // Sadece Drunken Rage efekti altındaysa ve flag set’liyse devam
        if (!attacker.hasEffect(ReverieModMobEffects.DRUNKEN_RAGE.get())) return;
        if (!attacker.getPersistentData().getBoolean(READY_TAG)) return;

        // 1) Hasarı katla, efekt ve flag temizle
        event.setAmount(event.getAmount() * 2f);
        attacker.removeEffect(ReverieModMobEffects.DRUNKEN_RAGE.get());
        attacker.getPersistentData().putBoolean(READY_TAG, false);

        // Efektleri sadece Drunkard veya elinde Booze Bottle tutanlar için oynat
        boolean playFx = attacker instanceof DrunkardEntity
                      || attacker.getMainHandItem().getItem() instanceof BoozeBottleItem;
        if (playFx) {
            double cx = attacker.getX();
            double cy = attacker.getY() + attacker.getBbHeight() * 0.5;
            double cz = attacker.getZ();

            // 2) Kırılma sesi
            server.playSound(
                null, cx, attacker.getY(), cz,
                ReverieModSounds.BOTTLE_CRASH.get(),
                SoundSource.NEUTRAL,
                1.0F, 1.0F
            );

            // 3) Booze bubbles çemberi
            int bubblePoints = 8;
            double radius = 1.0, y0 = attacker.getY() + attacker.getBbHeight() * 0.25;
            for (int i = 0; i < bubblePoints; i++) {
                double angle = 2 * Math.PI * i / bubblePoints;
                double dx = Math.cos(angle) * radius;
                double dz = Math.sin(angle) * radius;
                server.sendParticles(
                    ReverieModParticleTypes.BOOZE_BUBBLES.get(),
                    cx + dx, y0, cz + dz,
                    1, 0, 0, 0, 0.02
                );
            }

            // 4) Glass shards patlaması
            server.sendParticles(
                ReverieModParticleTypes.GLASS_SHARDS.get(),
                cx, cy, cz,
                12,      // shardCount
                0.5, 0.5, 0.5,
                0.15
            );
        }

        // 5) DrunkardEntity için flag set et
        if (attacker instanceof DrunkardEntity drunkard) {
            drunkard.getEntityData().set(DrunkardEntity.DATA_hasBrokenBottle, true);
        }

        // 6) Eğer Player ise main hand’deki booze bottle’ı kır
        if (attacker instanceof Player player) {
            ItemStack main = player.getMainHandItem();
            if (main.getItem() instanceof BoozeBottleItem) {
                player.setItemInHand(
                    InteractionHand.MAIN_HAND,
                    new ItemStack(ReverieModItems.BROKEN_BOOZE_BOTTLE.get())
                );
            }
        }
    }
}
