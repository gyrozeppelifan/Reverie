package net.eris.reverie.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.eris.reverie.entity.BoneSpearProjectileEntity;

public class BoneSpearItem extends Item {

    public BoneSpearItem() {
        super(new Item.Properties().stacksTo(8).durability(250)); // Veya MCreator ne verdiyse
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isDamageableItem() || stack.getDamageValue() < stack.getMaxDamage() - 1) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return;

        int charge = this.getUseDuration(stack) - timeLeft;
        float power = getPowerForTime(charge);

        if (power > 0.1F) {
            BoneSpearProjectileEntity spearEntity = new BoneSpearProjectileEntity(level, player, stack.copy());
            spearEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 2.5F, 1.0F);

            if (player.getAbilities().instabuild) {
                spearEntity.pickup = net.minecraft.world.entity.projectile.AbstractArrow.Pickup.CREATIVE_ONLY;
            }

            level.addFreshEntity(spearEntity);
            level.playSound(null, spearEntity.getX(), spearEntity.getY(), spearEntity.getZ(),
                    net.minecraft.sounds.SoundEvents.TRIDENT_THROW, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
        }
    }

    public float getPowerForTime(int charge) {
        float f = (float) charge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(f, 1.0F);
    }
}
