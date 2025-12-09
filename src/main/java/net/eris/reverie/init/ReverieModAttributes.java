package net.eris.reverie.init;

import net.eris.reverie.ReverieMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

// Bu annotation sayesinde ReverieMod.java'ya bir şey yazmamıza gerek kalmıyor!
@Mod.EventBusSubscriber(modid = ReverieMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ReverieModAttributes {

    // Erişebilmemiz için statik değişken
    public static Attribute SPIRITUALITY;

    @SubscribeEvent
    public static void registerAttributes(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ATTRIBUTES, helper -> {
            // Attribute'u oluştur
            Attribute spiritualityAttr = new RangedAttribute("attribute.reverie.spirituality", 0.0D, 0.0D, 1.0D).setSyncable(true);

            // Oyuna kaydet
            helper.register(new ResourceLocation(ReverieMod.MODID, "spirituality"), spiritualityAttr);

            // Değişkene ata
            SPIRITUALITY = spiritualityAttr;
        });
    }

    @SubscribeEvent
    public static void modifyAttributes(EntityAttributeModificationEvent event) {
        // Domuzda bu özellik yoksa ekle
        if (SPIRITUALITY != null && !event.has(EntityType.PIG, SPIRITUALITY)) {
            event.add(EntityType.PIG, SPIRITUALITY);
        }
    }
}