package net.eris.reverie.client.particle.lightning;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class CosmeticLightningBolt {
    private final List<Vec3> segments = new ArrayList<>();
    private final Vec3 start;
    private final Vec3 end;
    private final RandomSource random;

    // Renk: R, G, B, Alpha (Parlak Camgöbeği)
    public final Vector4f color = new Vector4f(0.0F, 1.0F, 1.0F, 0.8F);

    public CosmeticLightningBolt(Vec3 start, Vec3 end, RandomSource random) {
        this.start = start;
        this.end = end;
        this.random = random;
        this.generate();
    }

    private void generate() {
        segments.clear();
        segments.add(start);

        // Toplam mesafe
        double distance = start.distanceTo(end);
        // Kaç kırılma olacağı (Mesafeye göre artar)
        int generations = (int) Math.max(2, Math.ceil(distance / 2.0));

        // Başlangıç ve Bitiş arasındaki ana hat
        List<Vec3> currentPoints = new ArrayList<>();
        currentPoints.add(start);
        currentPoints.add(end);

        // Kırılma Döngüsü (Fraktal Üretim)
        for (int i = 0; i < generations; i++) {
            List<Vec3> nextPoints = new ArrayList<>();
            double offsetScale = (distance / 2.0) / (i + 1) * 0.2; // Kırılma şiddeti

            for (int j = 0; j < currentPoints.size() - 1; j++) {
                Vec3 p1 = currentPoints.get(j);
                Vec3 p2 = currentPoints.get(j + 1);

                nextPoints.add(p1);

                // Orta noktayı bul ve rastgele saptır
                Vec3 mid = p1.add(p2).scale(0.5);
                mid = mid.add(
                        (random.nextDouble() - 0.5) * offsetScale,
                        (random.nextDouble() - 0.5) * offsetScale,
                        (random.nextDouble() - 0.5) * offsetScale
                );
                nextPoints.add(mid);
            }
            nextPoints.add(currentPoints.get(currentPoints.size() - 1)); // Son noktayı ekle
            currentPoints = nextPoints;
        }

        this.segments.addAll(currentPoints);
    }

    public List<Vec3> getSegments() {
        return segments;
    }
}