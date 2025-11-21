package net.eris.reverie.item;

import net.eris.reverie.init.ReverieModBlocks;
import net.eris.reverie.init.ReverieModMobEffects;
import net.eris.reverie.init.ReverieModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3; // Vektör işlemleri için gerekli
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class AncientCrossbowItem extends Item {

    public static final int CLEANING_THRESHOLD = 50;
    public static final int ABILITY_COOLDOWN_TICKS = 400; // 20 Saniye
    private static final int STARTUP_DELAY = 8; // İlk atış için gecikme

    public AncientCrossbowItem() {
        super(new Properties().stacksTo(1).durability(CLEANING_THRESHOLD));
    }

    // --- 1. SAĞ TIK (TETİKLEME & YETENEK) ---
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // A) GİZLİLİK MODU
        if (player.isCrouching()) {
            if (!isClean(stack)) {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.literal("§7Mekanizma yosunlu, çalışmıyor..."), true);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 1.0F, 1.2F);
                }
                return InteractionResultHolder.fail(stack);
            }

            long gameTime = level.getGameTime();
            long nextUse = stack.getOrCreateTag().getLong("NextAbilityTime");
            long timeLeft = nextUse - gameTime;

            if (timeLeft > 0) {
                if (!level.isClientSide) {
                    int seconds = (int) (timeLeft / 20);
                    player.displayClientMessage(Component.literal("§cKadim Pelerin doluyor: " + seconds + "s"), true);
                }
                return InteractionResultHolder.fail(stack);
            }

            if (!level.isClientSide) {
                if (player.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get())) {
                    player.removeEffect(ReverieModMobEffects.ANCIENT_CLOAK.get());
                    player.displayClientMessage(Component.literal("§cGizlilik Devre Dışı"), true);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 1.0F, 0.8F);
                    stack.getOrCreateTag().putLong("NextAbilityTime", gameTime + ABILITY_COOLDOWN_TICKS);
                } else {
                    player.addEffect(new MobEffectInstance(ReverieModMobEffects.ANCIENT_CLOAK.get(), 200000, 0, false, false, true));
                    player.displayClientMessage(Component.literal("§aGizlilik Aktif"), true);
                    // Burada efekt açılma sesi çalıyor (Kendi ses dosyan varsa değiştirebilirsin)
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), ReverieModSounds.ANCIENT_CLOAK.get(), SoundSource.PLAYERS, 1.0F, 1.5F);
                }
            }
            return InteractionResultHolder.success(stack);
        }

        // B) ATEŞLEME BAŞLANGICI
        if (!player.getAbilities().instabuild && findAmmo(player).isEmpty()) {
            return InteractionResultHolder.fail(stack);
        }

        stack.getOrCreateTag().putInt("FireDelay", 0);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    // --- 2. MAKİNE TÜFEK MANTIĞI ---
    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseTicks) {
        if (!(livingEntity instanceof Player player)) return;

        int ticksHeld = getUseDuration(stack) - remainingUseTicks;

        if (ticksHeld < STARTUP_DELAY) {
            return;
        }

        int currentDelay = stack.getOrCreateTag().getInt("FireDelay");
        if (currentDelay > 0) {
            stack.getOrCreateTag().putInt("FireDelay", currentDelay - 1);
            return;
        }

        // --- HIZ HESAPLAMA ---
        int startRate = 25;
        int maxRate = 5;
        int fireRate;
        int activeTicks = ticksHeld - STARTUP_DELAY;
        boolean isStealth = player.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get());

        if (isStealth) {
            fireRate = maxRate;
        } else {
            int rampUp = activeTicks / 12;
            fireRate = Math.max(maxRate, startRate - rampUp);
        }

        if (!isClean(stack)) {
            fireRate = Math.max(12, fireRate);
        }

        // --- ATEŞLEME ZAMANI ---
        ItemStack ammoStack = findAmmo(player);
        if (ammoStack.isEmpty() && !player.getAbilities().instabuild) {
            player.stopUsingItem();
            return;
        }

        if (!level.isClientSide) {
            ArrowItem arrowItem = (ArrowItem) (ammoStack.getItem() instanceof ArrowItem ? ammoStack.getItem() : Items.ARROW);
            Arrow arrowEntity = (Arrow) arrowItem.createArrow(level, ammoStack, player);

            float spread = Math.min(3.0F, ticksHeld * 0.05F);

            arrowEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.5F, spread);
            arrowEntity.setBaseDamage(arrowEntity.getBaseDamage() + 1.5D);

            handleCleaning(stack, level, player);
            level.addFreshEntity(arrowEntity);

            if (!player.getAbilities().instabuild) {
                ammoStack.shrink(1);
            }
        }

        // --- SES VE EFEKTLER ---

        // 1. SES SEÇİMİ (YENİ)
        SoundEvent soundToPlay;
        float volume = 1.0F;
        float pitch = 1.0F;

        if (isStealth) {
            // Gizlilik Modu Sesi
            soundToPlay = ReverieModSounds.ANCIENT_CROSSBOW_CLOAK_SHOOT.get();
            volume = 0.8F; // Biraz daha sessiz
        } else {
            // Normal Mod: Hıza göre ses değişimi
            if (fireRate >= 15) {
                soundToPlay = ReverieModSounds.ANCIENT_CROSSBOW_SHOOT_NORMAL.get();
            } else if (fireRate >= 8) {
                soundToPlay = ReverieModSounds.ANCIENT_CROSSBOW_SHOOT_FAST.get();
            } else {
                soundToPlay = ReverieModSounds.ANCIENT_CROSSBOW_SHOOT_RAPID.get();
                // Makine tüfek gibi hissettirmesi için pitch'i çok hafif rastgele yapıyoruz
                pitch = 0.9F + (level.random.nextFloat() * 0.2F);
            }
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), soundToPlay, SoundSource.PLAYERS, volume, pitch);

        // 2. OYUNCU GERİ TEPMESİ (PHYSICAL RECOIL - YENİ)
        // Baktığı yönün tersine (negatif scale) itiyoruz.
        // 0.1 değeri hafif bir itme sağlar, seri atışta birikir.
        Vec3 lookDir = player.getLookAngle();
        player.push(-lookDir.x * 0.1, -lookDir.y * 0.05, -lookDir.z * 0.1);
        player.hurtMarked = true; // Hareketi güncelle

        // 3. GÖRSEL RECOIL
        stack.getOrCreateTag().putInt("RecoilTicks", 4);

        // 4. KAMERA SARSINTISI
        if (level.isClientSide) {
            float kick = isClean(stack) ? -1.2F : -0.6F;
            if (fireRate < 10) kick *= 0.7F;
            player.turn(0, kick * 0.3f);
            player.setXRot(player.getXRot() + kick);
        }

        stack.getOrCreateTag().putInt("FireDelay", fireRate);
    }

    // --- 3. CLIENT RENDER: SMOOTH ANIMATION ---
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            private float currentRecoil = 0.0F;

            @Override
            public boolean applyForgeHandTransform(PoseStack poseStack, net.minecraft.client.player.LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {

                boolean isFiring = itemInHand.hasTag() && itemInHand.getTag().getInt("RecoilTicks") > 0;
                float target = isFiring ? 1.0F : 0.0F;
                float speed = isFiring ? 0.5F : 0.15F;
                currentRecoil += (target - currentRecoil) * speed;

                if (currentRecoil > 0.01F) {
                    float kick = currentRecoil;

                    // Clipping fix + Hissiyat
                    poseStack.translate(0.0, 0.0, kick * 0.02);
                    poseStack.mulPose(Axis.XP.rotationDegrees(-kick * 12.0F));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(kick * 3.0F));

                    if (isFiring) {
                        float shake = (player.tickCount % 2 == 0) ? 0.5F : -0.5F;
                        poseStack.mulPose(Axis.YP.rotationDegrees(shake));
                    }
                }
                return false;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }
        });
    }

    // --- YARDIMCI METODLAR ---
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (stack.hasTag()) {
            if (stack.getTag().getInt("RecoilTicks") > 0) {
                stack.getTag().putInt("RecoilTicks", stack.getTag().getInt("RecoilTicks") - 1);
            }
            if (entity instanceof Player player && !player.isUsingItem()) {
                stack.getTag().putInt("FireDelay", 0);
            }
        }
    }

    private void handleCleaning(ItemStack stack, Level level, Player player) {
        if (!isClean(stack)) {
            int current = stack.getDamageValue();
            if (current >= stack.getMaxDamage() - 1) {
                setClean(stack, true);
                stack.setDamageValue(0);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                player.displayClientMessage(Component.literal("§6Kadim Arbalet gerçek formuna kavuştu!"), true);
            } else {
                stack.setDamageValue(current + 1);
            }
        }
    }

    private ItemStack findAmmo(Player player) {
        if (player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof ArrowItem) return player.getItemInHand(InteractionHand.OFF_HAND);
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof ArrowItem) return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.NONE; }
    @Override public int getUseDuration(ItemStack stack) { return 72000; }
    public boolean isClean(ItemStack stack) { return stack.getOrCreateTag().getBoolean("IsClean"); }
    public void setClean(ItemStack stack, boolean clean) { stack.getOrCreateTag().putBoolean("IsClean", clean); }
    @Override public boolean isBarVisible(ItemStack stack) { return !isClean(stack); }
    @Override public int getBarColor(ItemStack stack) { return 0x55FF55; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (!isClean(stack)) {
            tooltip.add(Component.literal("§2Yosunlu & Paslı"));
            tooltip.add(Component.literal("§7Mekanizmayı temizlemek için kullan."));
        } else {
            tooltip.add(Component.literal("§6Özel Yetenek: Kadim Pelerin"));
            if (stack.hasTag()) {
                long timeLeft = stack.getTag().getLong("NextAbilityTime") - (level != null ? level.getGameTime() : 0);
                if (timeLeft > 0) {
                    tooltip.add(Component.literal("§c(Doluyor: " + (timeLeft / 20) + "s)"));
                } else {
                    tooltip.add(Component.literal("§a(Hazır)"));
                }
            }
        }
    }
}