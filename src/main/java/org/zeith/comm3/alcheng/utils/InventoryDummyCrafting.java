package org.zeith.comm3.alcheng.utils;

import com.zeitheron.hammercore.utils.inventory.InventoryDummy;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class InventoryDummyCrafting
		extends InventoryDummy
{
	private final int inventoryWidth;
	private final int inventoryHeight;

	public InventoryDummyCrafting(int inventorySize, NBTTagCompound boundNBT)
	{
		super(inventorySize, boundNBT);
		inventoryWidth = inventorySize;
		inventoryHeight = 1;
	}

	public InventoryDummyCrafting(NBTTagCompound boundNBT, ItemStack... items)
	{
		super(boundNBT, items);
		inventoryWidth = items.length;
		inventoryHeight = 1;
	}

	public InventoryDummyCrafting(int inventorySize)
	{
		super(inventorySize);
		inventoryWidth = inventorySize;
		inventoryHeight = 1;
	}

	public InventoryDummyCrafting(ItemStack... items)
	{
		super(items);
		inventoryWidth = items.length;
		inventoryHeight = 1;
	}

	public InventoryDummyCrafting(int width, int height)
	{
		super(width * height);
		this.inventoryWidth = width;
		this.inventoryHeight = height;
	}

	public ItemStack getStackInRowAndColumn(int row, int column)
	{
		return row >= 0 && row < this.inventoryWidth && column >= 0 && column <= this.inventoryHeight ? this.getStackInSlot(row + column * this.inventoryWidth) : ItemStack.EMPTY;
	}

	public int getHeight()
	{
		return this.inventoryHeight;
	}

	public int getWidth()
	{
		return this.inventoryWidth;
	}
}