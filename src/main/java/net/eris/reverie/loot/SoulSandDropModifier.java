package net.eris.reverie.loot;

import com.google.common.base.Supplier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.eris.reverie.init.ReverieModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class SoulSandDropModifier extends LootModifier {
    public static final Supplier<Codec<SoulSandDropModifier>> CODEC = () ->
            RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, SoulSandDropModifier::new));

    public SoulSandDropModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // %5 Şansla Spirit Fragment ekle
        if (context.getRandom().nextFloat() < 0.05f) {
            generatedLoot.add(new ItemStack(ReverieModItems.SPIRIT_FRAGMENT.get()));
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }

    // Register işlemleri
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLM_REGISTRY =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, "reverie");

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> SOUL_SAND_DROP =
            GLM_REGISTRY.register("soul_sand_drop", CODEC);
}