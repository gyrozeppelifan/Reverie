package net.eris.reverie.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.eris.reverie.ReverieMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.List;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID)
public final class RevGlowCommand {
    private RevGlowCommand() {}

    public static final String DEBUG_TAG = "reverie:glow_test";

    @SubscribeEvent
    public static void register(RegisterCommandsEvent e) {
        var d = e.getDispatcher();

        d.register(Commands.literal("revglow")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("on")
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .executes(ctx -> toggle(ctx.getSource(), EntityArgument.getEntities(ctx, "targets"), true)))
                        .executes(ctx -> toggleSelf(ctx.getSource(), true)))
                .then(Commands.literal("off")
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .executes(ctx -> toggle(ctx.getSource(), EntityArgument.getEntities(ctx, "targets"), false)))
                        .executes(ctx -> toggleSelf(ctx.getSource(), false)))
        );
    }

    private static int toggleSelf(CommandSourceStack src, boolean enable) throws CommandSyntaxException {
        return toggle(src, List.of(src.getPlayerOrException()), enable);
    }

    private static int toggle(CommandSourceStack src, Collection<? extends Entity> targets, boolean enable) {
        int affected = 0;
        for (var ent : targets) {
            if (ent instanceof LivingEntity le) {
                boolean changed = enable ? le.addTag(DEBUG_TAG) : le.removeTag(DEBUG_TAG);
                if (changed) affected++;
            }
        }

        final int total = affected; // <- lambda için final kopya
        src.sendSuccess(() -> Component.literal(
                (enable ? "Enabled" : "Disabled") + " glow on " + total + " entit" + (total == 1 ? "y" : "ies")
        ), false);

        return total;
    }
}
