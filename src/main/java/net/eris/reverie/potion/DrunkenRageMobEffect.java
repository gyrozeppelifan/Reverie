package net.eris.reverie.potion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public class DrunkenRageMobEffect extends MobEffect {
    private static final String READY_TAG = "drunken_ready";

    public DrunkenRageMobEffect() {
        super(MobEffectCategory.NEUTRAL, 0xAA3300);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(entity, attributes, amplifier);
        AttributeInstance speed = attributes.getInstance(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.addPermanentModifier(new AttributeModifier(
                UUID.fromString("d9f0890c-3c09-4a3c-b7d0-2f5f8648fa7b"),
                "drunken_rage_speed",
                0.3,
                AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }
        entity.getPersistentData().putBoolean(READY_TAG, true);
        if (!entity.level().isClientSide()) {
            entity.level().playSound(
                null,
                entity.blockPosition(),
                ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("reverie:charge")),
                SoundSource.NEUTRAL,
                3.0f, 1.0f
            );
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);
        AttributeInstance speed = attributes.getInstance(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.getModifiers().stream()
                .filter(m -> "drunken_rage_speed".equals(m.getName()))
                .forEach(speed::removeModifier);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    public boolean isInstant() {
        return false;
    }

    // Aşağıdaki @Override satırını **mutlaka kaldır**:
    // public void applyEffectTick(LivingEntity entity, int amplifier) { ... }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
        Vec3 forward = entity.getLookAngle().normalize().scale(0.6);
        Vec3 motion  = new Vec3(forward.x, entity.getDeltaMovement().y, forward.z);

        if (entity instanceof Player) {
            entity.move(MoverType.SELF, motion);
        } else {
            entity.move(MoverType.SELF, motion);
            float targetYaw = entity.getYRot();
            float prevYaw   = entity.yRotO;
            float delta     = ((targetYaw - prevYaw + 180f) % 360f) - 180f;
            float maxTurn   = 1.5f;
            delta = Math.max(-maxTurn, Math.min(maxTurn, delta));
            float newYaw = prevYaw + delta;
            entity.setYRot(newYaw);
            entity.yRotO    = newYaw;
            entity.yBodyRot = newYaw;
            entity.yHeadRot = newYaw;
        }
    }
}
