package net.eris.reverie.item;

import net.eris.reverie.entity.SpikedLogEntity;
import net.eris.reverie.init.ReverieModEntities;
import net.eris.reverie.init.ReverieModSounds;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SpikedLogItemItem extends Item {
    public SpikedLogItemItem() {
        super(new Item.Properties().stacksTo(8).rarity(Rarity.COMMON));
    }

@Override
public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    if (!level.isClientSide) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 spawnBase = player.getEyePosition();

        // 1.8 blok öne, 0.5 blok yukarıya offset
        Vec3 spawnPos = spawnBase.add(look.x * 1.8, 0.5, look.z * 1.8);

        // Entity'yi oluştur
        SpikedLogEntity log = new SpikedLogEntity(ReverieModEntities.SPIKED_LOG.get(), level);
        log.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, player.getYRot(), 0);

        // OWNER’I ATA!
        log.setOwner(player);

            // OWNER ADINI DA AÇIKLA ki client skin’i değiştirsin
         log.setOwnerName(player.getName().getString());

        // Entity'yi dünyaya ekle
        level.addFreshEntity(log);

        // Ses efekti
        level.playSound(
            null,
            spawnPos.x, spawnPos.y, spawnPos.z,
            ReverieModSounds.SPIKED_LOG_THROW.get(),
            SoundSource.PLAYERS,
            1f, 1f
        );

        // Creative değilse item azalt
        if (!player.getAbilities().instabuild) {
            player.getItemInHand(hand).shrink(1);
        }

           player.getCooldowns().addCooldown(this, 120);
    }

    return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
}

}
