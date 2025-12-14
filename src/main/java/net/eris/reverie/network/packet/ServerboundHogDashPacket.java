package net.eris.reverie.network.packet;

import net.eris.reverie.entity.HogEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundHogDashPacket {

    public ServerboundHogDashPacket() {
    }

    public ServerboundHogDashPacket(FriendlyByteBuf buffer) {
    }

    public void toBytes(FriendlyByteBuf buffer) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // BURASI SUNUCU TARAFI
            ServerPlayer player = context.getSender();
            // Oyuncu Hog üzerinde mi?
            if (player != null && player.getVehicle() instanceof HogEntity hog) {
                // Evet üzerinde, o zaman Dash'i ateşle!
                hog.triggerDash();
            }
        });
        return true;
    }
}