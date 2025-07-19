package net.eris.reverie.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.decoration.PaintingVariants;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ReverieModPaintings {
    public static final DeferredRegister<PaintingVariant> PAINTINGS =
            DeferredRegister.create(ForgeRegistries.PAINTING_VARIANTS, "reverie");

    public static final RegistryObject<PaintingVariant> FORMA_DIVINA_GOBLINI =
            PAINTINGS.register("forma_divina_goblini",
                    () -> new PaintingVariant(64, 64)); // Genişlik & Yükseklik (px)

    public static final RegistryObject<PaintingVariant> AXIS_DEMENTIAE =
            PAINTINGS.register("axis_dementiae",
                    () -> new PaintingVariant(16, 32)); // Genişlik & Yükseklik (px)

    public static final RegistryObject<PaintingVariant> COGITATIO_MACHINA =
            PAINTINGS.register("cogitatio_machina",
                    () -> new PaintingVariant(32, 32)); // Genişlik & Yükseklik (px)

    public static final RegistryObject<PaintingVariant> PURE_MADNESS =
            PAINTINGS.register("pure_madness",
                    () -> new PaintingVariant(32, 48)); // Genişlik & Yükseklik (px)
}
