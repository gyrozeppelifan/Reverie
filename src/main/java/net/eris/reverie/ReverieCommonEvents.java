package net.eris.reverie;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import net.eris.reverie.entity.SpikedLogEntity;
import net.eris.reverie.init.ReverieModEntities;
import net.eris.reverie.init.ReverieModItems;
import net.eris.reverie.init.ReverieModSounds;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ReverieCommonEvents {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
DispenserBlock.registerBehavior(ReverieModItems.SPIKED_LOG_ITEM.get(), new DefaultDispenseItemBehavior() {
    @Override
    protected ItemStack execute(net.minecraft.core.BlockSource source, ItemStack stack) {
        Level level = source.getLevel();
        if (!level.isClientSide) {
            Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
            double x = source.x() + direction.getStepX();
            double y = source.y();
            double z = source.z() + direction.getStepZ();

            SpikedLogEntity log = new SpikedLogEntity(ReverieModEntities.SPIKED_LOG.get(), level);
            float yaw = direction.toYRot();
            log.moveTo(x, y, z, yaw, 0);

            level.addFreshEntity(log);

            level.playSound(
                null,
                x, y, z,
                ReverieModSounds.SPIKED_LOG_THROW.get(),
                SoundSource.BLOCKS, 1f, 1f
            );
        }
        stack.shrink(1);
        return stack;
    }
});
        });
    }
}
