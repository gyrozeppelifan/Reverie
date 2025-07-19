package net.eris.reverie.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.Collection;

public class GoblinReputation {

    public static final TagKey<EntityType<?>> GOBLINS_TAG =
            TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("reverie", "goblins"));

    public static final String TAG = "goblinReputation";
    public static final int MIN = -100;
    public static final int MAX = 100;
    public static final int DEFAULT = 40; // İlk girişte başlangıç puanı

    public static void ensureDefault(Player player) {
        if (!player.getPersistentData().contains(TAG)) {
            set(player, DEFAULT);
        }
    }

    public static int get(Player player) {
        ensureDefault(player);
        return player.getPersistentData().getInt(TAG);
    }

    public static void set(Player player, int value) {
        player.getPersistentData().putInt(TAG, clamp(value));
    }

    public static void add(Player player, int delta) {
        set(player, get(player) + delta);
    }

    private static int clamp(int value) {
        return Math.max(MIN, Math.min(MAX, value));
    }

    // SADECE TAG KONTROLÜ! Artık bütün goblin türleri için çalışır:
    public static boolean isGoblin(Entity entity) {
        return entity.getType().is(GOBLINS_TAG);
    }

    public enum State {
        AGGRESSIVE, NEUTRAL, FRIENDLY, HELPFUL
    }

    public static State getState(Player player) {
        int value = get(player);
        if (value >= 80) return State.HELPFUL;
        if (value >= 50) return State.FRIENDLY;
        if (value >= 0)  return State.NEUTRAL;
        return State.AGGRESSIVE;
    }

    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("goblinmeter")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    sendRepInfo(ctx.getSource(), player);
                    return 1;
                })
                .then(Commands.argument("target", EntityArgument.players())
                        .executes(ctx -> {
                            Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "target");
                            for (ServerPlayer player : players) {
                                sendRepInfo(ctx.getSource(), player);
                            }
                            return 1;
                        })
                        .then(Commands.argument("value", IntegerArgumentType.integer(MIN, MAX))
                                .executes(ctx -> {
                                    Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "target");
                                    int val = IntegerArgumentType.getInteger(ctx, "value");
                                    for (ServerPlayer player : players) {
                                        set(player, val);
                                        ctx.getSource().sendSuccess(() ->
                                                Component.translatable(
                                                        "command.reverie.goblinmeter.set",
                                                        player.getName(),
                                                        val,
                                                        getState(player)
                                                ), false);
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }

    private static void sendRepInfo(CommandSourceStack source, ServerPlayer player) {
        source.sendSuccess(() -> Component.translatable(
                "command.reverie.goblinmeter.show",
                player.getName(),
                get(player),
                getState(player)
        ), false);
    }

    // Olay tetikleyiciler: tag tabanlı!
    public static void onGoblinHurt(Entity goblin, Player player) {
        if (isGoblin(goblin)) {
            GoblinReputation.add(player, -10);
        }
    }

    public static void onGoblinFed(Entity goblin, Player player) {
        if (isGoblin(goblin)) {
            GoblinReputation.add(player, +5);
        }
    }
}
