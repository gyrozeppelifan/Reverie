package net.eris.reverie.mixins;

import net.eris.reverie.capability.MeditationProvider;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin<T extends LivingEntity> extends HumanoidModel<T> {

    @Shadow public ModelPart leftSleeve;
    @Shadow public ModelPart rightSleeve;
    @Shadow public ModelPart leftPants;
    @Shadow public ModelPart rightPants;
    @Shadow public ModelPart jacket;

    public PlayerModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "setupAnim", at = @At("TAIL"))
    public void reverie$setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity instanceof Player player) {
            player.getCapability(MeditationProvider.PLAYER_MEDITATION).ifPresent(cap -> {
                if (cap.isMeditating()) {

                    // --- 1. GÖVDE ---
                    this.body.y = 0.0F;
                    this.jacket.y = this.body.y;
                    this.jacket.copyFrom(this.body);

                    // --- 2. SAĞ BACAK ---
                    this.rightLeg.xRot = -1.4F;
                    this.rightLeg.yRot = -0.4F;  // İçe bük (Biraz azalttım ki çok karışmasın)

                    // KRİTİK AYAR BURADA (zRot):
                    // 0.1F yerine 0.35F yaptık -> Bacağı sağa doğru dışarı açtık.
                    this.rightLeg.zRot = -0.3F;

                    this.rightLeg.y = 11.0F;
                    this.rightPants.copyFrom(this.rightLeg);
                    this.rightPants.y = this.rightLeg.y;

                    // --- 3. SOL BACAK ---
                    this.leftLeg.xRot = -1.4F;
                    this.leftLeg.yRot = 0.4F;    // İçe bük

                    // KRİTİK AYAR BURADA (zRot):
                    // -0.1F yerine -0.35F yaptık -> Bacağı sola doğru dışarı açtık.
                    this.leftLeg.zRot = 0.3F;

                    this.leftLeg.y = 11.0F;
                    this.leftPants.copyFrom(this.leftLeg);
                    this.leftPants.y = this.leftLeg.y;

                    // --- 4. KOLLAR ---
                    this.rightArm.xRot = -0.8F;
                    this.rightArm.zRot = 0.1F;
                    this.rightSleeve.copyFrom(this.rightArm);

                    this.leftArm.xRot = -0.8F;
                    this.leftArm.zRot = -0.1F;
                    this.leftSleeve.copyFrom(this.leftArm);
                }
            });
        }
    }
}