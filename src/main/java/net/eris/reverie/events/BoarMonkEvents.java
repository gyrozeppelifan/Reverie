package net.eris.reverie.events;

import net.eris.reverie.ReverieMod;
import net.eris.reverie.entity.HogEntity;
import net.eris.reverie.init.ReverieModAttributes;
import net.eris.reverie.init.ReverieModEntities;
import net.eris.reverie.init.ReverieModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID)
public class BoarMonkEvents {

    public static final String TAG_TRANSFORMING = "ReverieTransformingTick";
    public static final String TAG_SCROLL_COOKING = "ReverieScrollCooking";
    public static final int RITUAL_DURATION = 100;

    // --- 1. SOUL CAMPFIRE RİTÜELİ (Wait & Transform) ---
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        Player player = event.player;
        ItemStack mainHand = player.getMainHandItem();

        if (mainHand.is(ReverieModItems.BOAR_WHISPERER_SCROLL.get())) {
            Level level = player.level();
            BlockPos pos = player.blockPosition();

            boolean nearSoulCampfire = false;
            BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
            for(int x=-1; x<=1; x++) {
                for(int y=-1; y<=1; y++) {
                    for(int z=-1; z<=1; z++) {
                        mPos.set(pos.getX()+x, pos.getY()+y, pos.getZ()+z);
                        if (level.getBlockState(mPos).is(Blocks.SOUL_CAMPFIRE)) {
                            nearSoulCampfire = true; break;
                        }
                    }
                }
            }

            CompoundTag data = player.getPersistentData();
            if (nearSoulCampfire) {
                int cookTime = data.getInt(TAG_SCROLL_COOKING);
                cookTime++;

                if (cookTime % 5 == 0 && level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1, player.getZ(), 1, 0.3, 0.5, 0.3, 0.05);
                }

                if (cookTime >= 100) {
                    if (!player.getAbilities().instabuild) mainHand.shrink(1);
                    ItemStack purified = new ItemStack(ReverieModItems.PURIFIED_SCROLL.get());
                    if (!player.getInventory().add(purified)) player.drop(purified, false);

                    level.playSound(null, pos, SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.0F, 1.0F);
                    level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 0.5F);

                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY()+1, player.getZ(), 20, 0.5, 0.5, 0.5, 0.1);
                    }

                    player.displayClientMessage(Component.translatable("reverie.message.scroll_purified"), true);
                    cookTime = 0;
                }
                data.putInt(TAG_SCROLL_COOKING, cookTime);
            } else {
                if (data.contains(TAG_SCROLL_COOKING)) data.remove(TAG_SCROLL_COOKING);
            }
        }
    }

    // --- 2. BÜYÜ MASASI RİTÜELİ (SEAL YAPIMI) ---
    @SubscribeEvent
    public static void onRitualBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide) return;

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        ItemStack heldItem = event.getItemStack();

        if (level.getBlockState(pos).is(Blocks.ENCHANTING_TABLE) && heldItem.is(ReverieModItems.PURIFIED_SCROLL.get())) {

            BlockPos[] candles = {pos.north(), pos.south(), pos.east(), pos.west()};
            boolean circleComplete = true;
            for (BlockPos p : candles) {
                if (!level.getBlockState(p).is(net.minecraft.tags.BlockTags.CANDLES)) {
                    circleComplete = false; break;
                }
            }

            if (circleComplete) {
                boolean hasFragment = player.getInventory().contains(new ItemStack(ReverieModItems.SPIRIT_FRAGMENT.get()));
                boolean hasDiamond = player.getInventory().contains(new ItemStack(Items.DIAMOND));

                if (hasFragment && hasDiamond) {
                    consumeItem(player, ReverieModItems.SPIRIT_FRAGMENT.get());
                    consumeItem(player, Items.DIAMOND);
                    if (!player.getAbilities().instabuild) heldItem.shrink(1);

                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX()+0.5, pos.getY()+1.5, pos.getZ()+0.5, 50, 0.5, 0.5, 0.5, 0.5);
                    }
                    level.playSound(null, pos, SoundEvents.ILLUSIONER_PREPARE_MIRROR, SoundSource.BLOCKS, 1.0F, 0.5F);

                    ItemEntity seal = new ItemEntity(level, pos.getX()+0.5, pos.getY()+1.2, pos.getZ()+0.5,
                            new ItemStack(ReverieModItems.BOAR_MONK_SEAL.get()));
                    seal.setNoGravity(true);
                    seal.setDeltaMovement(0, 0, 0);
                    level.addFreshEntity(seal);

                    event.setCanceled(true);
                    player.displayClientMessage(Component.translatable("reverie.message.ritual_success"), true);
                } else {
                    player.displayClientMessage(Component.translatable("reverie.message.ritual_missing_items"), true);
                }
            } else {
                player.displayClientMessage(Component.translatable("reverie.message.ritual_table_not_ready"), true);
            }
        }
    }

    // --- 3. DOMUZ DÖNÜŞÜMÜ (SEAL İLE) ---
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof Pig pig) {
            Player player = event.getEntity();
            ItemStack itemInHand = player.getItemInHand(event.getHand());

            // MÜHÜR BASMA
            if (itemInHand.is(ReverieModItems.BOAR_MONK_SEAL.get())) {
                if (!pig.getPersistentData().contains(TAG_TRANSFORMING)) {
                    pig.getPersistentData().putInt(TAG_TRANSFORMING, RITUAL_DURATION);
                    if (!event.getLevel().isClientSide) {
                        event.getLevel().playSound(null, pig.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.NEUTRAL, 1.0f, 1.0f);
                        if (!player.getAbilities().instabuild) itemInHand.shrink(1);
                    }
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }

            // PAPER -> SCROLL
            else if (itemInHand.is(Items.PAPER)) {
                if (ReverieModAttributes.SPIRITUALITY == null) return;

                AttributeInstance attr = pig.getAttribute(ReverieModAttributes.SPIRITUALITY);
                if (attr != null && attr.getValue() > 0.5D) {
                    if (!event.getLevel().isClientSide) {
                        if (!player.getAbilities().instabuild) itemInHand.shrink(1);
                        ItemStack scroll = new ItemStack(ReverieModItems.BOAR_WHISPERER_SCROLL.get());
                        if (!player.getInventory().add(scroll)) player.drop(scroll, false);

                        event.getLevel().playSound(null, pig.blockPosition(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 1.0f, 1.0f);
                        attr.setBaseValue(0.0D);
                    }
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }
        }
    }

    // --- 4. ANIMASYON & SPAWN (TEMİZLENDİ) ---
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Pig pig) {
            CompoundTag data = pig.getPersistentData();

            // SADECE MANTIK KODU KALDI.
            // Partikül kodlarının HEPSİNİ sildik çünkü RenderLayer hallediyor.

            if (data.contains(TAG_TRANSFORMING)) {
                int timer = data.getInt(TAG_TRANSFORMING);
                if (timer > 0) {
                    pig.getNavigation().stop();
                    pig.setDeltaMovement(0, 0.03, 0);
                    pig.setNoGravity(true);

                    data.putInt(TAG_TRANSFORMING, timer - 1);
                } else {
                    if (!pig.level().isClientSide) {
                        ServerLevel level = (ServerLevel) pig.level();
                        // Bu patlama efekti kalabilir, çünkü server-side ve tek seferlik.
                        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pig.getX(), pig.getY(), pig.getZ(), 1, 0, 0, 0, 0);
                        level.playSound(null, pig.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.NEUTRAL, 1.0f, 1.0f);

                        HogEntity hog = ReverieModEntities.HOG.get().create(level);
                        if (hog != null) {
                            hog.moveTo(pig.getX(), pig.getY(), pig.getZ(), pig.getYRot(), pig.getXRot());
                            hog.yBodyRot = pig.yBodyRot;
                            hog.setTamed(true);
                            if (pig.hasCustomName()) {
                                hog.setCustomName(pig.getCustomName());
                                hog.setCustomNameVisible(pig.isCustomNameVisible());
                            }
                            pig.discard();
                            level.addFreshEntity(hog);
                        }
                    }
                    data.remove(TAG_TRANSFORMING);
                }
            }
            // SİLİNDİ: else if (pig.level().isClientSide) { ... addParticle ... }
            // O kısım tamamen kaldırıldı.
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Pig pig && !event.getLevel().isClientSide) {
            if (ReverieModAttributes.SPIRITUALITY == null) return;
            AttributeInstance spirituality = pig.getAttribute(ReverieModAttributes.SPIRITUALITY);

            if (spirituality != null && spirituality.getValue() == 0.0D) {
                if (event.getLevel().random.nextFloat() < 0.1f) {
                    spirituality.setBaseValue(1.0D);
                }
            }
        }
    }

    private static void consumeItem(Player player, net.minecraft.world.item.Item item) {
        if (player.getAbilities().instabuild) return;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.is(item)) { s.shrink(1); return; }
        }
    }
}