package net.eris.reverie.block.entity;

import net.eris.reverie.entity.HogEntity;
import net.eris.reverie.init.ReverieModBlockEntities;
import net.eris.reverie.init.ReverieModSounds; // Kendi ses dosyanın importu
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ResonantGongBlockEntity extends BlockEntity {

    public int ringTicks = 0; // Animasyon sayacı
    private int cooldown = 0; // Bekleme süresi

    public ResonantGongBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ReverieModBlockEntities.RESONANT_GONG.get(), pPos, pBlockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ResonantGongBlockEntity entity) {
        if (entity.ringTicks > 0) entity.ringTicks--;
        if (entity.cooldown > 0) entity.cooldown--;
    }

    public void ring(Player player) {
        // Cooldown varsa çalma
        if (this.cooldown > 0) return;

        this.ringTicks = 100; // 5 saniye boyunca ağır ağır sallansın
        this.cooldown = 200;  // 10 saniye boyunca tekrar çalınamasın

        Level level = this.getLevel();
        BlockPos pos = this.getBlockPos();

        if (level != null) {
            // 1. SES: Senin özel sesin (Gong Play)
            // Ses yüksekliği 2.0F (geniş alan), Pitch 1.0F (normal ton)
            // Eğer ReverieModSounds.GONG_PLAY kızarırsa, ses dosyanın adını kontrol et.
            level.playSound(null, pos, ReverieModSounds.GONG_PLAY.get(), SoundSource.BLOCKS, 2.0F, 1.0F);

            // 2. PARTİKÜL (Müzik notaları ve sonik patlama)
            if (level.isClientSide) {
                // Ortayı bul (2x2 bloğun merkezi)
                double cx = pos.getX() + 1.0;
                double cy = pos.getY() - 1.0;
                double cz = pos.getZ() + 1.0;

                // Patlama efekti
                level.addParticle(ParticleTypes.SONIC_BOOM, cx, cy, cz, 0, 0, 0);

                // Notalar
                for (int i = 0; i < 15; i++) {
                    level.addParticle(ParticleTypes.NOTE,
                            cx + (level.random.nextDouble() - 0.5),
                            cy + (level.random.nextDouble() - 0.5),
                            cz + (level.random.nextDouble() - 0.5),
                            level.random.nextDouble(), 0, 0);
                }
            }
            // 3. EFEKTLER (SERVER TARAFI)
            else {
                AABB area = new AABB(pos).inflate(32); // 32 blok yarıçap (Bayağı geniş)

                // A) Hoglara Buff
                List<HogEntity> hogs = level.getEntitiesOfClass(HogEntity.class, area);
                for (HogEntity hog : hogs) {
                    applyMonkBlessing(hog);
                }

                // B) Oyunculara Buff
                List<Player> players = level.getEntitiesOfClass(Player.class, area);
                for (Player p : players) {
                    applyMonkBlessing(p);
                }
            }
        }
    }

    // Ortak Efekt Metodu (Hız + Zıplama + Direnç)
    private void applyMonkBlessing(net.minecraft.world.entity.LivingEntity entity) {
        int duration = 1200; // 60 Saniye (1 Dakika)

        // Speed II (Amplifier 1)
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 1));

        // Jump Boost II (Amplifier 1)
        entity.addEffect(new MobEffectInstance(MobEffects.JUMP, duration, 1));

        // Resistance I (Amplifier 0)
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 0));
    }
}