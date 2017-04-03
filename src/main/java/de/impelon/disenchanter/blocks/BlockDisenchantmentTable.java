package de.impelon.disenchanter.blocks;

import java.util.List;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.impelon.disenchanter.DisenchanterMain;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDisenchantmentTable extends BlockContainer {
	
    @SideOnly(Side.CLIENT)
    private IIcon top;
    @SideOnly(Side.CLIENT)
    private IIcon bottom;

	public BlockDisenchantmentTable() {
		super(Material.rock);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.75F, 1.0F);
		this.setLightOpacity(0);
		this.setCreativeTab(CreativeTabs.tabDecorations);
		this.setBlockName("blockDisenchantmentTable");
		this.setHardness(5.0F);
		this.setResistance(2000.0F);
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
	
	@Override
	public int damageDropped (int metadata) {
		return metadata;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World w, int x, int y, int z, Random random) {
		super.randomDisplayTick(w, x, y, z, random);

		for (int blockX = x - 2; blockX <= x + 2; ++blockX) {
			for (int blockZ = z - 2; blockZ <= z + 2; ++blockZ) {
				if (blockX > x - 2 && blockX < x + 2 && blockZ == z - 1) 
					blockZ = z + 2;

				if (random.nextInt(16) == 0) {
					for (int blockY = y; blockY <= y + 1; ++blockY) {
						if (w.getBlock(blockX, blockY, blockZ) == Blocks.bookshelf) {
							if (!w.isAirBlock((blockX - x) / 2 + x, blockY, (blockZ - z) / 2 + z))
								break;

							w.spawnParticle("enchantmenttable",
									(double) blockX + 0.25D,
									(double) blockY + 0.55D,
									(double) blockZ + 0.25D,
									(double) (x - blockX) + 0.5D,
									(double) (y - blockY) + (random.nextFloat() / 2) + 0.15D,
									(double) (z - blockZ) + 0.5D);
						}
					}
				}
			}
		}
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(int unknown, CreativeTabs tab, List subItems) {
		subItems.add(new ItemStack(this, 1, 0));
		subItems.add(new ItemStack(this, 1, 1));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return side == 0 ? this.bottom : (side == 1 ? this.top : this.blockIcon);
	}
	
	@Override
    @SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess access, int x, int y, int z) {
		if (access.getBlockMetadata(x, y, z) == 1)
			return 0x888888;
		return super.colorMultiplier(access, x, y, z);
	}
	
	@Override
    @SideOnly(Side.CLIENT)
	public int getRenderColor(int meta) {
		if (meta == 1)
			return 0x888888;
		return super.getRenderColor(meta);
	}
	
	@Override
	public boolean hasComparatorInputOverride() {
		return true;
	}
	@Override
	public int getComparatorInputOverride(World w, int x, int y, int z, int side) {
		TileEntity te = w.getTileEntity(x, y, z);
		if (te instanceof TileEntityDisenchantmentTableAutomatic) 
			return Container.calcRedstoneFromInventory((IInventory)te);
		return 0;
	}

	@Override
	public boolean onBlockActivated(World w, int x, int y, int z, EntityPlayer p,
			int metadata, float sideX, float sideY, float sideZ) {
		if (!w.isRemote)
			p.openGui(DisenchanterMain.instance, 0, w, x, y, z);
		return true;
	}

	@Override
	public void onBlockPlacedBy(World w, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		super.onBlockPlacedBy(w, x, y, z, entity, stack);

		if (stack.hasDisplayName())
			((TileEntityDisenchantmentTable) w.getTileEntity(x, y, z)).setCustomName(stack.getDisplayName());
	}
		
	@Override
	public void breakBlock(World w, int x, int y, int z, Block b, int meta) {
		TileEntity te = w.getTileEntity(x, y, z);
		if (te instanceof TileEntityDisenchantmentTableAutomatic) {
			TileEntityDisenchantmentTableAutomatic ta = (TileEntityDisenchantmentTableAutomatic) te;
			
			for (int i1 = 0; i1 < ta.getSizeInventory(); ++i1) {
                ItemStack itemstack = ta.getStackInSlot(i1);

                if (itemstack != null) {
                    float f = (float) (Math.random() * 0.8F + 0.1F);
                    float f1 = (float) (Math.random() * 0.8F + 0.1F);
                    EntityItem entityitem;

                    for (float f2 = (float) (Math.random() * 0.8F + 0.1F); itemstack.stackSize > 0; w.spawnEntityInWorld(entityitem)) {
                        int j1 = (int) (Math.random() * 21 + 10);

                        if (j1 > itemstack.stackSize)
                            j1 = itemstack.stackSize;

                        itemstack.stackSize -= j1;
                        entityitem = new EntityItem(w, (double)((float)x + f), (double)((float)y + f1), (double)((float)z + f2), new ItemStack(itemstack.getItem(), j1, itemstack.getItemDamage()));
                        float f3 = 0.05F;
                        entityitem.motionX = (double)((float)Math.random() * f3);
                        entityitem.motionY = (double)((float)Math.random() * f3 + 0.2F);
                        entityitem.motionZ = (double)((float)Math.random() * f3);

                        if (itemstack.hasTagCompound())
                            entityitem.getEntityItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
                    }
                }
            }
		}
		super.breakBlock(w, x, y, z, b, meta);
	}
		
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister i) {
		this.blockIcon = i.registerIcon("Disenchanter:disenchantment_side");
		this.top = i.registerIcon("Disenchanter:disenchantment_top");
		this.bottom = i.registerIcon("Disenchanter:disenchantment_bottom");
	}
	
	@Override
	public TileEntity createNewTileEntity(World w, int metadata) {
		if (metadata == 1)
			return new TileEntityDisenchantmentTableAutomatic();
		return new TileEntityDisenchantmentTable();
	}

}
