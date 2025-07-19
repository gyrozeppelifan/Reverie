package net.eris.reverie.block.entity;

import net.eris.reverie.block.CopperConduitBlock;
import net.eris.reverie.block.CopperJunctionBlock;
import net.eris.reverie.init.ReverieModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CopperJunctionBlockEntity extends BlockEntity {
    private int transferTimer = 0;
    private int shutdownTimer = 0;
    private Direction incomingDir = Direction.DOWN;

    public CopperJunctionBlockEntity(BlockPos pos, BlockState state) {
        super(ReverieModBlockEntities.COPPER_JUNCTION.get(), pos, state);
    }

    public void startPowerCycle(Direction incoming) {
        if (transferTimer > 0 || shutdownTimer > 0) return;
        this.incomingDir = incoming;
        this.transferTimer = 5;   // ~0.25s
        this.shutdownTimer = 20;  // ~1s

        BlockState current = getBlockState();
        if (!current.getValue(CopperJunctionBlock.POWERED)) {
            level.setBlock(worldPosition,
                    current.setValue(CopperJunctionBlock.POWERED, true),
                    3);
        }
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CopperJunctionBlockEntity be) {
        if (level.isClientSide) return;

        // Eğer idle durumdaysak (timer sıfırsa), komşuları Conduit & LightningRod gibi tarayıp enerji çek
        if (be.transferTimer == 0 && be.shutdownTimer == 0) {
            for (Direction face : Direction.values()) {
                BlockPos nPos = pos.relative(face);
                BlockState nb = level.getBlockState(nPos);

                // CopperConduit’dan enerji al
                if (nb.getBlock() instanceof CopperConduitBlock
                        && nb.getValue(CopperConduitBlock.POWERED)
                        && nb.getValue(CopperConduitBlock.FACING) == face.getOpposite()) {
                    be.startPowerCycle(face);
                    break;
                }

                // LightningRod’dan enerji al
                if (nb.getBlock() == Blocks.LIGHTNING_ROD
                        && nb.hasProperty(CopperJunctionBlock.POWERED)
                        && nb.getValue(CopperJunctionBlock.POWERED)) {
                    be.startPowerCycle(face);
                    break;
                }
            }
        }

        // Transfer timer işlemi
        if (be.transferTimer > 0) {
            be.transferTimer--;
            if (be.transferTimer == 0) {
                be.doTransfer(level, pos);
                be.setChanged();
            }
        }

        // Shutdown timer işlemi
        if (be.shutdownTimer > 0) {
            be.shutdownTimer--;
            if (be.shutdownTimer == 0 && state.getValue(CopperJunctionBlock.POWERED)) {
                level.setBlock(pos,
                        state.setValue(CopperJunctionBlock.POWERED, false),
                        3);
                be.setChanged();
            }
        }
    }

    private void doTransfer(Level level, BlockPos pos) {
        for (Direction face : Direction.values()) {
            if (face == incomingDir) continue;
            BlockPos tgt = pos.relative(face);
            BlockState nb = level.getBlockState(tgt);
            if (nb.getBlock() instanceof CopperConduitBlock) {
                Direction conduitFace = nb.getValue(CopperConduitBlock.FACING);
                if (conduitFace == face) {
                    ((CopperConduitBlock) nb.getBlock())
                            .energize(level, tgt, face.getOpposite());
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("IncomingDir", incomingDir.getName());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.transferTimer = 0;
        this.shutdownTimer = 0;
        if (tag.contains("IncomingDir")) {
            Direction d = Direction.byName(tag.getString("IncomingDir"));
            if (d != null) this.incomingDir = d;
        }
    }
}
