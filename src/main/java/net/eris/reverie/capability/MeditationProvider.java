package net.eris.reverie.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MeditationProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<PlayerMeditation> PLAYER_MEDITATION = CapabilityManager.get(new CapabilityToken<>() {});

    private PlayerMeditation meditation = null;
    private final LazyOptional<PlayerMeditation> optional = LazyOptional.of(this::createPlayerMeditation);

    private PlayerMeditation createPlayerMeditation() {
        if (this.meditation == null) {
            this.meditation = new PlayerMeditation();
        }
        return this.meditation;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_MEDITATION) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerMeditation().saveNBT(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerMeditation().loadNBT(nbt);
    }

    // ... importlar aynı ...

    // --- INNER CLASS: VERİ TUTUCU ---
    public static class PlayerMeditation {
        private boolean isMeditating;
        private int meditationTimer;
        private double originY; // YENİ: Başlangıç Yüksekliği

        public boolean isMeditating() { return isMeditating; }
        public void setMeditating(boolean meditating) { this.isMeditating = meditating; }

        public int getTimer() { return meditationTimer; }
        public void incrementTimer() { this.meditationTimer++; }
        public void resetTimer() { this.meditationTimer = 0; }

        // YENİ METOTLAR
        public double getOriginY() { return originY; }
        public void setOriginY(double y) { this.originY = y; }

        public void saveNBT(CompoundTag nbt) {
            nbt.putBoolean("IsMeditating", isMeditating);
            nbt.putDouble("OriginY", originY);
            nbt.putInt("Timer", meditationTimer); // EKLENDİ: Sayacı kaydet
        }

        public void loadNBT(CompoundTag nbt) {
            isMeditating = nbt.getBoolean("IsMeditating");
            originY = nbt.getDouble("OriginY");
            meditationTimer = nbt.getInt("Timer"); // EKLENDİ: Sayacı yükle
        }
     }
}