package net.eris.reverie.init;

import net.eris.reverie.ReverieMod;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Set;

public class ReveriePoiTypes {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(ForgeRegistries.POI_TYPES, "reverie");

    // 1. BARMEN
    public static final RegistryObject<PoiType> BARKEEPER_POI = POI_TYPES.register("barkeeper_poi",
            () -> new PoiType(getAllStates(ReverieModBlocks.SALOON_BAR.get()), 1, 1));

    // 2. SİLAHÇI (GUNSMITH)
    public static final RegistryObject<PoiType> GUNSMITH_POI = POI_TYPES.register("gunsmith_poi",
            () -> new PoiType(getAllStates(ReverieModBlocks.GUNSMITH_TABLE.get()), 1, 1));

    private static Set<BlockState> getAllStates(Block block) {
        return ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates());
    }

    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
    }
}