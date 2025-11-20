package net.eris.reverie.item;

import net.eris.reverie.init.ReverieModMobEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
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
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class AncientCrossbowItem extends Item {

    public static final int CLEANING_THRESHOLD = 50;
    private static final int ABILITY_COOLDOWN_TICKS = 400; // 20 Saniye

    public AncientCrossbowItem() {
        super(new Properties().stacksTo(1).durability(CLEANING_THRESHOLD));
    }

    // --- 1. SAĞ TIK (TETİKLEME & YETENEK) ---
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // A) GİZLİLİK MODU (Eğilerek)
        if (player.isCrouching()) {
            // Temizlik kontrolü
            if (!isClean(stack)) {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.literal("§7Mekanizma yosunlu, çalışmıyor..."), true);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 1.0F, 1.2F);
                }
                return InteractionResultHolder.fail(stack);
            }

            // MANUEL COOLDOWN KONTROLÜ (Vanilla cooldown kullanmıyoruz)
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

            // Yeteneği Aç/Kapa
            if (!level.isClientSide) {
                if (player.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get())) {
                    // Kapat ve Cooldown Başlat
                    player.removeEffect(ReverieModMobEffects.ANCIENT_CLOAK.get());
                    player.displayClientMessage(Component.literal("§cGizlilik Devre Dışı"), true);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 1.0F, 0.8F);

                    // Şu anki zaman + 400 tick sonrasını kaydet
                    stack.getOrCreateTag().putLong("NextAbilityTime", gameTime + ABILITY_COOLDOWN_TICKS);
                } else {
                    // Aç
                    player.addEffect(new MobEffectInstance(ReverieModMobEffects.ANCIENT_CLOAK.get(), 220, 0, false, false, true));
                    player.displayClientMessage(Component.literal("§aGizlilik Aktif"), true);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ILLUSIONER_PREPARE_MIRROR, SoundSource.PLAYERS, 1.0F, 1.5F);
                }
            }
            return InteractionResultHolder.success(stack);
        }

        // B) ATEŞLEME BAŞLANGICI
        // Mermi yoksa başlama
        if (!player.getAbilities().instabuild && findAmmo(player).isEmpty()) {
            return InteractionResultHolder.fail(stack);
        }

        // Atış sayacını sıfırla (Tıklayınca hemen ateş etmesin, azıcık beklesin)
        stack.getOrCreateTag().putInt("FireDelay", 5);

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    // --- 2. MAKİNE TÜFEK (TIMER MANTIĞI) ---
    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseTicks) {
        if (!(livingEntity instanceof Player player)) return;

        // Basılı tutma süresi (Ramp-up hesaplamak için)
        int durationHeld = getUseDuration(stack) - remainingUseTicks;

        // Sayaç mantığı: Her tickte "FireDelay"i 1 azaltıyoruz. 0 olunca ateşliyoruz.
        int currentDelay = stack.getOrCreateTag().getInt("FireDelay");

        if (currentDelay > 0) {
            stack.getOrCreateTag().putInt("FireDelay", currentDelay - 1);
            return; // Henüz zamanı gelmedi
        }

        // --- ATEŞ ETME ZAMANI! ---

        // Mermi kontrolü (Sürekli kontrol etmeliyiz)
        ItemStack ammoStack = findAmmo(player);
        if (ammoStack.isEmpty() && !player.getAbilities().instabuild) {
            player.stopUsingItem();
            return;
        }

        if (!level.isClientSide) {
            ArrowItem arrowItem = (ArrowItem) (ammoStack.getItem() instanceof ArrowItem ? ammoStack.getItem() : Items.ARROW);
            Arrow arrowEntity = (Arrow) arrowItem.createArrow(level, ammoStack, player);

            // İsabet (Spread): Hızlandıkça sapıtsın
            // Max hızda 3.0 spread, yavaşta 0.0
            float spread = Math.min(3.0F, durationHeld * 0.05F);

            arrowEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.5F, spread);
            arrowEntity.setBaseDamage(arrowEntity.getBaseDamage() + 0.8D);

            handleCleaning(stack, level, player);
            level.addFreshEntity(arrowEntity);

            if (!player.getAbilities().instabuild) {
                ammoStack.shrink(1);
                if (ammoStack.isEmpty()) player.getInventory().removeItem(ammoStack);
            }
        }

        // Efektler
        float pitch = 1.0F + Math.min(1.0F, durationHeld * 0.03F); // Hızlandıkça ses incelsin
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, pitch);

        // Recoil (Görsel) başlat
        stack.getOrCreateTag().putInt("RecoilTicks", 3); // 2 Ticklik kısa bir tepme

        // Kamera Sarsıntısı
        if (level.isClientSide) {
            float kick = isClean(stack) ? -1.0F : -0.5F;
            player.setXRot(player.getXRot() + kick);
            player.turn(0, (player.tickCount % 2 == 0 ? 0.5f : -0.5f)); // Yan titreme
        }

        // --- SONRAKİ ATIŞ ZAMANI HESAPLA ---

        int nextDelay;

        // Sinsi Mod açıksa -> Direkt Maksimum Hız (3 tick)
        if (player.hasEffect(ReverieModMobEffects.ANCIENT_CLOAK.get())) {
            nextDelay = 3;
        } else {
            // Normal Mod -> Hızlanarak git (Ramp-Up)
            // Başlangıç: 15 tick -> Bitiş: 4 tick
            // Her 10 tickte bir (0.5 saniye) hızlansın
            int speedLevel = durationHeld / 10;
            nextDelay = Math.max(4, 15 - speedLevel);
        }

        // Yosunluysa hızı sınırla
        if (!isClean(stack)) nextDelay = Math.max(12, nextDelay);

        // Sayacı kur
        stack.getOrCreateTag().putInt("FireDelay", nextDelay);
    }

    // --- 3. CLIENT RENDER: CLIPPING FIX ---
// ... (Sınıfın en altına, initializeClient metodunun olduğu yere gel) ...

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            // Client tarafında animasyonun akıcılığını tutacak değişkenler
            private float currentRecoil = 0.0F; // Şimdiki pozisyon (0.0 -> 1.0 -> 0.0)
            private float targetRecoil = 0.0F;  // Gitmek istediği pozisyon

            @Override
            public boolean applyForgeHandTransform(com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.player.LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {

                // 1. SİNYALİ AL (Server'dan gelen RecoilTicks)
                boolean isFiring = itemInHand.hasTag() && itemInHand.getTag().getInt("RecoilTicks") > 0;

                // 2. HEDEFİ BELİRLE
                // Ateş ediyorsa hedef 1.0 (Tam tepme), etmiyorsa 0.0 (Duruş)
                targetRecoil = isFiring ? 1.0F : 0.0F;

                // 3. SMOOTH GEÇİŞ (LERP)
                // currentRecoil değerini yavaşça targetRecoil'e yaklaştır.
                // 0.4F değeri hızı belirler. (Daha düşük = Daha yavaş/yumuşak, Daha yüksek = Daha sert)
                // partialTick kullanarak FPS bağımsız hale getiriyoruz.
                float lerpSpeed = 0.6F; // Tepme hızı (Sert kalksın)

                // Ateş etmiyorsa (geri dönüş) daha yavaş/yumuşak insin
                if (!isFiring) lerpSpeed = 0.2F;

                // MathHelper.lerp kullanmadan manuel lerp (basit formül):
                // current = current + (target - current) * hız
                currentRecoil += (targetRecoil - currentRecoil) * lerpSpeed;

                // 4. ANİMASYON EĞRİSİ (SİNÜS DALGASI gibi)
                // Dümdüz gidip gelmesin, "Vur-Sek" hissi için eğri kullanalım.
                // Bu değer 0.0 (normal) ile 1.0 (max tepme) arasında değişir.
                float kick = currentRecoil;

                // Eğer hareket çok azsa (0.01 altı) işlem yapma (Titremeyi önler)
                if (kick > 0.01F) {

                    // --- TRANSFORM İŞLEMLERİ ---

                    // A. YUMUŞAK ŞAHLANMA (X Rotasyonu)
                    // 0'dan 15 dereceye yumuşakça kalkıp inecek.
                    float xRot = kick * 15.0F;
                    poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-xRot));

                    // B. YUMUŞAK GERİ GELME (Z Ekseni)
                    // Yüzüne doğru yumuşakça gelip gidecek.
                    // kick * 0.1F = 10 cm geriye
                    poseStack.translate(0.0, 0.0, kick * 0.1F);

                    // C. YANA YATMA (Z Rotasyonu - Tilt)
                    // Silah ateşlerken hafifçe sağa yatsın (Daha doğal durur)
                    poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(kick * 3.0F));

                    // D. KAOS (Titreme) - Sadece ateş anında (Target 1 iken) olsun
                    if (isFiring) {
                        float randomShake = (player.tickCount % 2 == 0) ? 1.0F : -1.0F;
                        // Titreme miktarını azalttık, smoothluk bozulmasın
                        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(randomShake * 1.0F));
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

    // Elde değilken bile tagleri temizle ki bug olmasın
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (stack.hasTag()) {
            // Recoil efekti bitsin
            if (stack.getTag().getInt("RecoilTicks") > 0) {
                stack.getTag().putInt("RecoilTicks", stack.getTag().getInt("RecoilTicks") - 1);
            }
            // Eğer basılı tutmuyorsa (isSelected ama use değil) fire delay'i sıfırla
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
        } else {
            tooltip.add(Component.literal("§6Özel Yetenek: Kadim Pelerin"));
            // Cooldown bilgisini göster
            if (stack.hasTag()) {
                long timeLeft = stack.getTag().getLong("NextAbilityTime") - level.getGameTime();
                if (timeLeft > 0) {
                    tooltip.add(Component.literal("§c(Doluyor: " + (timeLeft/20) + "s)"));
                } else {
                    tooltip.add(Component.literal("§a(Hazır)"));
                }
            }
        }
    }
}