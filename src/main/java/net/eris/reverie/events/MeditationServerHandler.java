package net.eris.reverie.events;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.capability.MeditationProvider;
import net.eris.reverie.init.ReverieModEnchantments;
import net.eris.reverie.init.ReverieModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID)
public class MeditationServerHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        Player player = event.player;

        player.getCapability(MeditationProvider.PLAYER_MEDITATION).ifPresent(cap -> {
            if (cap.isMeditating()) {

                // 1. FİZİKSEL YÜKSELME
                player.setNoGravity(true);
                player.fallDistance = 0;
                double currentY = player.getY();
                double targetY = cap.getOriginY() + 3.0;
                double verticalMotion = 0;
                if (currentY < targetY) { verticalMotion = 0.08; }
                else { verticalMotion = 0; if (currentY > targetY + 0.1) { player.setPos(player.getX(), targetY, player.getZ()); } }
                player.setDeltaMovement(0, verticalMotion, 0);
                player.hasImpulse = true;
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 1, false, false, false));

                // --- BÜYÜ KONTROLLERİ ---
                ItemStack mainHand = player.getMainHandItem();
                ItemStack offHand = player.getOffhandItem();

                int guardLevel = getEnchantLevel(player, ReverieModEnchantments.SPIRIT_GUARD.get());
                int deflectionLevel = getEnchantLevel(player, ReverieModEnchantments.SPIRIT_DEFLECTION.get());
                int natureLevel = getEnchantLevel(player, ReverieModEnchantments.NATURES_BREATH.get());
                int peaceLevel = getEnchantLevel(player, ReverieModEnchantments.INNER_PEACE.get());

// A) SPIRIT GUARD (Alan İtme - Kalabalık Kontrolü)
                // Her 40 tickte bir (2 saniye) çalışır.
                if (guardLevel > 0 && player.tickCount % 40 == 0) {
                    // Menzil: Seviye 1 = 4 blok, Seviye 2 = 5 blok yarıçap
                    double range = 3.0 + guardLevel;

                    var enemies = player.level().getEntitiesOfClass(
                            net.minecraft.world.entity.monster.Monster.class,
                            // Yüksekliği (4.0) fazla tuttum ki aşağıda birikenleri de görsün
                            player.getBoundingBox().inflate(range, 4.0, range)
                    );

                    for (var enemy : enemies) {
                        // Düşmanın bize göre konumu
                        double dx = enemy.getX() - player.getX();
                        double dz = enemy.getZ() - player.getZ();

                        // Güç: Seviye 1 = 1.5, Seviye 2 = 2.0 (Bayağı sert iter)
                        double strength = 1.0 + (guardLevel * 0.5);

                        // Düşmanı merkezden dışarı doğru fırlat
                        enemy.knockback(strength, -dx, -dz);

                        // Başarımı tetikle (Düşmanı uçurunca)
                        grantAdvancement(player, "mind_over_matter");
                    }
                }

                // B) SPIRIT DEFLECTION (Ok/Mermi Saptırma)
                if (deflectionLevel > 0) {
                    double radius = 3.5;
                    List<Projectile> projectiles = player.level().getEntitiesOfClass(
                            Projectile.class,
                            player.getBoundingBox().inflate(radius)
                    );

                    for (Projectile projectile : projectiles) {
                        if (projectile.getOwner() != player) {
                            Vec3 motion = projectile.getDeltaMovement();
                            projectile.setDeltaMovement(motion.scale(-1.2).add(0, 0.2, 0));
                            projectile.yRotO += 180;
                        }
                    }
                }

                // C) NATURE'S BREATH (Ekin Büyütme)
                // 40 Tick = 2 Saniye
                if (natureLevel > 0 && player.tickCount % 40 == 0 && player.level() instanceof ServerLevel serverLevel) {
                    int range = 3 + (natureLevel * 2); // Menzil seviyeye göre artar
                    BlockPos playerPos = player.blockPosition();

                    // İSTEĞİN ÜZERİNE: Sabit 4 blok denemesi
                    int attempts = 4;

                    for (int i = 0; i < attempts; i++) {
                        int x = playerPos.getX() + serverLevel.random.nextInt(range * 2) - range;
                        int y = playerPos.getY() + serverLevel.random.nextInt(3) - 2;
                        int z = playerPos.getZ() + serverLevel.random.nextInt(range * 2) - range;
                        BlockPos targetPos = new BlockPos(x, y, z);
                        BlockState state = serverLevel.getBlockState(targetPos);

                        if (state.getBlock() instanceof BonemealableBlock growable) {
                            if (growable.isValidBonemealTarget(serverLevel, targetPos, state, false)) {
                                // %15 Şans (Dengeli)
                                if (serverLevel.random.nextFloat() < 0.15f) {
                                    growable.performBonemeal(serverLevel, serverLevel.random, targetPos, state);
                                    serverLevel.levelEvent(2005, targetPos, 0);
                                }
                            }
                        }
                    }
                }

                // D) INNER PEACE (Can Yenileme)
                cap.incrementTimer();
                int healInterval = 60 - (peaceLevel * 10);
                if (healInterval < 20) healInterval = 20;

                if (cap.getTimer() % healInterval == 0) {
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
                if (player.isNoGravity() && !player.getAbilities().flying) {
                    player.setNoGravity(false);
                }
                cap.resetTimer();
            }
        });
    }

    private static int getEnchantLevel(Player player, net.minecraft.world.item.enchantment.Enchantment enchant) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        int lvlMain = 0;
        int lvlOff = 0;

        if (main.is(ReverieModItems.BOAR_MONK_SEAL.get())) {
            lvlMain = main.getEnchantmentLevel(enchant);
        }
        if (off.is(ReverieModItems.BOAR_MONK_SEAL.get())) {
            lvlOff = off.getEnchantmentLevel(enchant);
        }

        return Math.max(lvlMain, lvlOff);
    }

    // Yardımcı Metod: Advancement Verici
    // Bunu dosyanın EN ALTINA, son kapatma parantezinden BİR ÖNCEKİ satıra yapıştır.
    private static void grantAdvancement(Player player, String advancementName) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            var advancement = serverPlayer.server.getAdvancements().getAdvancement(new net.minecraft.resources.ResourceLocation(ReverieMod.MODID, advancementName));
            if (advancement != null) {
                var progress = serverPlayer.getAdvancements().getOrStartProgress(advancement);
                if (!progress.isDone()) {
                    for (String criterion : progress.getRemainingCriteria()) {
                        serverPlayer.getAdvancements().award(advancement, criterion);
                    }
                }
            }
        }
    }

}