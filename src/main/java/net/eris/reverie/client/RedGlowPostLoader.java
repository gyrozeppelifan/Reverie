// net.eris.reverie.client.RedGlowPostLoader
package net.eris.reverie.client;

import net.eris.reverie.ReverieMod;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID, value = Dist.CLIENT)
public final class RedGlowPostLoader {
    private static boolean LOADED = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        var mc = Minecraft.getInstance();
        if (mc.player == null || LOADED) return;

        // assets/reverie/shaders/post/redglow.json
        mc.gameRenderer.loadEffect(new ResourceLocation(ReverieMod.MODID, "redglow"));
        LOADED = true;
    }
}
