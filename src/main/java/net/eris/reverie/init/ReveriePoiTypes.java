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

    // 3. TERZİ (TAILOR) - YENİ
    public static final RegistryObject<PoiType> TAILOR_POI = POI_TYPES.register("tailor_poi",
            () -> new PoiType(getAllStates(ReverieModBlocks.SEWING_STATION.get()), 1, 1));

    // 4. SEYİS (STABLE MASTER) - YENİ
    public static final RegistryObject<PoiType> STABLE_MASTER_POI = POI_TYPES.register("stable_master_poi",
            () -> new PoiType(getAllStates(ReverieModBlocks.SADDLE_RACK.get()), 1, 1));

    // 5. BANKACI (BANKER) - YENİ
    public static final RegistryObject<PoiType> BANKER_POI = POI_TYPES.register("banker_poi",
            () -> new PoiType(getAllStates(ReverieModBlocks.VAULT.get()), 1, 1));

    // 6. ÖDÜL MEMURU (BOUNTY CLERK) - YENİ
    public static final RegistryObject<PoiType> BOUNTY_CLERK_POI = POI_TYPES.register("bounty_clerk_poi",
            () -> new PoiType(getAllStates(ReverieModBlocks.BOUNTY_BOARD.get()), 1, 1));

    // 7. CENAZE LEVAZIMATÇISI (UNDERTAKER) - YENİ
    public static final RegistryObject<PoiType> UNDERTAKER_POI = POI_TYPES.register("undertaker_poi",
            () -> new PoiType(getAllStates(ReverieModBlocks.COFFIN_TRESTLE.get()), 1, 1));

    public static final RegistryObject<PoiType> MEETING_POI = POI_TYPES.register("meeting_poi",
            () -> new PoiType(getAllStates(net.minecraft.world.level.block.Blocks.RAW_GOLD_BLOCK), 1, 1));


    private static Set<BlockState> getAllStates(Block block) {
        return ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates());
    }

    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
    }
}