package net.eris.reverie.block.entity;

import net.eris.reverie.block.CopperConduitBlock;
import net.eris.reverie.init.ReverieModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CopperConduitBlockEntity extends BlockEntity {
    private int transferTimer = 0;
    private int shutdownTimer = 0;

    public CopperConduitBlockEntity(BlockPos pos, BlockState state) {
        super(ReverieModBlockEntities.COPPER_CONDUIT.get(), pos, state);
    }

    /** Başlatıldığında iki sayaç ayarlanır ve powered=true yapılır */
    public void startPowerCycle() {
        this.transferTimer = 5;
        this.shutdownTimer = 20;
        if (!this.getBlockState().getValue(CopperConduitBlock.POWERED)) {
            level.setBlock(worldPosition,
                    getBlockState().setValue(CopperConduitBlock.POWERED, true),
                    3
            );
        }
        setChanged();
    }

    /** Her tick çağrılan metod */
    public static void tick(Level level, BlockPos pos, BlockState state, CopperConduitBlockEntity be) {
        if (level.isClientSide) return;

        // 1) Transfer zamanı
        if (be.transferTimer > 0) {
            be.transferTimer--;
            if (be.transferTimer == 0) {
                ((CopperConduitBlock) state.getBlock())
                        .transferEnergy(level, pos, state.getValue(CopperConduitBlock.FACING));
                be.setChanged();
            }
        }

        // 2) Kapatma zamanı
        if (be.shutdownTimer > 0) {
            be.shutdownTimer--;
            if (be.shutdownTimer == 0) {
                if (state.getValue(CopperConduitBlock.POWERED)) {
                    level.setBlock(pos,
                            state.setValue(CopperConduitBlock.POWERED, false),
                            3
                    );
                }
                be.setChanged();
            }
        }
    }

    /** World kaydedilirken sayaçları NBT’ye yaz */
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("TransferTimer", this.transferTimer);
        tag.putInt("ShutdownTimer", this.shutdownTimer);
    }

    /** World yüklendiğinde NBT’den sayaçları geri yükle */
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.transferTimer  = tag.getInt("TransferTimer");
        this.shutdownTimer  = tag.getInt("ShutdownTimer");
    }
}
