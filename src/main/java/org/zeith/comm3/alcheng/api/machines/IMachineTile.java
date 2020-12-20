package org.zeith.comm3.alcheng.api.machines;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.zeith.comm3.alcheng.api.machines.upgrades.IMachineUpgrades;
import org.zeith.comm3.alcheng.api.machines.upgrades.IUpgradeItem;
import org.zeith.comm3.alcheng.api.machines.upgrades.UpgradablePart;

public interface IMachineTile
{
	void handleUpgrades();

	IMachineUpgrades upgrades();

	IInventory upgradeInventory();

	default int countUpgrades(UpgradablePart part)
	{
		IInventory inv = upgradeInventory();
		int c = 0;
		for(int i = 0; i < inv.getSizeInventory(); ++i)
		{
			ItemStack stack = inv.getStackInSlot(i);
			UpgradablePart cPart = IUpgradeItem.fromStack(stack);
			if(cPart == part) c += stack.getCount();
		}
		return c;
	}
}