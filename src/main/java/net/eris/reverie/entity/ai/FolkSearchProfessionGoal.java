package net.eris.reverie.entity.ai;

import net.eris.reverie.entity.custom.FolkEntity;
import net.eris.reverie.init.ReveriePoiTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.Optional;

public class FolkSearchProfessionGoal extends Goal {
    private final FolkEntity folk;
    private int searchCooldown = 0;

    public FolkSearchProfessionGoal(FolkEntity folk) {
        this.folk = folk;
    }

    @Override
    public boolean canUse() {
        // Sadece işsizse ve tapulu bir workstation'ı yoksa ara
        return this.folk.getProfessionId() == 0 && this.folk.getWorkstationPos() == null;
    }

    @Override
    public void tick() {
        // Her 5 saniyede bir (100 tick) çevreyi tara, performansı koru
        if (--this.searchCooldown <= 0 && this.folk.level() instanceof ServerLevel serverLevel) {
            this.searchCooldown = 100;

            // --- 1.20.1 HATASIZ POI ALMA SİSTEMİ ---
            Optional<BlockPos> poiPos = serverLevel.getPoiManager().take(
                    // 1. Parametre: POI Tipini Kontrol Et (Barkeeper mı?)
                    holder -> holder.is(ReveriePoiTypes.BARKEEPER_POI.getKey()),
                    // 2. Parametre: İki parametreli BiPredicate (Hata alan yer burasıydı!)
                    (poiType, pos) -> true,
                    this.folk.blockPosition(),
                    16 // 16 blok yarıçapında tara
            );

            poiPos.ifPresent(pos -> {
                // Barı buldu, üzerine tapuladı ve mesleğini aldı
                this.folk.setWorkstationPos(pos);
                this.folk.setProfessionId(1); // 1: BARKEEPER
            });
        }
    }
}