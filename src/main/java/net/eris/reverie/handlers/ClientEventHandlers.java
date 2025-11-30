package net.eris.reverie.handlers;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.util.IAncientCloakData; // Arayüzü import et
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEventHandlers {

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();

        // DÜZELTME: Potion Effect yerine Synched Data kontrolü yapıyoruz!
        // Bu sayede server-client gecikmesi olmadan anında titrer.
        if (entity instanceof IAncientCloakData dataHolder && dataHolder.reverie$hasZapped()) {

            // Titreme Şiddeti (0.15F gayet belirgin bir titreme yapar)
            float shakeAmount = 0.15F;

            float offsetX = (float) ((Math.random() - 0.5) * shakeAmount);
            float offsetY = (float) ((Math.random() - 0.5) * shakeAmount);
            float offsetZ = (float) ((Math.random() - 0.5) * shakeAmount);

            event.getPoseStack().translate(offsetX, offsetY, offsetZ);
        }
    }
}