package org.zeith.comm3.alcheng.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.zeith.comm3.alcheng.api.machines.upgrades.IUpgradeItem;
import org.zeith.comm3.alcheng.api.machines.upgrades.UpgradablePart;

public class ItemSimpleUpgrade
		extends Item
		implements IUpgradeItem
{
	public final UpgradablePart part;

	public ItemSimpleUpgrade(UpgradablePart part)
	{
		this.part = part;
		setTranslationKey(part.itemID().getPath());
		setMaxStackSize(Math.min(64, part.getMax()));
	}

	@Override
	public UpgradablePart part(ItemStack stack)
	{
		return part;
	}
}