package net.eris.reverie.entity.custom;

import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.UUID;

public class FolkGossipManager {
    private final FolkEntity folk;
    private final GossipContainer gossips; // Vanilla Dedikodu Kutusu

    public FolkGossipManager(FolkEntity folk) {
        this.folk = folk;
        this.gossips = new GossipContainer();
    }

    // 1. Dedikodu Paylaşımı (Buluşma anında çağıracağız)
    public void shareGossipWith(FolkEntity otherFolk) {
        // Vanilla sistemi: Kendi bildiklerini karşı tarafa aktarır.
        // Major Positive (Kahramanlık) veya Major Negative (Cinayet) gibi şeyleri anlatır.
        this.gossips.transferFrom(otherFolk.getGossipManager().gossips, folk.getRandom(), 10);
    }

    // 2. Oyuncu Hakkında Dedikodu Ekle (Örn: Biri vurduğunda çağıracağız)
    public void addGossip(UUID targetId, GossipType type, int value) {
        this.gossips.add(targetId, type, value);
    }

    // 3. Fiyatları Güncelle (Ticaret açılınca çağrılır)
    public void applyPriceModifiers(MerchantOffers offers, Player player) {
        int reputation = this.gossips.getReputation(player.getUUID(), (type) -> true);

        // Reputation (İtibar) formülü:
        // Pozitifse indirim, Negatifse zam.

        for (MerchantOffer offer : offers) {
            // Vanilla mantığına benzer bir formül:
            // Her 1 puan itibar, fiyatta ufak bir oynama yapar.
            // Ama taban fiyatın altına çok düşürmemeye dikkat eder.

            double modifier = 0.0;
            if (reputation != 0) {
                // Basit matematik: İtibar yüksekse modifier negatif olur (fiyat düşer)
                modifier = -reputation * 0.05;
            }

            // Vanilla'nın specialPriceDiff alanını güncelliyoruz
            int priceChange = (int)(offer.getBaseCostA().getCount() * modifier);
            offer.addToSpecialPriceDiff(priceChange);
        }
    }

    public GossipContainer getGossips() {
        return this.gossips;
    }
}