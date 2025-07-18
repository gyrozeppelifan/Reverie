package net.eris.reverie.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class SpikedLogPlaybackConditionProcedure {
	@SubscribeEvent
	public static void onEntitySpawned(EntityJoinLevelEvent event) {
		execute(event);
	}

	public static boolean execute() {
		return execute(null);
	}

	private static boolean execute(@Nullable Event event) {
		return true;
	}
}
