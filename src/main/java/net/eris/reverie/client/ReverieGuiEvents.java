package net.eris.reverie.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.util.IAncientCloakData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// BUS = FORGE olduğuna dikkat et!
@Mod.EventBusSubscriber(modid = ReverieMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ReverieGuiEvents {

    // GUI'nin en son aşamasında çizim yapıyoruz ki her şeyin üstünde olsun.
    @SubscribeEvent
    public static void onRenderGuiOverlayPost(RenderGuiOverlayEvent.Post event) {
        // Sadece ana HUD çizildikten sonra çalış (Gereksiz yere çalışmasın)
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        // Oyuncu yoksa veya hayatta değilse çizme
        if (player == null || !player.isAlive()) return;

        // --- KRİTİK NOKTA: MIXIN VERİSİ KONTROLÜ ---
        // Potion effect yerine garantili veri kanalına bakıyoruz.
        if (!((IAncientCloakData) player).reverie$hasDrunkenRage()) {
            return;
        }

        // Shader yüklendi mi kontrol et
        if (ReverieClientEvents.drunkenRageShader == null) return;

        // --- ÇİZİM BAŞLIYOR ---
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Kendi shader'ımızı aktif et
        RenderSystem.setShader(() -> ReverieClientEvents.drunkenRageShader);

        // Shader zamanını güncelle (Animasyon için şart!)
        ReverieClientEvents.drunkenRageShader.getUniform("GameTime").set((player.tickCount + event.getPartialTick()) / 20.0F);

        // Tüm ekranı kaplayan bir dörtgen çiz
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(0.0D, height, -90.0D).uv(0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(width, height, -90.0D).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(width, 0.0D, -90.0D).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(0.0D, 0.0D, -90.0D).uv(0.0F, 0.0F).endVertex();
        tesselator.end();

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}