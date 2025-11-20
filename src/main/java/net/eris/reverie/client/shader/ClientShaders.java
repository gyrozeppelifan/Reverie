package net.eris.reverie.client.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.eris.reverie.ReverieMod;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientShaders {
    public static ShaderInstance RED_GLOW;

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent e) throws IOException {
        e.registerShader(
                new ShaderInstance(
                        e.getResourceProvider(),
                        new ResourceLocation(ReverieMod.MODID, "reverie_redglow"),
                        DefaultVertexFormat.NEW_ENTITY
                ),
                s -> RED_GLOW = s
        );
    }
}

