package org.zeith.comm3.alcheng.api.machines.upgrades;

import net.minecraft.item.ItemStack;

public interface IUpgradeItem
{
	UpgradablePart part(ItemStack stack);

	static UpgradablePart fromStack(ItemStack stack)
	{
		if(!stack.isEmpty() && stack.getItem() instanceof IUpgradeItem)
			return ((IUpgradeItem) stack.getItem()).part(stack);
		return null;
	}
}