package net.eris.reverie.events;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.capability.MeditationProvider;
import net.eris.reverie.init.ReverieModEnchantments;
import net.eris.reverie.init.ReverieModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper; // <-- Önemli Import
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
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

                // --- ADVANCEMENT TETİKLEME ---
                grantAdvancement(player, "start_meditating");

                // --- BÜYÜ SEVİYELERİ (Garanti Yöntem) ---
                int guardLevel = getEnchantLevel(player, ReverieModEnchantments.SPIRIT_GUARD.get());
                int deflectionLevel = getEnchantLevel(player, ReverieModEnchantments.SPIRIT_DEFLECTION.get());
                int natureLevel = getEnchantLevel(player, ReverieModEnchantments.NATURES_BREATH.get());
                int peaceLevel = getEnchantLevel(player, ReverieModEnchantments.INNER_PEACE.get());

                // 1. FİZİKSEL YÜKSELME
                player.setNoGravity(true);
                player.fallDistance = 0;

                double currentY = player.getY();

                // --- HEDEF YÜKSEKLİK (BUFFLI) ---
                // Base: 3.0 Blok.
                // Level başına +1.5 Blok (Bayağı fark eder)
                // Level 3 ise: 3.0 + 4.5 = 7.5 blok yükseğe çıkar.
                double targetY = cap.getOriginY() + 3.0 + (peaceLevel * 1.5);

                // --- HIZ ---
                double ascentSpeed = 0.05 + (peaceLevel * 0.07);

                double verticalMotion = 0;
                // Eğer hedef yüksekliğin altındaysak yukarı çek
                if (currentY < targetY) {
                    verticalMotion = ascentSpeed;

                    // Yükselirken ses efekti
                    if (player.tickCount % 5 == 0) {
                        float pitch = 0.5f + (peaceLevel * 0.5f);
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                net.minecraft.sounds.SoundEvents.BEACON_AMBIENT,
                                net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, pitch);
                    }
                } else {
                    verticalMotion = 0;
                    // Hedefi geçince veya ulaşınca sabitle
                    if (currentY > targetY + 0.1) {
                        player.setPos(player.getX(), targetY, player.getZ());
                        player.setDeltaMovement(0, 0, 0);
                    }
                }

                if (verticalMotion > 0) {
                    player.setDeltaMovement(0, verticalMotion, 0);
                    player.hasImpulse = true;
                }

                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 1, false, false, false));

                // A) SPIRIT GUARD
                if (guardLevel > 0 && player.tickCount % 40 == 0) {
                    double range = 3.0 + guardLevel;
                    var enemies = player.level().getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, player.getBoundingBox().inflate(range, 4.0, range));
                    for (var enemy : enemies) {
                        double dx = enemy.getX() - player.getX();
                        double dz = enemy.getZ() - player.getZ();
                        double strength = 1.0 + (guardLevel * 0.5);
                        enemy.knockback(strength, -dx, -dz);
                        grantAdvancement(player, "mind_over_matter");
                    }
                }

                // B) SPIRIT DEFLECTION
                if (deflectionLevel > 0) {
                    double radius = 3.5;
                    if (player.level() instanceof ServerLevel serverLevel) {
                        List<Projectile> projectiles = serverLevel.getEntitiesOfClass(Projectile.class, player.getBoundingBox().inflate(radius));

                        for (Projectile projectile : projectiles) {
                            if (projectile.getOwner() == player) continue;
                            if (projectile.onGround() || projectile.getDeltaMovement().lengthSqr() < 0.01) continue;

                            Vec3 playerToProj = projectile.position().subtract(player.position());
                            if (projectile.getDeltaMovement().dot(playerToProj) > 0) continue;

                            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM,
                                    projectile.getX(),
                                    projectile.getY() + projectile.getBbHeight() / 2.0,
                                    projectile.getZ(),
                                    1, 0, 0, 0, 0);

                            if (projectile instanceof AbstractHurtingProjectile hurtingProjectile) {
                                Vec3 repelVec = playerToProj.normalize().scale(2.0);
                                hurtingProjectile.setDeltaMovement(repelVec);
                                hurtingProjectile.hasImpulse = true;
                            } else {
                                Vec3 motion = projectile.getDeltaMovement();
                                projectile.setDeltaMovement(motion.scale(-1.2).add(0, 0.2, 0));
                                projectile.yRotO += 180;
                            }
                            grantAdvancement(player, "mind_over_matter");
                        }
                    }
                }

                // C) NATURE'S BREATH (OPTIMIZED)
                if (natureLevel > 0 && player.tickCount % 10 == 0 && player.level() instanceof ServerLevel serverLevel) {
                    int range = 8 + (natureLevel * 5);
                    BlockPos playerPos = player.blockPosition();
                    RandomSource random = serverLevel.random;

                    for(int p = 0; p < 2; p++) {
                        double px = player.getX() + (random.nextDouble() - 0.5) * range;
                        double py = player.getY() + random.nextDouble() * 3.0 - 1.0;
                        double pz = player.getZ() + (random.nextDouble() - 0.5) * range;
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 1, 0, 0, 0, 0);
                    }

                    int attempts = 40 + (natureLevel * 20);
                    for (int i = 0; i < attempts; i++) {
                        int x = playerPos.getX() + random.nextInt(range * 2 + 1) - range;
                        int y = playerPos.getY() - random.nextInt(6) + 2;
                        int z = playerPos.getZ() + random.nextInt(range * 2 + 1) - range;

                        BlockPos targetPos = new BlockPos(x, y, z);
                        BlockState state = serverLevel.getBlockState(targetPos);
                        BlockPos abovePos = targetPos.above();
                        BlockState aboveState = serverLevel.getBlockState(abovePos);

                        if (state.getBlock() instanceof BonemealableBlock growable) {
                            boolean isTarget = state.is(BlockTags.CROPS) ||
                                    state.is(BlockTags.SAPLINGS) ||
                                    state.getBlock() instanceof StemBlock ||
                                    state.getBlock() instanceof SweetBerryBushBlock ||
                                    state.getBlock() instanceof CocoaBlock ||
                                    state.getBlock() instanceof CropBlock;

                            if (isTarget && growable.isValidBonemealTarget(serverLevel, targetPos, state, false)) {
                                if (growable.isBonemealSuccess(serverLevel, random, targetPos, state)) {
                                    growable.performBonemeal(serverLevel, random, targetPos, state);
                                    if (random.nextInt(3) == 0) serverLevel.levelEvent(2005, targetPos, 0);
                                }
                            }
                        }
                        else if ((state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.PODZOL)) && aboveState.isAir()) {
                            if (random.nextFloat() < 0.1f) {
                                BlockState flowerState = getRandomFlower(random);
                                if (flowerState.canSurvive(serverLevel, abovePos)) {
                                    serverLevel.setBlockAndUpdate(abovePos, flowerState);
                                    if (random.nextInt(5) == 0) {
                                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, abovePos.getX() + 0.5, abovePos.getY() + 0.5, abovePos.getZ() + 0.5, 1, 0.1, 0.1, 0.1, 0.05);
                                    }
                                }
                            }
                        }
                    }
                }

                // D) INNER PEACE
                cap.incrementTimer();
                int healInterval = 60 - (peaceLevel * 10);
                if (healInterval < 20) healInterval = 20;
                if (cap.getTimer() % healInterval == 0) {
                    if (player.getHealth() < player.getMaxHealth()) player.heal(1.0F);
                    else if (player.getAbsorptionAmount() < 10.0F) player.setAbsorptionAmount(player.getAbsorptionAmount() + 1.0F);
                }
            } else {
                if (player.isNoGravity() && !player.getAbilities().flying) player.setNoGravity(false);
                cap.resetTimer();
            }
        });
    }

    private static BlockState getRandomFlower(RandomSource random) {
        Block[] flowers = {
                Blocks.DANDELION, Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM,
                Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP,
                Blocks.WHITE_TULIP, Blocks.PINK_TULIP, Blocks.OXEYE_DAISY,
                Blocks.CORNFLOWER, Blocks.LILY_OF_THE_VALLEY
        };
        return flowers[random.nextInt(flowers.length)].defaultBlockState();
    }

    // --- ENCHANT SEVİYESİNİ OKUMA (YENİLENMİŞ - HATASIZ) ---
    private static int getEnchantLevel(Player player, net.minecraft.world.item.enchantment.Enchantment enchant) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        // ARTIK EŞYA KONTROLÜ YOK!
        // Elindeki herhangi bir eşyada bu büyü varsa kabul ediyoruz.
        // Bu sayede "Mühürü tanıyamadım" hatası ortadan kalkar.
        int lvlMain = EnchantmentHelper.getItemEnchantmentLevel(enchant, main);
        int lvlOff = EnchantmentHelper.getItemEnchantmentLevel(enchant, off);

        return Math.max(lvlMain, lvlOff);
    }

    private static void grantAdvancement(Player player, String advancementName) {
        if (player instanceof ServerPlayer serverPlayer) {
            var advancement = serverPlayer.server.getAdvancements().getAdvancement(new net.minecraft.resources.ResourceLocation(ReverieMod.MODID, advancementName));
            if (advancement != null) {
                var progress = serverPlayer.getAdvancements().getOrStartProgress(advancement);
                if (!progress.isDone()) {
                    for (String criterion : progress.getRemainingCriteria()) serverPlayer.getAdvancements().award(advancement, criterion);
                }
            }
        }
    }
}