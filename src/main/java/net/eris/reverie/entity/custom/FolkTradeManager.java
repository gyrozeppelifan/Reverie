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

    // Ticaret yapıldığında XP kazandır ve Level atlat
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

    // Mesleğe ve Levele göre listeyi doldur
    public void updateTrades(MerchantOffers offers) {
        int profession = folk.getProfessionId();
        int level = folk.getFolkLevel();
        if (level <= 0) level = 1;
        offers.clear();

        switch (profession) {
            case 1 -> { for (int i = 1; i <= level; i++) addBarkeeperTrades(offers, i); }
            case 2 -> { for (int i = 1; i <= level; i++) addGunsmithTrades(offers, i); }
            case 3 -> { for (int i = 1; i <= level; i++) addTailorTrades(offers, i); }
            case 4 -> { for (int i = 1; i <= level; i++) addStableMasterTrades(offers, i); }
            case 5 -> { for (int i = 1; i <= level; i++) addBankerTrades(offers, i); }
            case 6 -> { for (int i = 1; i <= level; i++) addBountyClerkTrades(offers, i); }
            case 7 -> { for (int i = 1; i <= level; i++) addUndertakerTrades(offers, i); }
        }
    }

    // --- 1. BARMEN (BARKEEPER) ---
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

    // --- 2. SİLAHÇI (GUNSMITH) ---
    private void addGunsmithTrades(MerchantOffers offers, int level) {
        switch (level) {
            case 1 -> offers.add(new MerchantOffer(new ItemStack(Items.COAL, 15), new ItemStack(Items.EMERALD, 1), 16, 2, 0.05F));
            case 2 -> offers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 4), new ItemStack(Items.EMERALD, 1), 12, 10, 0.05F));
            case 3 -> offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.FLINT_AND_STEEL, 1), 3, 20, 0.05F));
            case 4 -> offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.GUNPOWDER, 5), 12, 30, 0.05F));
            case 5 -> offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 15), new ItemStack(Items.CROSSBOW, 1), 3, 30, 0.05F));
        }
    }

    // --- 3. TERZİ (TAILOR) ---
    private void addTailorTrades(MerchantOffers offers, int level) {
        switch (level) {
            case 1 -> offers.add(new MerchantOffer(new ItemStack(Items.WHITE_WOOL, 16), new ItemStack(Items.EMERALD, 1), 16, 2, 0.05F));
            case 2 -> offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.SHEARS, 1), 5, 5, 0.05F));
            case 3 -> offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.LEATHER_CHESTPLATE, 1), 5, 10, 0.05F));
            case 4 -> offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 7), new ItemStack(Items.LEATHER_LEGGINGS, 1), 5, 15, 0.05F));
            case 5 -> offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 10), new ItemStack(Items.LEATHER_CHESTPLATE, 1), 3, 30, 0.05F));
        }
    }

    // --- 4. SEYİS (STABLE MASTER) ---
    private void addStableMasterTrades(MerchantOffers offers, int level) {
        switch (level) {
            case 1 -> offers.add(new MerchantOffer(new ItemStack(Items.WHEAT, 20), new ItemStack(Items.EMERALD, 1), 16, 2, 0.05F));
            case 2 -> offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.LEAD, 2), 10, 5, 0.05F));
            case 3 -> offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 6), new ItemStack(Items.SADDLE, 1), 3, 15, 0.05F));
            case 4 -> offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 8), new ItemStack(Items.GOLDEN_CARROT, 5), 5, 20, 0.05F));
            case 5 -> offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 15), new ItemStack(Items.DIAMOND_HORSE_ARMOR, 1), 1, 30, 0.05F));
        }
    }

    // --- 5. BANKACI (BANKER) ---
    private void addBankerTrades(MerchantOffers offers, int level) {
        switch (level) {
            case 1 -> offers.add(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 3), new ItemStack(Items.EMERALD, 1), 20, 2, 0.05F));
            case 2 -> offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.GOLD_INGOT, 2), 20, 5, 0.05F));
            case 3 -> offers.add(new MerchantOffer(new ItemStack(Items.DIAMOND, 1), new ItemStack(Items.EMERALD, 4), 10, 15, 0.05F));
            case 4 -> offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.DIAMOND, 1), 10, 20, 0.05F));
            case 5 -> offers.add(new MerchantOffer(new ItemStack(Items.NETHERITE_SCRAP, 1), new ItemStack(Items.EMERALD, 20), 3, 50, 0.05F));
        }
    }

    // --- 6. ÖDÜL MEMURU (BOUNTY CLERK) ---
    private void addBountyClerkTrades(MerchantOffers offers, int level) {
        switch (level) {
            case 1 -> offers.add(new MerchantOffer(new ItemStack(Items.ROTTEN_FLESH, 32), new ItemStack(Items.EMERALD, 1), 16, 2, 0.05F));
            case 2 -> offers.add(new MerchantOffer(new ItemStack(Items.BONE, 24), new ItemStack(Items.EMERALD, 1), 16, 5, 0.05F));
            case 3 -> offers.add(new MerchantOffer(new ItemStack(Items.SPIDER_EYE, 12), new ItemStack(Items.EMERALD, 1), 16, 10, 0.05F));
            case 4 -> offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.NAME_TAG, 1), 5, 20, 0.05F));
            case 5 -> offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 64), new ItemStack(Items.TOTEM_OF_UNDYING, 1), 1, 50, 0.05F));
        }
    }

    // --- 7. CENAZECİ (UNDERTAKER) - DARK EDITION ---
    private void addUndertakerTrades(MerchantOffers offers, int level) {
        switch (level) {
            // Seviye 1: Ceset Temizliği (Bizden çöp alır)
            case 1 -> {
                offers.add(new MerchantOffer(new ItemStack(Items.ROTTEN_FLESH, 24), new ItemStack(Items.EMERALD, 1), 16, 2, 0.05F));
                offers.add(new MerchantOffer(new ItemStack(Items.BONE, 20), new ItemStack(Items.EMERALD, 1), 16, 2, 0.05F));
            }
            // Seviye 2: Ruh Hazırlığı (Nether Malzemesi Satar)
            case 2 -> {
                offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 2), new ItemStack(Items.SOUL_SAND, 4), 10, 5, 0.05F));
                offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 2), new ItemStack(Items.SOUL_SOIL, 4), 10, 5, 0.05F));
            }
            // Seviye 3: Organ Ticareti
            case 3 -> {
                // Örümcek Gözü ALIR
                offers.add(new MerchantOffer(new ItemStack(Items.SPIDER_EYE, 14), new ItemStack(Items.EMERALD, 1), 16, 10, 0.05F));
                // Zehirli Patates veya Mayalı Göz SATAR
                offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.FERMENTED_SPIDER_EYE, 1), 5, 10, 0.05F));
            }
            // Seviye 4: Lanetli Ürünler
            case 4 -> {
                // Phantom Zarı ALIR
                offers.add(new MerchantOffer(new ItemStack(Items.PHANTOM_MEMBRANE, 3), new ItemStack(Items.EMERALD, 1), 12, 20, 0.05F));
                // Wither Gülü SATAR (Çok nadir)
                offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 8), new ItemStack(Items.WITHER_ROSE, 1), 3, 30, 0.05F));
            }
            // Seviye 5: Karanlık Sanatlar
            case 5 -> {
                // İskelet Kafası SATAR (Wither çağırmak için)
                offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 20), new ItemStack(Items.SKELETON_SKULL, 1), 3, 50, 0.05F));
            }
        }
    }
}