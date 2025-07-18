package net.eris.reverie.mixins;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.Minecraft;
import net.eris.reverie.init.ReverieModMobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    // Eski sensitivity’yi burada saklıyoruz (sadece 1 oyuncu için yeterli)
    @Unique
    private static Double reverie$oldSensitivity = null;

    @Inject(method = "turnPlayer", at = @At("HEAD"))
    private void reverie$slowMouse(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.options != null) {
            boolean hasDrunk = mc.player.hasEffect(ReverieModMobEffects.DRUNKEN_RAGE.get());
            Double currentSens = mc.options.sensitivity().get();

            if (hasDrunk) {
                // Efekt varken: eski değeri bir defa sakla, sonra hep 0.2’ye setle
                if (reverie$oldSensitivity == null) {
                    reverie$oldSensitivity = currentSens;
                }
                Double newSens = 0.2D; // Değerini istediğin gibi ayarla
                if (currentSens > newSens) {
                    mc.options.sensitivity().set(newSens);
                }
            } else {
                // Efekt bittiyse eski sensitivity’yi geri ver
                if (reverie$oldSensitivity != null && !currentSens.equals(reverie$oldSensitivity)) {
                    mc.options.sensitivity().set(reverie$oldSensitivity);
                }
                reverie$oldSensitivity = null;
            }
        }
    }
}
