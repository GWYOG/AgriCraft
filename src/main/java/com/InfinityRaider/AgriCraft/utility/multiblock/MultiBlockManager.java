package com.InfinityRaider.AgriCraft.utility.multiblock;

import com.InfinityRaider.AgriCraft.utility.CoordinateIterator;
import com.InfinityRaider.AgriCraft.utility.ForgeDirection;
import com.InfinityRaider.AgriCraft.utility.LogHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class MultiBlockManager implements IMultiBlockManager<MultiBlockPartData> {
    private static MultiBlockManager INSTANCE;

    private MultiBlockManager() {
    }

    public static MultiBlockManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MultiBlockManager();
        }
        return INSTANCE;
    }

    @Override
    public void onBlockPlaced(World world, BlockPos pos, IMultiBlockComponent component) {
        boolean flag = false;
        for (ForgeDirection dir : ForgeDirection.values()) {
            if (dir == ForgeDirection.UNKNOWN) {
                continue;
            }
            TileEntity te = world.getTileEntity(dir.offset(pos));
            if (te != null && te instanceof IMultiBlockComponent && component.isValidComponent((IMultiBlockComponent) te)) {
                IMultiBlockComponent componentAt = (IMultiBlockComponent) te;
                if (canCheckForMultiBlock(componentAt)) {
                    if (checkForMultiBlock(world, dir.offset(pos), componentAt)) {
                        return;
                    }
                    flag = true;
                }
            }
        }
        if (!flag) {
            checkForMultiBlock(world, pos, component);
        }
    }

    @Override
    public void onBlockBroken(World world, BlockPos pos, IMultiBlockComponent<? extends IMultiBlockManager<MultiBlockPartData>, MultiBlockPartData> component) {
        component.getMainComponent().preMultiBlockBreak();
        IMultiBlockPartData data = component.getMultiBlockData();
        breakAllMultiBlocksInRange(world, pos.getX() - data.posX(), pos.getY() - data.posY(), pos.getZ() - data.posZ(), pos.getX() + data.sizeX(), pos.getY() + data.sizeY(), pos.getZ() + data.sizeZ());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void createMultiBlock(World world, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
        int sizeX = xMax-xMin;
        int sizeY = yMax-yMin;
        int sizeZ = zMax-zMin;
        BlockPos pos;
        for (int x = xMin; x < xMax; x++) {
            for (int y = yMin; y < yMax; y++) {
                for (int z = zMin; z < zMax; z++) {
                    pos = new BlockPos(x, y, z);
                    IMultiBlockComponent component = (IMultiBlockComponent) world.getTileEntity(pos);
                    if(x == xMin && y == yMin && z == zMin) {
                        component.preMultiBlockCreation(xMax-xMin, yMax-yMin, zMax-zMin);
                    }
                    component.setMultiBlockPartData(new MultiBlockPartData(x-xMin, y-yMin, z-zMin, sizeX, sizeY, sizeZ));
                }
            }
        }
        if (world.isRemote) {
            world.markBlockRangeForRenderUpdate(xMin, yMin, zMin, xMax, yMax, zMax);
        }
        ((IMultiBlockComponent) world.getTileEntity(new BlockPos(xMin, yMin, zMin))).postMultiBlockCreation();
    }

    private boolean canCheckForMultiBlock(IMultiBlockComponent component) {
        return component.getMultiBlockData().size() > 1;
    }

    private boolean checkForMultiBlock(World world, BlockPos pos, IMultiBlockComponent component) {
        IMultiBlockComponent rootComponent = component.getMainComponent();
        if (rootComponent != component) {
            IMultiBlockPartData data = component.getMultiBlockData();
            return checkForMultiBlock(world, pos.add(-data.posX(), -data.posY(), -data.posZ()), rootComponent);
        }
        CoordinateIterator iterator = new CoordinateIterator();
        int xOffsetMin = calculateDimensionOffsetBackwards(world, pos, component, iterator.setX());
        int yOffsetMin = calculateDimensionOffsetBackwards(world, pos, component, iterator.setY());
        int zOffsetMin = calculateDimensionOffsetBackwards(world, pos, component, iterator.setZ());
        int xOffsetPlus = calculateDimensionOffsetForwards(world, pos, component, iterator.setX());
        int yOffsetPlus = calculateDimensionOffsetForwards(world, pos, component, iterator.setY());
        int zOffsetPlus = calculateDimensionOffsetForwards(world, pos, component, iterator.setZ());
        //if not all blocks for new root are correct, do nothing
        if (!areAllBlocksInRangeValidComponents(world, pos.getX() - xOffsetMin, pos.getY() - yOffsetMin, pos.getZ() - zOffsetMin, pos.getX() + xOffsetPlus, pos.getY() + yOffsetPlus, pos.getZ() + zOffsetPlus, component)) {
            return false;
        }
        IMultiBlockComponent newRoot = (IMultiBlockComponent) world.getTileEntity(pos.add(-xOffsetMin, -yOffsetMin, -zOffsetMin));
        int xSizeNew = xOffsetPlus + xOffsetMin;
        int ySizeNew = yOffsetPlus + yOffsetMin;
        int zSizeNew = zOffsetPlus + zOffsetMin;
        IMultiBlockPartData data = component.getMultiBlockData();
        //if dimensions and root are the same the multiblock hasn't changed and nothing has to happen
        if (component == newRoot && xSizeNew == data.sizeX() && ySizeNew == data.sizeY() && zSizeNew == data.sizeZ()) {
            return false;
        }
        //new multiblock dimensions are required, update the multiblock
        createMultiBlock(world, pos.getX() - xOffsetMin, pos.getY() - yOffsetMin, pos.getZ() - zOffsetMin, pos.getX() + xOffsetPlus, pos.getY() + yOffsetPlus, pos.getZ() + zOffsetPlus);
        return true;
    }

    private int calculateDimensionOffsetBackwards(World world, BlockPos pos, IMultiBlockComponent component, CoordinateIterator it) {
        if (!it.isActive()) {
            LogHelper.debug("ERROR WHEN ITERATING COORDINATES: ITERATOR NOT ACTIVE");
            return 0;
        }
        IMultiBlockPartData data = component.getMultiBlockData();
        int x = pos.getX() - data.posX();
        int y = pos.getY() - data.posY();
        int z = pos.getZ() - data.posZ();
        while (true) {
            it.increment();
            if (!isValidComponent(world, x - it.x(), y - it.y(), z - it.z(), component)) {
                break;
            }
        }
        return it.getOffset() - 1;
    }

    private int calculateDimensionOffsetForwards(World world, BlockPos pos, IMultiBlockComponent component, CoordinateIterator it) {
        if (!it.isActive()) {
            LogHelper.debug("ERROR WHEN ITERATING COORDINATES: ITERATOR NOT ACTIVE");
            return 0;
        }
        IMultiBlockPartData data = component.getMultiBlockData();
        int x = pos.getX() - data.posX();
        int y = pos.getY() - data.posY();
        int z = pos.getZ() - data.posZ();
        while (true) {
            it.increment();
            if (!isValidComponent(world, x + it.x(), y + it.y(), z + it.z(), component)) {
                break;
            }
        }
        return it.getOffset();
    }

    private boolean areAllBlocksInRangeValidComponents(World world, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, IMultiBlockComponent component) {
        for (int x = xMin; x < xMax; x++) {
            for (int y = yMin; y < yMax; y++) {
                for (int z = zMin; z < zMax; z++) {
                    if (!isValidComponent(world, x, y, z, component)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean isValidComponent(World world, int x, int y, int z, IMultiBlockComponent component) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        return (te != null) && (te instanceof IMultiBlockComponent) && (component.isValidComponent((IMultiBlockComponent) te));
    }

    @SuppressWarnings("unchecked")
    private void breakAllMultiBlocksInRange(World world, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
        BlockPos pos;
        for (int x=xMin; x<xMax;x++) {
            for (int y=yMin;y<yMax;y++) {
                for (int z=zMin;z<zMax;z++) {
                    pos = new BlockPos(x, y, z);
                    TileEntity te = world.getTileEntity(pos);
                    if((te == null) || !(te instanceof IMultiBlockComponent)) {
                        continue;
                    }
                    IMultiBlockComponent component = (IMultiBlockComponent) te;
                    component.setMultiBlockPartData(new MultiBlockPartData(0, 0, 0, 1, 1, 1));
                    component.postMultiBlockBreak();
                }
            }
        }
    }
}
