package net.eris.reverie.events;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.init.ReverieModAttributes; // Yeni dosyamız
import net.eris.reverie.init.ReverieModItems;
import net.eris.reverie.init.ReverieModParticleTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID)
public class BoarMonkEvents {

    // 1. Domuz doğduğunda şans faktörü
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Pig pig && !event.getLevel().isClientSide) {
            // Null kontrolü önemli (Attribute henüz yüklenmemiş olabilir)
            if (ReverieModAttributes.SPIRITUALITY == null) return;

            AttributeInstance spirituality = pig.getAttribute(ReverieModAttributes.SPIRITUALITY);

            if (spirituality != null && spirituality.getValue() == 0.0D) {
                // %50 Şansla (Test için yüksek, sonra 0.02f yaparsın)
                if (event.getLevel().random.nextFloat() < 0.5f) {
                    spirituality.setBaseValue(1.0D);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Pig pig && event.getEntity().level().isClientSide) {

            // Attribute Kontrolü
            if (ReverieModAttributes.SPIRITUALITY == null) return;
            Double value = pig.getAttributeValue(ReverieModAttributes.SPIRITUALITY);

            if (value != null && value > 0.5D) {
                // --- MONK ENERJİSİ ---
                // Her tick 1-2 partikül spawnla
                if (pig.getRandom().nextFloat() < 0.3f) { // Çok yoğun olmasın, %30 şansla her tick

                    // Merkez: Domuzun tam ortası
                    double x = pig.getX();
                    double y = pig.getY() + 0.5D; // Gövde ortası
                    double z = pig.getZ();

                    // Hız parametrelerini "Rastgele Açı Başlangıcı" olarak kullanıyoruz
                    double randomSeed = pig.getRandom().nextDouble();

                    pig.level().addParticle(ReverieModParticleTypes.SPIRIT_ORB.get(),
                            x, y, z,
                            randomSeed, 0.0, 0.0); // xSpeed parametresini tohum olarak gönderdik
                }
            }
        }
    }

    // 3. Oyuncu Etkileşimi
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof Pig pig) {
            if (ReverieModAttributes.SPIRITUALITY == null) return;

            Double value = pig.getAttributeValue(ReverieModAttributes.SPIRITUALITY);

            if (value != null && value > 0.5D) {
                Player player = event.getEntity();
                ItemStack itemInHand = player.getItemInHand(event.getHand());

                if (itemInHand.is(Items.PAPER)) {
                    if (!event.getLevel().isClientSide) {
                        if (!player.getAbilities().instabuild) itemInHand.shrink(1);

                        ItemStack scroll = new ItemStack(ReverieModItems.BOAR_WHISPERER_SCROLL.get());
                        if (!player.getInventory().add(scroll)) player.drop(scroll, false);

                        event.getLevel().playSound(null, pig.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);

                        // Özelliği sıfırla
                        pig.getAttribute(ReverieModAttributes.SPIRITUALITY).setBaseValue(0.0D);
                    }
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }
        }
    }
}