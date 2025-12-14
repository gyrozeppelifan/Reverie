package net.eris.reverie.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.network.packet.ServerboundHogDashPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID, value = Dist.CLIENT)
public class ReverieKeyHandler {

    // G Tuşunu Tanımlıyoruz
    public static final KeyMapping HOG_DASH_KEY = new KeyMapping(
            "key.reverie.hog_dash", // Tuşun dil dosyasındaki adı
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G, // Varsayılan tuş: G
            "key.categories.reverie" // Ayarlardaki kategori
    );

    // Tuşu Oyuna Kaydediyoruz
    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(HOG_DASH_KEY);
    }

    // Tuşa Basılınca Ne Olacak?
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (HOG_DASH_KEY.consumeClick()) {
            // Eğer oyuncu Hog üzerindeyse sunucuya haber ver
            if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getVehicle() != null) {
                // Postacıyı yola çıkar
                ReverieMod.PACKET_HANDLER.sendToServer(new ServerboundHogDashPacket());
            }
        }
    }
}