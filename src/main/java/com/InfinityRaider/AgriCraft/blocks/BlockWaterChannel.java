package com.InfinityRaider.AgriCraft.blocks;

import com.InfinityRaider.AgriCraft.reference.Constants;
import com.InfinityRaider.AgriCraft.reference.Names;
import com.InfinityRaider.AgriCraft.renderers.blocks.RenderBlockBase;
import com.InfinityRaider.AgriCraft.renderers.blocks.RenderChannel;
import com.InfinityRaider.AgriCraft.tileentity.irrigation.TileEntityChannel;
import com.InfinityRaider.AgriCraft.utility.ForgeDirection;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class BlockWaterChannel extends BlockCustomWood {
    protected static final float MIN = Constants.UNIT * Constants.QUARTER;
    protected static final float MAX = Constants.UNIT * Constants.THREE_QUARTER;
    
    public BlockWaterChannel() {
        super();
        this.setBlockBounds(MIN, MIN, MIN, MAX, MAX, MAX);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityChannel();
    }

    @Override
    protected String getTileEntityName() {
        return Names.Objects.channel;
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block block) {
        TileEntity te = world.getTileEntity(pos);
        if (te != null && te instanceof TileEntityChannel) {
            ((TileEntityChannel) te).findNeighbours();
        }
    }

    @Override
    public boolean onBlockEventReceived(World world, BlockPos pos, IBlockState state, int id, int data) {
        return world.getTileEntity(pos)!=null && world.getTileEntity(pos).receiveClientEvent(id, data);
    }

    @Override
    public boolean isMultiBlock() {
        return false;
    }

    /**
     * Adds all intersecting collision boxes to a list. (Be sure to only add boxes to the list if they intersect the
     * mask.) Parameters: World, pos, mask, list, colliding entity
     */
    @SideOnly(Side.CLIENT)
    @Override
    public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB>  list, Entity entity) {
        //adjacent boxes
        TileEntity te = world.getTileEntity(pos);
        if (te != null && te instanceof TileEntityChannel) {
            TileEntityChannel channel = (TileEntityChannel) te;
            if (channel.hasNeighbourCheck(ForgeDirection.EAST)) {
                this.setBlockBounds(MAX - Constants.UNIT, MIN, MIN, Constants.UNIT * Constants.WHOLE, MAX, MAX);
                super.addCollisionBoxesToList(world, pos, state, mask, list, entity);
            }
            if (channel.hasNeighbourCheck(ForgeDirection.WEST)) {
                this.setBlockBounds(0, MIN, MIN, MIN + Constants.UNIT, MAX, MAX);
                super.addCollisionBoxesToList(world, pos, state, mask, list, entity);
            }
            if (channel.hasNeighbourCheck(ForgeDirection.SOUTH)) {
                this.setBlockBounds(MIN, MIN, MAX - Constants.UNIT, MAX, MAX, Constants.UNIT * Constants.WHOLE);
                super.addCollisionBoxesToList(world, pos, state, mask, list, entity);
            }
            if (channel.hasNeighbourCheck(ForgeDirection.NORTH)) {
                this.setBlockBounds(MIN, MIN, 0, MAX, MAX, MIN + Constants.UNIT);
                super.addCollisionBoxesToList(world, pos, state, mask, list, entity);
            }
            //central box
            this.setBlockBounds(MIN, MIN, MIN, MAX, MAX, MAX);
            super.addCollisionBoxesToList(world, pos, state, mask, list, entity);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getSelectedBoundingBox(World world, BlockPos pos) {
        TileEntityChannel channel = (TileEntityChannel) world.getTileEntity(pos);
        AxisAlignedBB minBB = new AxisAlignedBB(MIN, MIN, MIN, MAX, MAX, MAX);
        if (channel.hasNeighbourCheck(ForgeDirection.EAST)) {
            minBB.addCoord(1, MAX, minBB.maxZ);
        }
        if (channel.hasNeighbourCheck(ForgeDirection.WEST)) {
            minBB.addCoord(0, MIN, minBB.minZ);
        }
        if (channel.hasNeighbourCheck(ForgeDirection.SOUTH)) {
            minBB.addCoord(minBB.maxX, MAX, 1);
        }
        if (channel.hasNeighbourCheck(ForgeDirection.NORTH)) {
            minBB.addCoord(minBB.minX, MIN, 0);
        }
        return minBB.offset(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
        list.add(new ItemStack(item, 1, 0));    //wooden channel
        list.add(new ItemStack(item, 1, 1));    //iron pipe
    }

    //render methods
    //--------------
    @Override
    public boolean isOpaqueCube() {return false;}

    @Override
    protected IProperty[] getPropertyArray() {
        return new IProperty[0];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public RenderBlockBase getRenderer() {
        return new RenderChannel();
    }

    @Override
    protected String getInternalName() {
        return Names.Objects.channel;
    }
}
