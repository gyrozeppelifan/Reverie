package net.eris.reverie.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.eris.reverie.init.ReverieModMobEffects;
import net.eris.reverie.entity.GoblinBruteEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class ClientPlayerInputMixin {
    @Inject(method = "aiStep", at = @At("HEAD"))
    private void blockCertainInputs(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer)(Object)this;
        Minecraft mc = Minecraft.getInstance();

        // Drunken Rage etkisindeysek VEYA GoblinBrute üzerinde isek tuşları kapat
        if (player.hasEffect(ReverieModMobEffects.DRUNKEN_RAGE.get())
                || player.getVehicle() instanceof GoblinBruteEntity) {

            // A, S, D tuşları kapalı
            mc.options.keyLeft.setDown(false);
            mc.options.keyDown.setDown(false);
            mc.options.keyRight.setDown(false);

            // Sprint kapalı
            mc.options.keySprint.setDown(false);

            // Shift (sneak) kapalı - hem options hem player
            mc.options.keyShift.setDown(false);
            player.setShiftKeyDown(false);

            // W ve Jump açık kaldı
            player.setSprinting(true);
        }
    }
}
