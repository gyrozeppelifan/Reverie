package net.eris.reverie.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.gui.StitchedMenu;
import net.eris.reverie.network.packet.ServerboundNameStitchedPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.network.PacketDistributor;

public class StitchedScreen extends AbstractContainerScreen<StitchedMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(ReverieMod.MODID, "textures/gui/stitched_gui.png");

    private EditBox nameField;
    private Button confirmButton;

    public StitchedScreen(StitchedMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 200;
        this.imageHeight = 230;

        this.titleLabelX = -9000;
        this.inventoryLabelX = 18;
        this.inventoryLabelY = 134;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // İSİM KUTUSU
        this.nameField = new EditBox(this.font, x + 58, y + 15, 76, 13, Component.translatable("gui.reverie.name"));
        this.nameField.setMaxLength(16);
        this.nameField.setBordered(false);
        this.nameField.setTextColor(0xFFFFFF);

        if (this.menu.getStitched().hasCustomName()) {
            this.nameField.setValue(this.menu.getStitched().getCustomName().getString());
        }
        this.addRenderableWidget(this.nameField);

        // --- ŞEFFAF BUTON AYARI ---
        // Butonu anonim class olarak oluşturuyoruz ve renderWidget metodunu eziyoruz.
        this.confirmButton = new Button.Builder(Component.empty(), (btn) -> {
            String newName = nameField.getValue();
            if (!newName.isEmpty()) {
                ReverieMod.PACKET_HANDLER.send(PacketDistributor.SERVER.noArg(),
                        new ServerboundNameStitchedPacket(this.menu.getStitched().getId(), newName));
            }
        }).bounds(x + 136, y + 13, 12, 12).build();

        // Burası önemli: Varsayılan buton çizimini iptal etmek için kurnazlık yapabiliriz
        // VEYA direkt butonun alpha değerini sıfırlayabiliriz ama hover efekti istiyoruz.
        // En temiz yöntem butonu eklemek ama render'da özel işlem yapmak.
        // Ancak Java'da "inline override" Button classı final olmadığı için yapılabilir ama Builder pattern biraz zorlar.
        // O yüzden 'Button' yerine 'ImageButton' veya özel render mantığı kullanalım.

        // YÖNTEM 2: Butonu ekle ama renderBg'de üstüne çizim yapma.
        // Minecraft butonu otomatik çizer. Bunu engellemek için 'confirmButton.alpha = 0' yapabiliriz
        // ama o zaman hover efekti de gider.

        // EN KOLAY YÖNTEM: Kendi widget'ımızı yazmak yerine butonu ekleyelim
        // ve alpha'sını 0 yapalım. Zaten senin GUI resminde "tık" işareti var diye tahmin ediyorum?
        // Eğer GUI resminde işaret varsa:
        this.confirmButton.setAlpha(0.0F); // Tamamen görünmez olur ama tıklanır.

        this.addRenderableWidget(this.confirmButton);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        // Eğer butonun üstüne gelindiyse hafif bir parlama (Highlight) yapalım
        // Çünkü butonun kendisi görünmez (Alpha 0)
        if (this.confirmButton.isHoveredOrFocused()) {
            // x + 136, y + 13 koordinatına 12x12'lik yarı şeffaf beyaz kutu çiz
            guiGraphics.fill(x + 136, y + 13, x + 136 + 12, y + 13 + 12, 0x40FFFFFF); // %25 Opaklıkta Beyaz
        }

        // MOB RENDER
        int modelX = x + 51;
        int modelY = y + 120;

        InventoryScreen.renderEntityInInventoryFollowsMouse(
                guiGraphics,
                modelX, modelY,
                18,
                (float)(modelX) - mouseX,
                (float)(modelY - 50) - mouseY,
                this.menu.getStitched()
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.nameField.isFocused()) {
            if (keyCode == 256) {
                this.minecraft.player.closeContainer();
            }
            return this.nameField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}