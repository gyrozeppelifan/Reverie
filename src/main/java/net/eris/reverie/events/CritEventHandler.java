package net.eris.reverie.events;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.init.ReverieModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(
        modid = ReverieMod.MODID,
        bus   = Mod.EventBusSubscriber.Bus.FORGE
)
public class CritEventHandler {
    private static final Random RAND = new Random();

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        DamageSource src   = event.getSource();
        Entity direct      = src.getDirectEntity();
        Entity shooter     = src.getEntity();
        if (!(shooter instanceof LivingEntity attacker)) return;

        boolean isProjectile = direct instanceof Projectile;
        boolean isMelee      = !isProjectile;
        boolean isFallCrit   = isMelee && attacker.fallDistance > 0;

        // 1) Attribute tabanlı luck (oyuncu veya attribute eklenmiş mob)
        double attrLuck = 0;
        if (attacker.getAttribute(Attributes.LUCK) != null) {
            attrLuck = attacker.getAttributeValue(Attributes.LUCK);
        }

        // 2) Effect tabanlı luck (mob potion olarak almışsa)
        double effectLuck = 0;
        if (attacker.hasEffect(MobEffects.LUCK)) {
            // amplifier: 0->Luck I, 1->Luck II, ...
            effectLuck = attacker.getEffect(MobEffects.LUCK).getAmplifier() + 1;
        }

        // hangisi büyükse onu al
        float luck = (float) Math.max(attrLuck, effectLuck);
        if (luck <= 0) return;

        // Crit ihtimali: 5% × Luck seviyesi
        float critChance = luck * 0.05f;
        if (RAND.nextFloat() >= critChance) return;

        // Crit çarpanı
        float multiplier = isFallCrit ? 1.5f : 3f;
        event.setAmount(event.getAmount() * multiplier);

        // SES
        Level world = attacker.level();
        if (!world.isClientSide) {
            world.playSound(
                    null,
                    event.getEntity().blockPosition(),
                    ReverieModSounds.LUCKY_CRIT.get(),
                    SoundSource.NEUTRAL,
                    1f,
                    1f
            );
        }

        // PARTİKÜLLER
        if (world instanceof ServerLevel serverWorld) {
            LivingEntity target = (LivingEntity) event.getEntity();
            serverWorld.sendParticles(
                    ParticleTypes.CRIT,
                    target.getX(),
                    target.getY() + target.getBbHeight() * 0.5,
                    target.getZ(),
                    8,
                    target.getBbWidth() * 0.5,
                    target.getBbHeight() * 0.25,
                    target.getBbWidth() * 0.5,
                    0.0
            );
        }
    }
}
