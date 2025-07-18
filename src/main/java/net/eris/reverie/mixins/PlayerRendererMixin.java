package net.eris.reverie.mixins;

import net.eris.reverie.init.ReverieModMobEffects;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {

    @Inject(
        method = "getArmPose(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void onGetArmPose(AbstractClientPlayer player, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> cir) {
        if (hand == InteractionHand.MAIN_HAND && player.hasEffect(ReverieModMobEffects.DRUNKEN_RAGE.get())) {
            // Burada artık booze bottle şartı yok, yani hangi item olursa olsun,
            // sadece main handdeyse animasyon oynar.
            cir.setReturnValue(HumanoidModel.ArmPose.THROW_SPEAR);
        }
    }
}
