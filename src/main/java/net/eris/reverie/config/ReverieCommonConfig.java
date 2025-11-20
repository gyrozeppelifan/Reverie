package net.eris.reverie.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ReverieCommonConfig {
    public static final ForgeConfigSpec SPEC;

    // Goblin Brute’un ateşe atma cooldown’ı (tick) — 20 tick = 1 sn
    public static final ForgeConfigSpec.IntValue GOBLIN_BRUTE_FIRE_THROW_COOLDOWN_TICKS;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("goblin_brute");
        GOBLIN_BRUTE_FIRE_THROW_COOLDOWN_TICKS = b
                .comment("Cooldown between fire-throws (in ticks). 20 = 1s. Default = 800 (=40s).")
                .defineInRange("fire_throw_cooldown_ticks", 800, 0, 20000);
        b.pop();

        SPEC = b.build();
    }

    private ReverieCommonConfig() {}
}
