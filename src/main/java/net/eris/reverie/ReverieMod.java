package net.eris.reverie;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;

import net.eris.reverie.init.ReverieModTabs;
import net.eris.reverie.init.ReverieModSounds;
import net.eris.reverie.init.ReverieModParticleTypes;
import net.eris.reverie.init.ReverieModMobEffects;
import net.eris.reverie.init.ReverieModItems;
import net.eris.reverie.init.ReverieModEntities;
import net.eris.reverie.init.ReverieModBlocks;

import net.eris.reverie.util.GoblinReputation; // <-- GoblinReputation'un yolunu buraya göre ayarla
import net.eris.reverie.events.GoblinRepEvent; // <-- Event dosyanı doğru importla!

import java.util.function.Supplier;
import java.util.function.Function;
import java.util.function.BiConsumer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.AbstractMap;

@Mod("reverie")
public class ReverieMod {
	public static final Logger LOGGER = LogManager.getLogger(ReverieMod.class);
	public static final String MODID = "reverie";

	public ReverieMod() {
		MinecraftForge.EVENT_BUS.register(this);

		// *** Eventleri burada register et! ***
		MinecraftForge.EVENT_BUS.register(GoblinRepEvent.class);

		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		ReverieModSounds.REGISTRY.register(bus);
		ReverieModBlocks.REGISTRY.register(bus);
		ReverieModItems.REGISTRY.register(bus);
		ReverieModEntities.REGISTRY.register(bus);
		ReverieModTabs.REGISTRY.register(bus);
		ReverieModMobEffects.REGISTRY.register(bus);
		ReverieModParticleTypes.REGISTRY.register(bus);
	}

	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	private static int messageID = 0;

	public static <T> void addNetworkMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
		PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer);
		messageID++;
	}

	private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

	public static void queueServerWork(int tick, Runnable action) {
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
			workQueue.add(new AbstractMap.SimpleEntry<>(action, tick));
	}

	@SubscribeEvent
	public void tick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			List<AbstractMap.SimpleEntry<Runnable, Integer>> actions = new ArrayList<>();
			workQueue.forEach(work -> {
				work.setValue(work.getValue() - 1);
				if (work.getValue() == 0)
					actions.add(work);
			});
			actions.forEach(e -> e.getKey().run());
			workQueue.removeAll(actions);
		}
	}

	// *** GOBLIN KOMUTU REGISTER! ***
	@SubscribeEvent
	public void onCommandRegister(RegisterCommandsEvent event) {
		GoblinReputation.registerCommand(event.getDispatcher());
	}
}
