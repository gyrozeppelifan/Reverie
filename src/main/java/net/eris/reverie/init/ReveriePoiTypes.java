package net.eris.reverie.init;

import com.google.common.collect.ImmutableSet;
import net.eris.reverie.ReverieMod;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ReveriePoiTypes {
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, ReverieMod.MODID);

    // Barmen için Saloon Bar bloğunu POI yapıyoruz.
    // Şimdilik test için Blocks.BARREL (Varil) kullanabilirsin, sonra kendi bloğunu eklersin.
    public static final RegistryObject<PoiType> BARKEEPER_POI = POI_TYPES.register("barkeeper_poi",
            () -> new PoiType(ImmutableSet.copyOf(ReverieModBlocks.SALOON_BAR.get().getStateDefinition().getPossibleStates()),
                    1, 1)); // 1: Max kapasite, 1: Arama menzili

    // Bankacı için POI (Örn: Altın Bloğu)
    public static final RegistryObject<PoiType> BANKER_POI = POI_TYPES.register("banker_poi",
            () -> new PoiType(ImmutableSet.copyOf(Blocks.GOLD_BLOCK.getStateDefinition().getPossibleStates()), 1, 1));
}