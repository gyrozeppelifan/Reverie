package net.eris.reverie.item;

import net.eris.reverie.entity.SpikedLogEntity;
import net.eris.reverie.init.ReverieModEnchantments;
import net.eris.reverie.init.ReverieModEntities;
import net.eris.reverie.init.ReverieModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SpikedLogItemItem extends Item {
    public SpikedLogItemItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.COMMON));
    }

    // --- EKLENDİ: ENCHANTMENT TABLE DESTEĞİ ---
    @Override
    public int getEnchantmentValue() {
        return 15; // Büyü masasında işlem görmesini sağlar (Demir eşyalarla aynı değer)
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            Vec3 look = player.getLookAngle().normalize();
            Vec3 spawnBase = player.getEyePosition();
            Vec3 spawnPos = spawnBase.add(look.x * 1.8, 0.5, look.z * 1.8);

            SpikedLogEntity log = new SpikedLogEntity(ReverieModEntities.SPIKED_LOG.get(), level);
            log.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, player.getYRot(), 0);

            log.setOwner(player);
            log.setOwnerName(player.getName().getString());

            // --- BÜYÜLERİ OKU ---
            int wildfire = EnchantmentHelper.getItemEnchantmentLevel(ReverieModEnchantments.WILDFIRE.get(), stack);
            int momentum = EnchantmentHelper.getItemEnchantmentLevel(ReverieModEnchantments.MOMENTUM.get(), stack);
            int ricochet = EnchantmentHelper.getItemEnchantmentLevel(ReverieModEnchantments.RICOCHET.get(), stack);
            int vortex = EnchantmentHelper.getItemEnchantmentLevel(ReverieModEnchantments.VORTEX.get(), stack);
            int recall = EnchantmentHelper.getItemEnchantmentLevel(ReverieModEnchantments.RECALL.get(), stack);

            log.setWildfireLevel(wildfire);
            log.setMomentumLevel(momentum);
            log.setRicochetLevel(ricochet);
            log.setVortexLevel(vortex);
            log.setRecallLevel(recall);

            level.addFreshEntity(log);

            // Momentum Hızlandırması
            if (momentum > 0) {
                Vec3 initialBoost = look.scale(0.5 + (momentum * 0.2));
                log.setDeltaMovement(log.getDeltaMovement().add(initialBoost));
            }

            level.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z, ReverieModSounds.SPIKED_LOG_THROW.get(), SoundSource.PLAYERS, 1f, 1f);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            player.getCooldowns().addCooldown(this, 120);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}