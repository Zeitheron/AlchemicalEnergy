package org.zeith.comm3.alcheng.api.machines.upgrades;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;

import java.util.function.Predicate;

public class MachineUpgradeManager
		implements IMachineUpgrades
{
	public final Object2IntArrayMap<UpgradablePart> map = new Object2IntArrayMap<>();

	public final Predicate<UpgradablePart> hasUpgrade;

	public MachineUpgradeManager(Predicate<UpgradablePart> hasUpgrade)
	{
		this.hasUpgrade = hasUpgrade;
	}

	@Override
	public void resetUpgrades()
	{
		map.clear();
	}

	@Override
	public boolean upgradePart(UpgradablePart part, int by)
	{
		if(hasUpgrade(part))
		{
			map.put(part, getUpgrade(part) + by);
			return true;
		}
		return false;
	}

	@Override
	public int getUpgrade(UpgradablePart part)
	{
		return map.getInt(part);
	}

	@Override
	public boolean hasUpgrade(UpgradablePart part)
	{
		return part != null && hasUpgrade.test(part);
	}
}