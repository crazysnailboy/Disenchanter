package de.impelon.disenchanter.blocks;

import java.util.Random;

import de.impelon.disenchanter.DisenchanterMain;
import de.impelon.disenchanter.proxies.CommonProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ContainerDisenchantment extends Container {
	
	private World worldObj;
	private int posX;
	private int posY;
	private int posZ;
	private TileEntityDisenchantmentTableAutomatic tileentity;
	private Random random = new Random();
	private IInventory slots = new InventoryBasic("Disenchant", true, 3) {

		@Override
		public int getInventoryStackLimit() {
			return 1;
		}
				
		@Override
		public void markDirty() {
			super.markDirty();
			ContainerDisenchantment.this.onCraftMatrixChanged(this);
		}
	};

	public ContainerDisenchantment(IInventory inventory, World w, int x, int y, int z) {
		this.worldObj = w;
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.tileentity = null;
		
		TileEntity te = this.worldObj.getTileEntity(this.posX, this.posY, this.posZ);
		if (te instanceof TileEntityDisenchantmentTableAutomatic) {
			this.tileentity = (TileEntityDisenchantmentTableAutomatic) te;
			this.slots = this.tileentity;
		}

		this.addSlotToContainer(new Slot(this.slots, 0, 26, 35));

		this.addSlotToContainer(new Slot(this.slots, 1, 75, 35) {

			@Override
			public boolean isItemValid(ItemStack stack) {
				return stack.getItem().equals(Items.book);
			}

		});

		this.addSlotToContainer(new Slot(this.slots, 2, 133, 35) {

			@Override
			public boolean isItemValid(ItemStack stack) {
				return false;
			}
			
			@Override
			public void onPickupFromSlot(EntityPlayer p, ItemStack stack) {
				if (tileentity != null)
					return;
				ItemStack itemstack = slots.getStackInSlot(0);
				ItemStack bookstack = slots.getStackInSlot(1);
				ItemStack outputBookstack = new ItemStack(Items.enchanted_book);
				BlockDisenchantmentTable table = DisenchanterMain.proxy.disenchantmentTable;

				if (itemstack != null && bookstack != null && itemstack.stackTagCompound != null) {
					if (bookstack.stackSize > 1)
						bookstack.stackSize -= 1;
					else
						bookstack = (ItemStack) null;
					slots.setInventorySlotContents(1, bookstack);

					float power = table.getEnchantingPower(worldObj, posX, posY, posZ);
					int flatDmg = DisenchanterMain.config.get("disenchanting", "FlatDamage", 10).getInt();
					double durabiltyDmg = DisenchanterMain.config.get("disenchanting", "MaxDurabilityDamage", 0.025).getDouble();
					double reduceableDmg = DisenchanterMain.config.get("disenchanting", "MaxDurabilityDamageReduceable", 0.2).getDouble();
					double machineDmgMultiplier = DisenchanterMain.config.get("disenchanting", "MachineDamageMultiplier", 2.5).getDouble();
						
					while (table.getEnchantmentList(itemstack) != null) {
						table.transferEnchantment(itemstack, outputBookstack, 0, random);
						
						itemstack.attemptDamageItem((int) (machineDmgMultiplier * (flatDmg + itemstack.getMaxDamage() * durabiltyDmg + 
								itemstack.getMaxDamage() * (reduceableDmg / power))), random);
						if (itemstack.getItemDamage() > itemstack.getMaxDamage()) {
							slots.setInventorySlotContents(0, (ItemStack) null);
							break;
						}
						
						if (!(table.isBulkDisenchanting(worldObj.getBlockMetadata(posX, posY, posZ))))
							break;
					}
					
					if (table.getEnchantmentList(itemstack) == null) {
						if (itemstack.getItem() == Items.enchanted_book)
							slots.setInventorySlotContents(0, new ItemStack(Items.book));
						if (table.isVoiding(worldObj.getBlockMetadata(posX, posY, posZ)))
							slots.setInventorySlotContents(0, (ItemStack) null);
					}
				}
			}

		});

		int l;

		for (l = 0; l < 3; ++l)
			for (int i1 = 0; i1 < 9; ++i1)
				this.addSlotToContainer(new Slot(inventory, i1 + l * 9 + 9, 8 + i1 * 18, 84 + l * 18));

		for (l = 0; l < 9; ++l)
			this.addSlotToContainer(new Slot(inventory, l, 8 + l * 18, 142));

	}

	@Override
	public void onCraftMatrixChanged(IInventory inventory) {
		super.onCraftMatrixChanged(inventory);

		if (inventory == this.slots && this.tileentity == null)
			this.updateOutput();
	}
	
	public void updateOutput() {
		if (!this.worldObj.isRemote) {
			ItemStack itemstack = this.slots.getStackInSlot(0);
			ItemStack bookstack = this.slots.getStackInSlot(1);
			ItemStack outputBookstack = new ItemStack(Items.enchanted_book);
			BlockDisenchantmentTable table = DisenchanterMain.proxy.disenchantmentTable;

			if (itemstack != null && bookstack != null && itemstack.stackTagCompound != null) {
				itemstack = itemstack.copy();
				if (itemstack.stackTagCompound.getTag("InfiTool") != null)
					if (DisenchanterMain.config.get("disenchanting", "EnableTCBehaviour", true).getBoolean())
						return;

				if (table.getEnchantmentList(itemstack) == null) {
					if (this.slots.getStackInSlot(2) != null)
						this.slots.setInventorySlotContents(2, (ItemStack) null);
					return;
				}
				
				float power = table.getEnchantingPower(this.worldObj, this.posX, this.posY, this.posZ);
				int flatDmg = DisenchanterMain.config.get("disenchanting", "FlatDamage", 10).getInt();
				double durabiltyDmg = DisenchanterMain.config.get("disenchanting", "MaxDurabilityDamage", 0.025).getDouble();
				double reduceableDmg = DisenchanterMain.config.get("disenchanting", "MaxDurabilityDamageReduceable", 0.2).getDouble();
				double machineDmgMultiplier = DisenchanterMain.config.get("disenchanting", "MachineDamageMultiplier", 2.5).getDouble();

				while (table.getEnchantmentList(itemstack) != null) {
					table.transferEnchantment(itemstack, outputBookstack, 0, this.random);
					
					itemstack.attemptDamageItem((int) (machineDmgMultiplier * (flatDmg + itemstack.getMaxDamage() * durabiltyDmg + 
							itemstack.getMaxDamage() * (reduceableDmg / power))), this.random);
					
					if (itemstack.getItemDamage() > itemstack.getMaxDamage() || 
							!(table.isBulkDisenchanting(this.worldObj.getBlockMetadata(this.posX, this.posY, this.posZ))))
						break;
				}
				
				if (!(this.slots.getStackInSlot(2) != null && 
						this.slots.getStackInSlot(2).getItem() == outputBookstack.getItem() && 
						this.slots.getStackInSlot(2).stackTagCompound.getTag("StoredEnchantments").equals(outputBookstack.stackTagCompound.getTag("StoredEnchantments"))))
					this.slots.setInventorySlotContents(2, (ItemStack) outputBookstack);
			} else
				if (this.slots.getStackInSlot(2) != null)
					this.slots.setInventorySlotContents(2, (ItemStack) null);
		}
	}

	@Override
	public void onContainerClosed(EntityPlayer p) {
		super.onContainerClosed(p);
		
		if (this.tileentity == null) {
			if (!this.worldObj.isRemote) {
				ItemStack itemstack = this.slots.getStackInSlotOnClosing(0);
				ItemStack bookstack = this.slots.getStackInSlotOnClosing(1);
	
				if (itemstack != null)
					p.dropPlayerItemWithRandomChoice(itemstack, false);
				if (bookstack != null)
					p.dropPlayerItemWithRandomChoice(bookstack, false);
			}
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer p) {
		return this.worldObj.getBlock(this.posX, this.posY, this.posZ) != CommonProxy.disenchantmentTable ? false
				: p.getDistanceSq((double) this.posX + 0.5D, (double) this.posY + 0.5D, (double) this.posZ + 0.5D) <= 64.0D;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer p, int slotID) {
		if (p.worldObj.isRemote)
			return null;
		ItemStack itemstackPrev = null;
		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(slotID);

		if (slot != null && slot.getHasStack()) {
			itemstack = slot.getStack();
			itemstackPrev = itemstack.copy();

			if (slotID == 2) {
				if (!this.mergeItemStack(itemstack, 3, 39, true))
					return null;
				slot.onSlotChange(itemstack, itemstackPrev);
			} else if (slotID != 0 && slotID != 1) {
				ItemStack i = itemstack.splitStack(1);
				if (i.getItem().equals(Items.book)) {
					if (((Slot) this.inventorySlots.get(1)).getHasStack() || !this.mergeItemStack(i, 1, 2, false)) {
						itemstack.stackSize++;
						return null;
					}
				} else {
					if (((Slot) this.inventorySlots.get(0)).getHasStack() || !this.mergeItemStack(i, 0, 1, false)) {
						itemstack.stackSize++;
						return null;
					}
				}
			} else if (!this.mergeItemStack(itemstack, 3, 39, true)) {
				return null;
			}
			
			if (itemstack.stackSize <= 0)
				slot.putStack((ItemStack) null);
			else
				slot.onSlotChanged();

			if (itemstack.stackSize == itemstackPrev.stackSize)
				return null;

			slot.onPickupFromSlot(p, itemstack);
		}

		return itemstack;
	}
}