package net.eris.reverie.fx.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.ThreadLocalRandom;

@Mod.EventBusSubscriber(modid = "reverie", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ScreenShakeClient {
    private static int   remainingTicks = 0;
    private static int   totalTicks     = 0;
    private static float amplitudeDeg   = 0f;

    // faz tohumları (temel wobble için)
    private static float seedYaw   = (float) Math.random() * 1000f;
    private static float seedPitch = (float) Math.random() * 1000f;
    private static float seedRoll  = (float) Math.random() * 1000f;

    // anlık darbe (kick) bileşenleri
    private static float impulseYaw = 0f, impulsePitch = 0f, impulseRoll = 0f;

    // ---- ayarlar (isteğine göre oynayabilirsin) ----
    private static final float INTENSITY     = 0.90f; // genel güç çarpanı
    private static final float DECAY_POW     = 1.10f; // zamanla sönüm eğrisi (daha küçük => daha yavaş sönüm)
    private static final float MAX_DEG       = 2.4f;  // güvenlik limiti (derece)

    // “kick” darbeleri (rastgele kısa şoklar)
    private static final float IMPULSE_PROB  = 0.18f; // her tick darbe olasılığı
    private static final float IMPULSE_DECAY = 0.82f; // darbelerin her tick zayıflaması
    private static final float IMP_YAW_SCALE   = 0.55f;
    private static final float IMP_PITCH_SCALE = 0.35f; // pitch'i biraz sakin tut
    private static final float IMP_ROLL_SCALE  = 0.45f;

    private ScreenShakeClient() {}

    public static void trigger(int durationTicks, float amplitudeDegrees) {
        remainingTicks = Math.max(0, durationTicks);
        totalTicks     = Math.max(1, durationTicks);
        amplitudeDeg   = Math.max(0f, amplitudeDegrees);

        // tekrar tetiklenince faz ve darbeleri yenile
        seedYaw   = (float) Math.random() * 1000f;
        seedPitch = (float) Math.random() * 1000f;
        seedRoll  = (float) Math.random() * 1000f;
        impulseYaw = impulsePitch = impulseRoll = 0f;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        if (remainingTicks > 0) {
            remainingTicks--;

            // aktifken ara ara şok darbeleri üret
            var rnd = ThreadLocalRandom.current();
            if (rnd.nextFloat() < IMPULSE_PROB) {
                float base = Math.min(amplitudeDeg, MAX_DEG);
                float s1 = rnd.nextBoolean() ? 1f : -1f;
                float s2 = rnd.nextBoolean() ? 1f : -1f;
                float s3 = rnd.nextBoolean() ? 1f : -1f;
                float r  = 0.6f + rnd.nextFloat() * 0.8f; // 0.6–1.4
                impulseYaw   += s1 * base * IMP_YAW_SCALE   * r;
                impulsePitch += s2 * base * IMP_PITCH_SCALE * r;
                impulseRoll  += s3 * base * IMP_ROLL_SCALE  * r;
            }

            // darbeleri eksponansiyel söndür
            impulseYaw   *= IMPULSE_DECAY;
            impulsePitch *= IMPULSE_DECAY;
            impulseRoll  *= IMPULSE_DECAY;
        } else {
            impulseYaw = impulsePitch = impulseRoll = 0f;
        }
    }

    // Kamera açılarına shake uygula (yaw/pitch/roll)
    @SubscribeEvent
    public static void onAngles(ViewportEvent.ComputeCameraAngles e) {
        if (remainingTicks <= 0) return;

        // sönüm ve anlık güç
        float tLeft = remainingTicks + (float) e.getPartialTick();
        float trauma = tLeft / (float) totalTicks;                   // 0..1
        float decay  = (float) Math.pow(trauma, DECAY_POW);
        float amp    = Math.min(amplitudeDeg * decay * INTENSITY, MAX_DEG);

        // zaman
        final var mc = Minecraft.getInstance();
        float t = (mc.level == null) ? 0f
                : ((float) mc.level.getGameTime() + (float) e.getPartialTick());

        // yüksek frekanslı, katmanlı wobble (daha “vahşi”)
        double wobYaw   = Math.sin((t + seedYaw)   * 22.0) * 0.55 + Math.sin((t * 0.63f + seedYaw * 1.7f)   * 33.0) * 0.45;
        double wobPitch = Math.cos((t + seedPitch) * 20.0) * 0.50 + Math.cos((t * 0.58f + seedPitch * 0.9f) * 29.0) * 0.50;
        double wobRoll  = Math.sin((t + seedRoll)  * 24.0) * 0.50 + Math.cos((t * 0.71f + seedRoll * 1.2f)  * 37.0) * 0.50;

        // toplam sapmalar (derece)
        float yawOffDeg   = (float) (wobYaw   * amp)            + impulseYaw;
        float pitchOffDeg = (float) (wobPitch * amp * 0.75f)    + impulsePitch; // pitch’i düşük tut
        float rollOffDeg  = (float) (wobRoll  * amp * 0.85f)    + impulseRoll;

        e.setYaw(e.getYaw() + yawOffDeg);
        e.setPitch(e.getPitch() + pitchOffDeg);
        e.setRoll(e.getRoll() + rollOffDeg);
    }
}
