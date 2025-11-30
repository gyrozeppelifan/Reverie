package net.eris.reverie.client.particle.lightning;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class CosmeticLightningBolt {
    private final List<List<Vec3>> allBranches = new ArrayList<>();

    private final Vec3 start;
    private final Vec3 end;
    private final RandomSource random;

    // --- AYAR 3: DAHA ŞEFFAF RENK ---
    // Alpha 0.6 -> 0.4 (Daha az göz yorar)
    public final Vector4f color = new Vector4f(0.0F, 1.0F, 1.0F, 0.4F);

    public CosmeticLightningBolt(Vec3 start, Vec3 end, RandomSource random) {
        this.start = start;
        this.end = end;
        this.random = random;
        this.generateFullBolt();
    }

    private void generateFullBolt() {
        allBranches.clear();

        // Ana Gövde (Vahşilik 0.5, bu iyiydi)
        List<Vec3> mainBranch = generateSingleBranch(start, end, 0.5);
        allBranches.add(mainBranch);

        // --- AYAR 4: DAHA AZ DAL ---
        int maxBranches = 2; // En fazla 2 dal (Eskiden 3)
        int currentBranches = 0;

        for (int i = 1; i < mainBranch.size() - 1; i++) {
            // --- ŞANS DÜŞÜRÜLDÜ ---
            // %35 -> %12 (Sadece ara sıra dal çıksın)
            if (random.nextFloat() < 0.12f && currentBranches < maxBranches) {
                Vec3 branchStart = mainBranch.get(i);

                Vec3 offset = new Vec3(
                        (random.nextDouble() - 0.5) * 3.5,
                        (random.nextDouble() - 0.5) * 3.5,
                        (random.nextDouble() - 0.5) * 3.5
                );
                Vec3 branchEnd = branchStart.add(offset);

                List<Vec3> subBranch = generateSingleBranch(branchStart, branchEnd, 0.3);
                allBranches.add(subBranch);
                currentBranches++;
            }
        }
    }

    private List<Vec3> generateSingleBranch(Vec3 pStart, Vec3 pEnd, double wildness) {
        double distance = pStart.distanceTo(pEnd);
        int generations = (int) Math.max(3, Math.ceil(distance / 1.5));

        List<Vec3> currentPoints = new ArrayList<>();
        currentPoints.add(pStart);
        currentPoints.add(pEnd);

        for (int i = 0; i < generations; i++) {
            List<Vec3> nextPoints = new ArrayList<>();
            double offsetScale = (distance / 2.0) / (i + 1) * wildness;

            for (int j = 0; j < currentPoints.size() - 1; j++) {
                Vec3 p1 = currentPoints.get(j);
                Vec3 p2 = currentPoints.get(j + 1);
                nextPoints.add(p1);

                Vec3 mid = p1.add(p2).scale(0.5);
                mid = mid.add(
                        (random.nextDouble() - 0.5) * offsetScale,
                        (random.nextDouble() - 0.5) * offsetScale,
                        (random.nextDouble() - 0.5) * offsetScale
                );
                nextPoints.add(mid);
            }
            nextPoints.add(currentPoints.get(currentPoints.size() - 1));
            currentPoints = nextPoints;
        }
        return currentPoints;
    }

    public List<List<Vec3>> getAllBranches() {
        return allBranches;
    }
}