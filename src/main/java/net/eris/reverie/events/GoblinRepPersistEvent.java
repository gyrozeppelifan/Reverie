package net.eris.reverie.events;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.nbt.CompoundTag;

public class GoblinRepPersistEvent {

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag original = event.getOriginal().getPersistentData();
        CompoundTag clone = event.getEntity().getPersistentData();

        // Sadece goblin rep'i taşıyoruz
        if (original.contains("goblinReputation")) {
            clone.putInt("goblinReputation", original.getInt("goblinReputation"));
        }
    }
}
