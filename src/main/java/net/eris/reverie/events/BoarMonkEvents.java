package net.eris.reverie.events;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.entity.HogEntity;
import net.eris.reverie.init.ReverieModAttributes;
import net.eris.reverie.init.ReverieModEntities;
import net.eris.reverie.init.ReverieModItems;
import net.eris.reverie.init.ReverieModParticleTypes; // Senin modunun partikülleri
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID)
public class BoarMonkEvents {

    public static final String TAG_TRANSFORMING = "ReverieTransformingTick";
    public static final int RITUAL_DURATION = 100; // 5 Saniye sürsün (Daha kısa ve öz)

    // 1. DOMUZ DOĞUMU (Şans Faktörü)
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Pig pig && !event.getLevel().isClientSide) {
            if (ReverieModAttributes.SPIRITUALITY == null) return;
            AttributeInstance spirituality = pig.getAttribute(ReverieModAttributes.SPIRITUALITY);
            if (spirituality != null && spirituality.getValue() == 0.0D) {
                if (event.getLevel().random.nextFloat() < 0.5f) {
                    spirituality.setBaseValue(1.0D);
                }
            }
        }
    }

    // 2. DÖNÜŞÜM SÜRECİ (ORBITING ORBS)
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Pig pig) {

            CompoundTag data = pig.getPersistentData();

            // --- DÖNÜŞÜM RİTÜELİ ---
            if (data.contains(TAG_TRANSFORMING)) {
                int timer = data.getInt(TAG_TRANSFORMING);

                if (timer > 0) {
                    // Hareket Kilidi ve Hafif Yükselme
                    pig.getNavigation().stop();
                    pig.setDeltaMovement(0, 0.02, 0); // Çok hafif yükselme
                    pig.setNoGravity(true);

                    // --- YÖRÜNGE PARTİKÜLLERİ (SERVER SIDE) ---
                    if (!pig.level().isClientSide) {
                        ServerLevel level = (ServerLevel) pig.level();

                        // Dönüş hızı (Zamanla artabilir)
                        double speed = (RITUAL_DURATION - timer) * 0.2;
                        double radius = 1.2; // Domuzdan ne kadar uzakta dönecekleri

                        // 3 Tane Orb Çiziyoruz (120 derece arayla)
                        for (int i = 0; i < 3; i++) {
                            // Açıyı hesapla: (Hız) + (Ofset: 0, 120, 240 derece)
                            double angle = speed + (i * (Math.PI * 2) / 3);

                            double px = pig.getX() + Math.cos(angle) * radius;
                            double pz = pig.getZ() + Math.sin(angle) * radius;
                            double py = pig.getY() + 0.5 + Math.sin(speed * 0.5) * 0.2; // Hafif aşağı yukarı yapsın

                            // Partikülü Yolla (Hızı 0 olsun ki olduğu yerde kalsın)
                            level.sendParticles(ReverieModParticleTypes.SPIRIT_ORB.get(),
                                    px, py, pz, 1, 0, 0, 0, 0);

                            // Arkasında iz bıraksın diye End Rod da ekleyelim (Opsiyonel, şık durur)
                            if (timer % 3 == 0) {
                                level.sendParticles(ParticleTypes.END_ROD, px, py, pz, 1, 0, 0, 0, 0.01);
                            }
                        }

                        // Merkezden çıkan hafif duman
                        if (timer % 5 == 0) {
                            level.sendParticles(ParticleTypes.PORTAL, pig.getX(), pig.getY() + 0.5, pig.getZ(), 1, 0.2, 0.2, 0.2, 0.1);
                        }
                    }

                    // Sayacı düşür
                    data.putInt(TAG_TRANSFORMING, timer - 1);

                } else {
                    // --- FİNAL: PATLAMA VE HOG ---
                    if (!pig.level().isClientSide) {
                        ServerLevel level = (ServerLevel) pig.level();

                        // Patlama Efektleri
                        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pig.getX(), pig.getY(), pig.getZ(), 1, 0, 0, 0, 0);
                        level.sendParticles(ParticleTypes.FLASH, pig.getX(), pig.getY(), pig.getZ(), 1, 0, 0, 0, 0);

                        // Ses
                        level.playSound(null, pig.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0f, 1.0f);
                        level.playSound(null, pig.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.NEUTRAL, 1.0f, 1.0f);

                        // Hog Yarat
                        HogEntity hog = ReverieModEntities.HOG.get().create(level);
                        if (hog != null) {
                            hog.moveTo(pig.getX(), pig.getY(), pig.getZ(), pig.getYRot(), pig.getXRot());
                            hog.yBodyRot = pig.yBodyRot;
                            hog.setTamed(true);

                            pig.discard(); // Domuzu sil
                            level.addFreshEntity(hog); // Hog'u ekle
                        }
                    }
                    data.remove(TAG_TRANSFORMING);
                }
            }

            // Client: Eski Spirit Partikülleri (Ellemedik)
            else if (pig.level().isClientSide && ReverieModAttributes.SPIRITUALITY != null) {
                Double value = pig.getAttributeValue(ReverieModAttributes.SPIRITUALITY);
                if (value != null && value > 0.5D && pig.getRandom().nextFloat() < 0.3f) {
                    pig.level().addParticle(ReverieModParticleTypes.SPIRIT_ORB.get(),
                            pig.getX(), pig.getY() + 0.5, pig.getZ(),
                            pig.getRandom().nextDouble(), 0.0, 0.0);
                }
            }
        }
    }

    // 3. OYUNCU ETKİLEŞİMİ
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof Pig pig) {
            Player player = event.getEntity();
            ItemStack itemInHand = player.getItemInHand(event.getHand());

            // A) MÜHÜR (SEAL) - RİTÜELİ BAŞLAT
            if (itemInHand.is(ReverieModItems.BOAR_MONK_SEAL.get())) {
                if (!pig.getPersistentData().contains(TAG_TRANSFORMING)) {
                    pig.getPersistentData().putInt(TAG_TRANSFORMING, RITUAL_DURATION);

                    // Ses
                    if (!event.getLevel().isClientSide) {
                        event.getLevel().playSound(null, pig.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.NEUTRAL, 1.0f, 1.0f);
                        if (!player.getAbilities().instabuild) itemInHand.shrink(1);
                    } else {
                        event.getLevel().playSound(player, pig.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.NEUTRAL, 1.0f, 1.0f);
                    }

                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }

            // B) PARŞÖMEN (Eski kod)
            else if (itemInHand.is(Items.PAPER) && ReverieModAttributes.SPIRITUALITY != null) {
                Double value = pig.getAttributeValue(ReverieModAttributes.SPIRITUALITY);
                if (value != null && value > 0.5D) {
                    if (!event.getLevel().isClientSide) {
                        if (!player.getAbilities().instabuild) itemInHand.shrink(1);
                        ItemStack scroll = new ItemStack(ReverieModItems.BOAR_WHISPERER_SCROLL.get());
                        if (!player.getInventory().add(scroll)) player.drop(scroll, false);
                        event.getLevel().playSound(null, pig.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
                        pig.getAttribute(ReverieModAttributes.SPIRITUALITY).setBaseValue(0.0D);
                    }
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }
        }
    }
}