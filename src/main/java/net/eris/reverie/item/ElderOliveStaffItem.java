package net.eris.reverie.item;

import net.eris.reverie.entity.GoblinBarrelEntity;
import net.eris.reverie.init.ReverieModSounds;
import net.eris.reverie.util.GoblinReputation;
import net.eris.reverie.util.GoblinReputation.State;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.core.Holder;

public class ElderOliveStaffItem extends Item {
    public ElderOliveStaffItem() {
        super(new Item.Properties().stacksTo(1));
    }

    private static final String STATE_KEY = "BarrelStaffState";
    private static final String READY = "READY";
    private static final String IDLE = "IDLE";

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }

        String current = stack.getOrCreateTag().getString(STATE_KEY);

        // READY değilse önce rep kontrolü
        if (!READY.equals(current)) {
            State rep = GoblinReputation.getState(player);
            if (!(rep == State.FRIENDLY || rep == State.HELPFUL)) {
                // Reddetme mesajı ve sesi
                serverPlayer.connection.send(
                        new ClientboundSoundPacket(
                                Holder.direct(ReverieModSounds.GOBLIN_STAFF_DECLINE.get()),
                                SoundSource.PLAYERS,
                                serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                                1.4F, 0.9F,
                                serverPlayer.level().getRandom().nextLong()
                        )
                );
                serverPlayer.displayClientMessage(
                        Component.translatable("message.reverie.barrel_rep_too_low")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }

            stack.getOrCreateTag().putString(STATE_KEY, READY);
            serverPlayer.displayClientMessage(
                    Component.translatable("message.reverie.barrel_approved"),
                    true
            );
            return InteractionResultHolder.success(stack);
        }

        // READY ise, shift+sağ tıkta çağır
        if (player.isShiftKeyDown() && READY.equals(current)) {
            BlockPos base = player.blockPosition();
            boolean blocked = false;
            for (int y = base.getY() + 1; y <= base.getY() + 128; y++) {
                if (!world.isEmptyBlock(new BlockPos(base.getX(), y, base.getZ()))) {
                    blocked = true;
                    break;
                }
            }
            if (blocked) {
                serverPlayer.connection.send(
                        new ClientboundSoundPacket(
                                Holder.direct(ReverieModSounds.GOBLIN_STAFF_DECLINE.get()),
                                SoundSource.PLAYERS,
                                serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                                1.4F, 0.9F,
                                serverPlayer.level().getRandom().nextLong()
                        )
                );
                serverPlayer.displayClientMessage(
                        Component.translatable("message.reverie.barrel_block_not_clear")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }

            // Fıçı spawnla
            double spawnX = player.getX();
            double spawnY = player.getY() + 125.0;
            double spawnZ = player.getZ();

            GoblinBarrelEntity barrel = new GoblinBarrelEntity(
                    world, spawnX, spawnY, spawnZ, player.getUUID()
            );
            barrel.setYRot(player.getYRot());
            barrel.setXRot(player.getXRot());
            barrel.setOwnerName(player.getName().getString());
            world.addFreshEntity(barrel);

            for (ServerPlayer p : serverPlayer.server.getPlayerList().getPlayers()) {
                p.connection.send(
                        new ClientboundSoundPacket(
                                Holder.direct(ReverieModSounds.GOBLIN_BARREL_SUMMON.get()),
                                SoundSource.PLAYERS, // PLAYERS: kulaklık efekti, bloktan bağımsız
                                p.getX(), p.getY(), p.getZ(),
                                0.7F, 1.0F, // volume, pitch (daha yüksek ister misin? 2.0F da yapabilirsin)
                                p.level().getRandom().nextLong()
                        )
                );
            }

            stack.getOrCreateTag().putString(STATE_KEY, IDLE);
            player.getCooldowns().addCooldown(this, 800);
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return READY.equals(stack.getOrCreateTag().getString(STATE_KEY)) || super.isFoil(stack);
    }
}
