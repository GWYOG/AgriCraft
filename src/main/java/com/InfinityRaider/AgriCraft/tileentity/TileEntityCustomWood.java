package com.InfinityRaider.AgriCraft.tileentity;

import com.InfinityRaider.AgriCraft.api.v1.IDebuggable;
import com.InfinityRaider.AgriCraft.blocks.BlockCustomWood;
import com.InfinityRaider.AgriCraft.reference.Names;
import com.InfinityRaider.AgriCraft.renderers.TextureCache;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * This class represents the root tile entity for all AgriCraft custom wood blocks.
 * Through this class, the custom woods are remembered for the blocks. *
 */
public class TileEntityCustomWood extends TileEntityBase implements IDebuggable {
	
	/** The default material to use. Currently is wood planks. */
    private static final Block DEFAULT_MATERIAL = Blocks.planks;
    
    /** The default metadata to use. Currently is set to Oak(0) for Planks. */
    private static final int DEFAULT_META = 0;
	
	/**
     * A pointer to the the block the CustomWoodBlock is imitating.
     * 
     * Defaults to {@link #DEFAULT_MATERIAL}.
     */
    private Block material = Blocks.planks;
    
    /**
     * The metadata of the block the CustomWoodBlock is imitating.
     * 
     * Defaults to {@link #DEFAULT_META}.
     */
    private int materialMeta = 0;

    /** Cached icon */
    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite icon;

    @Override
    public void writeToNBT(NBTTagCompound tag) {
    	tag.setString(Names.NBT.material, Block.blockRegistry.getNameForObject(this.getMaterial()).toString());
        tag.setInteger(Names.NBT.materialMeta, this.getMaterialMeta());
        super.writeToNBT(tag);
    }

    /**
     * Loads the CustomWood entity from a NBTTag, as to load from a savefile.
     * 
     * @param tag the tag to load the entity data from.
     */
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.setMaterial(tag);
    }

    /**
     * Tests to see if another CustomWood entity is of the same material.
     * 
     * @param tileEntity the CustomWood entity to test.
     * @return if the construction materials for both entities are the same.
     */
    public final boolean isSameMaterial(TileEntityCustomWood tileEntity) {
        return tileEntity!=null && this.getBlockMetadata()==tileEntity.getBlockMetadata() && this.getMaterial()==tileEntity.getMaterial() && this.getMaterialMeta()==tileEntity.getMaterialMeta();
    }

    
    /**
     * Sets the CustomWood block's material, the material to mimic, from an NBTTag.
     * This function is intended for use internally, for serialization.
     * 
     * @param tag the tag to set the block's material from.
     */
    private void setMaterial(NBTTagCompound tag) {
        if(tag!=null && tag.hasKey(Names.NBT.material) && tag.hasKey(Names.NBT.materialMeta)) {
            this.setMaterial(tag.getString(Names.NBT.material), tag.getInteger(Names.NBT.materialMeta));
        }
    }

    /**
     * Sets the CustomWood block's material, the material to mimic, from an ItemStack.
     * 
     * @param stack the ItemStack to set the block's material from.
     */
    public final void setMaterial(ItemStack stack) {
        if(stack!=null && stack.getItem()!=null && stack.getItem() instanceof ItemBlock) {
        	this.setMaterial(stack.getTagCompound());
        }
    }

    /**
     * Sets the CustomWood block's material, the material to mimic, from the name of the material (block) and its metadata value.
     * 
     * @param name the name of the material (block).
     * @param meta the metadata value of the material (block).
     */
    public final void setMaterial(String name, int meta) {
        Block block = Block.getBlockFromName(name);
        this.setMaterial(block==Blocks.air?DEFAULT_MATERIAL:block, block==Blocks.air?DEFAULT_META:meta);
    }
    
    /**
     * Sets the CustomWood block's material, the material to mimic, from the name of the material (block) and its metadata value.
     * 
     * @param block the name of the material (block).
     * @param meta the metadata value of the material (block).
     */
    public final void setMaterial(Block block, int meta) {
        if(block!=null) {
            this.material = block;
            this.materialMeta = meta;
        }
    }

    /**
     * Retrieves the material the CustomWood is mimicking.
     * 
     * @return the material, in Block form.
     */
    public final Block getMaterial() {
        return this.material==null?DEFAULT_MATERIAL:this.material;
    }

    public final IBlockState getMaterialState() {
        Block mat = this.getMaterial();
        return mat.getStateFromMeta(getMaterialMeta());
    }

    /**
     * Retrieves the metadata of the material the CustomWood is mimicking.
     * 
     * @return the metadata of the material.
     */
    public final int getMaterialMeta() {
        return this.material==null?DEFAULT_META:this.materialMeta;
    }

    public final ItemStack getMaterialStack() {
        return new ItemStack(this.getMaterial(), 1, this.getMaterialMeta());
    }

    /**
     * Retrieves the name of the material the CustomWood is mimicking.
     * 
     * @return the name of the material.
     */
    public final String getMaterialName() {
        return Block.blockRegistry.getNameForObject(this.getMaterial()).toString();
    }
    
    /**
     * Generates an NBTTag for the material the CustomWood is mimicking.
     * 
     * @return an NBTTag for the CustomWood material.
     */
    public final NBTTagCompound getMaterialTag() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString(Names.NBT.material, this.getMaterialName());
        tag.setInteger(Names.NBT.materialMeta, this.materialMeta);
        return tag;
    }

    public final TextureAtlasSprite getIcon() {
        if(this.icon == null) {
            this.cacheIcon();
        }
        return this.icon == null ? getDefaultIcon() : this.icon;
    }

    private void cacheIcon() {
        if (this.material == null) {
            return;
        }
        List<TextureAtlasSprite> icons = TextureCache.getInstance().retrieveIcons(getMaterialState());
        if (icons.size() > 0) {
            TextureAtlasSprite fromCache = icons.get(0);
            if(fromCache != Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite()) {
                this.icon = fromCache;
            }
        }

    }

    @Override
    public BlockCustomWood getBlockType() {
        return (BlockCustomWood) super.getBlockType();
    }

    public TextureAtlasSprite getDefaultIcon() {
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/oak_planks");
    }

    @Override
    public void addDebugInfo(List<String> list) {
        list.add("this material is: " + this.getMaterialName() + ":" + this.getMaterialMeta());
    }

    @Override
    public boolean isRotatable() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public int colorMultiplier() {
        if(this.worldObj==null) {
            return 16777215;
        } else {
            return getBlockType().colorMultiplier(worldObj, this.getPos());
        }
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addWailaInformation(List information) {
    	ItemStack mat = this.getMaterialStack();
    	information.add(StatCollector.translateToLocal("agricraft_tooltip.material")+": "+ mat.getItem().getItemStackDisplayName(mat));
    }
}
