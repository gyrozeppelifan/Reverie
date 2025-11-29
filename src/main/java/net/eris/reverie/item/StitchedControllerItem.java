package net.eris.reverie.item;

import net.eris.reverie.entity.StitchedEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class StitchedControllerItem extends Item {

    public StitchedControllerItem() {
        super(new Item.Properties().stacksTo(1)); // Sadece 1 tane taşınabilsin
    }

    // --- BAĞLAMA İŞLEMİ (MOBA SAĞ TIK) ---
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (interactionTarget instanceof StitchedEntity stitched) {
            // KRİTİK DÜZELTME: NBT Kaydını HEM Client HEM Server tarafında yapıyoruz!
            // Böylece Creative modda item silinmiyor ve tooltip anında güncelleniyor.
            CompoundTag tag = stack.getOrCreateTag();
            tag.putUUID("LinkedStitched", stitched.getUUID());
            stack.setTag(tag);

            // Sadece Server tarafında mesaj ve ses verelim (Spam olmasın)
            if (!player.level().isClientSide) {
                player.sendSystemMessage(Component.literal("§a[Reverie] Kumanda bağlandı: " + stitched.getName().getString()));
                player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
            }

            // İşlem başarılı, el sallama animasyonunu oynat
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
        return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
    }

    // --- KULLANMA İŞLEMİ (HAVAYA SAĞ TIK) ---
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        // Client tarafında da "Success" dönelim ki item kullanıldığı belli olsun (animasyon vs.)
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        // Server tarafı mantığı
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.hasUUID("LinkedStitched")) {
            UUID stitchedId = tag.getUUID("LinkedStitched");

            if (level instanceof ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(stitchedId);

                if (entity instanceof StitchedEntity stitched && stitched.isAlive()) {
                    // Yeteneği tetikle
                    stitched.triggerAbility();

                    // Oyuncuya görsel feedback
                    player.swing(usedHand);
                    player.getCooldowns().addCooldown(this, 10);

                    return InteractionResultHolder.success(stack);
                } else {
                    player.sendSystemMessage(Component.literal("§cBağlı Stitched bulunamadı veya ölü!"));
                }
            }
        } else {
            player.sendSystemMessage(Component.literal("§eÖnce bir Stitched'e sağ tıklayarak bağla!"));
        }

        return InteractionResultHolder.fail(stack);
    }

    // --- TOOLTIP (BİLGİ YAZISI) ---
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        if (stack.hasTag() && stack.getTag().hasUUID("LinkedStitched")) {
            tooltipComponents.add(Component.literal("§7Durum: §aBağlı (Tetiklemek için Sağ Tık)"));
            // Debug için ID görmek istersen şu satırı aç:
            // tooltipComponents.add(Component.literal("§8ID: " + stack.getTag().getUUID("LinkedStitched").toString().substring(0, 8) + "..."));
        } else {
            tooltipComponents.add(Component.literal("§7Durum: §cBağlantı Yok"));
            tooltipComponents.add(Component.literal("§8(Bağlamak için Stitched'e sağ tıkla)"));
        }
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}