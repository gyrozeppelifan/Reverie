package net.eris.reverie.init;

import net.eris.reverie.block.entity.CopperConduitBlockEntity;
import net.eris.reverie.block.entity.CopperJunctionBlockEntity;
import net.eris.reverie.init.ReverieModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ReverieModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "reverie");

    public static final RegistryObject<BlockEntityType<CopperConduitBlockEntity>> COPPER_CONDUIT =
            BLOCK_ENTITIES.register("copper_conduit",
                    () -> BlockEntityType.Builder.of(CopperConduitBlockEntity::new, ReverieModBlocks.COPPER_CONDUIT.get()).build(null)

            );

    public static final RegistryObject<BlockEntityType<CopperJunctionBlockEntity>> COPPER_JUNCTION =
            BLOCK_ENTITIES.register("copper_junction",
                    () -> BlockEntityType.Builder.of(CopperJunctionBlockEntity::new, ReverieModBlocks.COPPER_JUNCTION.get()).build(null)

            );

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
