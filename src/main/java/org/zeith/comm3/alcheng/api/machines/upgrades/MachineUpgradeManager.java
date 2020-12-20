package org.zeith.comm3.alcheng.api.machines.upgrades;

import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;

import java.util.function.Predicate;

public class MachineUpgradeManager
		implements IMachineUpgrades
{
	public final Object2FloatArrayMap<UpgradablePart> map = new Object2FloatArrayMap<>();

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
	public boolean upgradePart(UpgradablePart part, float by)
	{
		if(hasUpgrade(part))
		{
			map.put(part, getUpgrade(part) + by);
			return true;
		}
		return false;
	}

	@Override
	public float getUpgrade(UpgradablePart part)
	{
		return map.getFloat(part);
	}

	@Override
	public boolean hasUpgrade(UpgradablePart part)
	{
		return part != null && hasUpgrade.test(part);
	}
}