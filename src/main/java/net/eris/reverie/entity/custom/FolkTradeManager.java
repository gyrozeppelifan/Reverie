package net.eris.reverie.entity.custom;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public class FolkTradeManager {
    private final FolkEntity folk;

    public FolkTradeManager(FolkEntity folk) {
        this.folk = folk;
    }

    public void onTrade(MerchantOffer offer) {
        offer.increaseUses();

        if (offer.shouldRewardExp()) {
            int xpEarned = 3 + folk.getRandom().nextInt(4);
            folk.setFolkXp(folk.getFolkXp() + xpEarned);

            if (folk.getFolkLevel() < 5) {
                int nextLevelXp = getXpForLevel(folk.getFolkLevel() + 1);
                if (folk.getFolkXp() >= nextLevelXp) {
                    folk.setFolkLevel(folk.getFolkLevel() + 1);
                    this.updateTrades(folk.getOffers());
                    folk.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.0F);
                }
            }
        }
    }

    public int getXpForLevel(int level) {
        return switch (level) {
            case 2 -> 10; case 3 -> 70; case 4 -> 150; case 5 -> 250; default -> 0;
        };
    }

    public void updateTrades(MerchantOffers offers) {
        int profession = folk.getProfessionId();
        int level = folk.getFolkLevel();
        if (level <= 0) level = 1;
        offers.clear();

        if (profession == 1) { // BARKEEPER
            for (int i = 1; i <= level; i++) addBarkeeperTrades(offers, i);
        }
        else if (profession == 2) { // GUNSMITH - YENİ
            for (int i = 1; i <= level; i++) addGunsmithTrades(offers, i);
        }
    }

    private void addBarkeeperTrades(MerchantOffers offers, int level) {
        switch (level) {
            case 1 -> {
                offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.HEALING), 10, 2, 0.05F));
                offers.add(new MerchantOffer(new ItemStack(Items.WHEAT, 3), new ItemStack(Items.EMERALD, 1), 16, 2, 0.05F));
            }
            case 2 -> offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 2), PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.STRENGTH), 5, 5, 0.05F));
            case 3 -> offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 3), PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.REGENERATION), 5, 10, 0.05F));
            case 4 -> offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 4), PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.NIGHT_VISION), 5, 15, 0.05F));
            case 5 -> offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 5), PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.INVISIBILITY), 3, 30, 0.05F));
        }
    }

    // YENİ: SİLAHÇI TAKASLARI
    private void addGunsmithTrades(MerchantOffers offers, int level) {
        switch (level) {
            case 1 -> {
                offers.add(new MerchantOffer(new ItemStack(Items.COAL, 15), new ItemStack(Items.EMERALD, 1), 16, 2, 0.05F));
            }
            case 2 -> {
                offers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 4), new ItemStack(Items.EMERALD, 1), 12, 10, 0.05F));
            }
            case 3 -> {
                offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.FLINT_AND_STEEL, 1), 3, 20, 0.05F));
            }
            case 4 -> {
                offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.GUNPOWDER, 5), 12, 30, 0.05F));
            }
            case 5 -> {
                offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 15), new ItemStack(Items.CROSSBOW, 1), 3, 30, 0.05F));
            }
        }
    }
}