package net.eris.reverie.mixins;

import net.eris.reverie.init.ReverieModMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {

    // tick(boolean slowDown, float moveFactor)
    @Inject(method = "tick(ZF)V", at = @At("HEAD"), cancellable = true)
    private void reverie$possession_holdForward(boolean slowDown, float moveFactor, CallbackInfo ci) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p != null && p.hasEffect(ReverieModMobEffects.POSSESSION.get())) {
            this.forwardImpulse = 1.0F;
            this.leftImpulse = 0.0F;
            this.jumping = false;
            this.shiftKeyDown = false;
            ci.cancel();
        }
    }
}
