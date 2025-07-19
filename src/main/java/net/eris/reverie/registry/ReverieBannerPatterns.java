package net.eris.reverie.registry;

import net.eris.reverie.ReverieMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ReverieBannerPatterns {

    public static final DeferredRegister<BannerPattern> BANNER_PATTERNS =
            DeferredRegister.create(Registries.BANNER_PATTERN, ReverieMod.MODID);

    public static final RegistryObject<BannerPattern> GOBLIN_SYMBOL =
            BANNER_PATTERNS.register("goblin_symbol", () -> new BannerPattern("goblin_symbol"));

    // İşte buraya ekle!
    public static final TagKey<BannerPattern> GOBLIN_SYMBOL_TAG =
            TagKey.create(Registries.BANNER_PATTERN, new ResourceLocation(ReverieMod.MODID, "goblin_symbol"));
}

