package net.eris.reverie.events;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.capability.MeditationProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID)
public class MeditationServerHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        Player player = event.player;

        player.getCapability(MeditationProvider.PLAYER_MEDITATION).ifPresent(cap -> {
            if (cap.isMeditating()) {

                // 1. FİZİKSEL YÜKSELME VE KİLİTLEME
                player.setNoGravity(true);
                player.fallDistance = 0;

                double currentY = player.getY();
                double targetY = cap.getOriginY() + 3.0; // Hedef Yükseklik

                // --- HAREKET KİLİTLEME (YENİ) ---
                // X ve Z hızını zorla 0 yapıyoruz (Yatayda kımıldayamaz)
                double verticalMotion = 0;

                // Hedefe ulaşmadıysa yukarı it
                if (currentY < targetY) {
                    verticalMotion = 0.08;
                }
                // Hedefi geçtiyse sabitle
                else {
                    verticalMotion = 0;
                    if (currentY > targetY + 0.1) {
                        player.setPos(player.getX(), targetY, player.getZ());
                    }
                }

                // DeltaMovement'i güncelle: X=0, Y=Hesaplanan, Z=0
                player.setDeltaMovement(0, verticalMotion, 0);
                player.hasImpulse = true; // Client'a zorla gönder

                // 2. GÜVENLİK VE BUFF
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 1, false, false, false));

                // 3. CAN YENİLEME
                cap.incrementTimer();
                if (cap.getTimer() % 60 == 0) {
                    if (player.getHealth() < player.getMaxHealth()) {
                        player.heal(1.0F);
                    } else {
                        float currentAbsorb = player.getAbsorptionAmount();
                        if (currentAbsorb < 10.0F) {
                            player.setAbsorptionAmount(currentAbsorb + 1.0F);
                        }
                    }
                }
            } else {
                // Bitiş
                if (player.isNoGravity() && !player.getAbilities().flying) {
                    player.setNoGravity(false);
                }
                cap.resetTimer();
            }
        });
    }
}