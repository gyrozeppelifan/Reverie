// net/eris/reverie/mixin/MinecraftMixin.java
package net.eris.reverie.mixins;

import net.eris.reverie.init.ReverieModMobEffects;
import net.eris.reverie.possess.MeleeUtil;
import net.eris.reverie.possess.Targeting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void reverie$lockHotbarAndAutoSelect(CallbackInfo ci){
        Minecraft mc = (Minecraft)(Object)this;
        LocalPlayer p = mc.player;
        if (p == null) return;

        if (p.hasEffect(ReverieModMobEffects.POSSESSION.get())){
            LivingEntity target = Targeting.findPreferredTarget(p, 16);
            int best = MeleeUtil.bestHotbarSlot(p, target);
            if (best >= 0 && p.getInventory().selected != best){
                p.getInventory().selected = best; // hotbar kontrolü fiilen devre dışı
            }
        }
    }
}
