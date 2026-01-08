package net.eris.reverie.init;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.enchantment.*; // Importları kısalttım, hepsini çeker
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ReverieModEnchantments {
    public static final DeferredRegister<Enchantment> REGISTRY = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, ReverieMod.MODID);

    // --- MEVCUT BÜYÜLERİN (Bunlar zaten vardı) ---
    public static final RegistryObject<Enchantment> SPIRIT_GUARD = REGISTRY.register("spirit_guard", SpiritGuardEnchantment::new);
    public static final RegistryObject<Enchantment> INNER_PEACE = REGISTRY.register("inner_peace", InnerPeaceEnchantment::new);
    public static final RegistryObject<Enchantment> SPIRIT_DEFLECTION = REGISTRY.register("spirit_deflection", SpiritDeflectionEnchantment::new);
    public static final RegistryObject<Enchantment> NATURES_BREATH = REGISTRY.register("natures_breath", NaturesBreathEnchantment::new);

    // --- YENİ EKLENEN SPIKED LOG BÜYÜLERİ ---
    public static final RegistryObject<Enchantment> WILDFIRE = REGISTRY.register("wildfire", WildfireEnchantment::new);
    public static final RegistryObject<Enchantment> MOMENTUM = REGISTRY.register("momentum", MomentumEnchantment::new);
    public static final RegistryObject<Enchantment> RICOCHET = REGISTRY.register("ricochet", RicochetEnchantment::new);
    public static final RegistryObject<Enchantment> VORTEX = REGISTRY.register("vortex", VortexEnchantment::new);
    public static final RegistryObject<Enchantment> RECALL = REGISTRY.register("recall", RecallEnchantment::new);
}