package net.eris.reverie.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public class GeckEntity extends FolkEntity {
    // Constructor Tip Eşleşmesi
    public GeckEntity(EntityType<GeckEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    /**
     * Mob dünyada doğduğunda (spawn) bir kez çalışır.
     * Burada varyasyon zarı atıyoruz.
     */
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty,
                                        MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData,
                                        @Nullable CompoundTag pDataTag) {

        // 0: Chameleon, 1: Lizard, 2: Snake
        this.setVariant(this.random.nextInt(3));

        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }
}