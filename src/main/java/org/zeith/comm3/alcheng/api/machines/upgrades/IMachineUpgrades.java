package org.zeith.comm3.alcheng.api.machines.upgrades;

public interface IMachineUpgrades
{
	void resetUpgrades();

	boolean upgradePart(UpgradablePart part, float by);

	float getUpgrade(UpgradablePart part);

	boolean hasUpgrade(UpgradablePart part);
}