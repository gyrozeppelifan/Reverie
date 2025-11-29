package net.eris.reverie.gui;

import net.eris.reverie.entity.StitchedEntity;
import net.eris.reverie.init.ReverieModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class StitchedMenu extends AbstractContainerMenu {
    private final StitchedEntity stitched;
    private final IItemHandler itemHandler;

    public StitchedMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, (StitchedEntity) playerInv.player.level().getEntity(extraData.readInt()));
    }

    public StitchedMenu(int containerId, Inventory playerInv, StitchedEntity entity) {
        super(ReverieModMenus.STITCHED_MENU.get(), containerId);
        this.stitched = entity;
        this.itemHandler = entity.inventory;

        // --- STITCHED SLOTLARI (Ortalanmış) ---
        // Verdiğin koordinatlara +2 ekledim ki 20x20 kutunun tam ortasına (16x16 item) otursun.

        // KAFA (129+2, 38+2)
        this.addSlot(new SlotItemHandler(itemHandler, 0, 131, 40));

        // GÖVDE (128+2, 70+2)
        this.addSlot(new SlotItemHandler(itemHandler, 1, 130, 72));

        // KOL 1 (95+2, 79+2)
        this.addSlot(new SlotItemHandler(itemHandler, 2, 97, 81));

        // KOL 2 (164+2, 78+2)
        this.addSlot(new SlotItemHandler(itemHandler, 3, 166, 80));


        // --- OYUNCU ENVANTERİ (Hizalanmış) ---
        // Envanter X:17, Hotbar X:18 idi. İkisini de 18 yaptım ki dümdüz olsun.

        int invX = 19;
        int invY = 148; // Envanter Y

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, invX + col * 18, invY + row * 18));
            }
        }

        // Hotbar Y (205)
        int hotbarY = 205;

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, invX + col * 18, hotbarY));
        }
    }

    public StitchedEntity getStitched() {
        return this.stitched;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.stitched.isAlive() && this.stitched.distanceTo(player) < 8.0F;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 4) {
                if (!this.moveItemStackTo(itemstack1, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 4, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }
}