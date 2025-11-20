package net.eris.reverie.fx;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.client.network.ScreenShakeS2CPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public final class ScreenShake {
    private ScreenShake() {}

    public static void broadcast(ServerLevel level, Vec3 center, double radius,
                                 int durationTicks, float amplitudeDeg) {
        final double r2 = radius * radius;
        for (ServerPlayer sp : level.getServer().getPlayerList().getPlayers()) {
            if (sp.level() != level) continue;
            if (sp.position().distanceToSqr(center) <= r2) {
                ReverieMod.PACKET_HANDLER.send(
                        PacketDistributor.PLAYER.with(() -> sp),
                        new ScreenShakeS2CPacket(durationTicks, amplitudeDeg)
                );
            }
        }
    }
}
