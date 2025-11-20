// net/eris/reverie/possess/MeleeUtil.java
package net.eris.reverie.possess;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class MeleeUtil {

    /** Hotbar (0–8) içinden en iyi yakın dövüş silahının slotu; yoksa -1. */
    public static int bestHotbarSlot(Player p, LivingEntity target){
        int best = -1;
        double bestScore = -1;
        for (int s = 0; s < 9; s++){
            ItemStack st = p.getInventory().getItem(s);
            if (st.isEmpty()) continue;
            double sc = meleeScore(st, target);
            if (sc > bestScore){
                bestScore = sc;
                best = s;
            }
        }
        return (bestScore > 0) ? best : -1;
    }

    /** Silah skoru: ATTACK_DAMAGE + enchant bonus; Sword > Axe hafif öncelik. */
    public static double meleeScore(ItemStack stack, LivingEntity target){
        if (stack.isEmpty()) return 0;

        double dmg = 0.0;
        Multimap<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier> mods =
                stack.getAttributeModifiers(EquipmentSlot.MAINHAND);

        for (AttributeModifier m : mods.get(Attributes.ATTACK_DAMAGE)) {
            switch (m.getOperation()){
                case ADDITION -> dmg += m.getAmount();
                case MULTIPLY_BASE, MULTIPLY_TOTAL -> dmg += 4.0 * m.getAmount();
            }
        }

        // Enchant (Sharpness/Smite/Bane vb.)
        dmg += EnchantmentHelper.getDamageBonus(stack, target != null ? target.getMobType() : net.minecraft.world.entity.MobType.UNDEFINED);

        // Tür önceliği
        if (stack.getItem() instanceof SwordItem) dmg += 0.5;
        else if (stack.getItem() instanceof AxeItem) dmg += 0.4;
        else if (stack.getItem() instanceof TridentItem) dmg += 0.3; // yakın dövüşte de fena değil

        // Çok kırık olana ufak ceza
        if (stack.isDamaged() && stack.getMaxDamage() > 0){
            double wear = (double)stack.getDamageValue() / (double)stack.getMaxDamage();
            dmg *= (1.0 - 0.15 * wear);
        }

        return Math.max(0, dmg);
    }
}
