package net.eris.reverie.init;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.enchantment.InnerPeaceEnchantment;
import net.eris.reverie.enchantment.NaturesBreathEnchantment;
import net.eris.reverie.enchantment.SpiritDeflectionEnchantment;
import net.eris.reverie.enchantment.SpiritGuardEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ReverieModEnchantments {
    public static final DeferredRegister<Enchantment> REGISTRY = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, ReverieMod.MODID);

    public static final RegistryObject<Enchantment> INNER_PEACE = REGISTRY.register("inner_peace",
            () -> new InnerPeaceEnchantment());

    public static final RegistryObject<Enchantment> SPIRIT_GUARD = REGISTRY.register("spirit_guard",
            () -> new SpiritGuardEnchantment());

    // --- YENİLER ---
    public static final RegistryObject<Enchantment> SPIRIT_DEFLECTION = REGISTRY.register("spirit_deflection",
            () -> new SpiritDeflectionEnchantment());

    public static final RegistryObject<Enchantment> NATURES_BREATH = REGISTRY.register("natures_breath",
            () -> new NaturesBreathEnchantment());
}