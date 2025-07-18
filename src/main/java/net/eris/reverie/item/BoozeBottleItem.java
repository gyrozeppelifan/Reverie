package net.eris.reverie.item;

import net.eris.reverie.init.ReverieModMobEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class BoozeBottleItem extends Item {
    private static final String READY_TAG = "drunken_ready";

    public BoozeBottleItem() {
        super(new Item.Properties()
            .stacksTo(64)
            .rarity(Rarity.RARE)
            .durability(6) // 6 yudum
        );
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 40;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() >= stack.getMaxDamage()) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        // Sadece server tarafında ve gerçek bir ServerPlayer ise:
        if (!world.isClientSide() && user instanceof ServerPlayer player) {
            // Creative moddaysa durability artışını iptal et
            if (!player.isCreative()) {
                int dmg = stack.getDamageValue();
                if (dmg < stack.getMaxDamage()) {
                    stack.setDamageValue(dmg + 1);
                }
            }

            // 30 sn Drunken Rage efekti
            player.addEffect(new MobEffectInstance(
                ReverieModMobEffects.DRUNKEN_RAGE.get(),
                600,
                0
            ));
            // Bir sonraki vuruşta tetiklemek için flag set
            player.getPersistentData().putBoolean(READY_TAG, true);
        }

        return stack;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFF4EC9;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int max = stack.getMaxDamage();
        int dmg = stack.getDamageValue();
        return Math.round((float)(max - dmg) * 13f / (float)max);
    }
}
