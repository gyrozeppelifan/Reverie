package net.eris.reverie.init;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.gui.StitchedMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ReverieModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ReverieMod.MODID);

    // Menü Kaydı
    public static final RegistryObject<MenuType<StitchedMenu>> STITCHED_MENU = MENUS.register("stitched_menu",
            () -> IForgeMenuType.create(StitchedMenu::new));
}