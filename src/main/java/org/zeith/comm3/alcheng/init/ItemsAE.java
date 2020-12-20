package org.zeith.comm3.alcheng.init;

import com.zeitheron.hammercore.internal.SimpleRegistration;
import org.zeith.comm3.alcheng.AlchemicalEnergyMod;
import org.zeith.comm3.alcheng.api.machines.upgrades.UpgradablePart;
import org.zeith.comm3.alcheng.items.ItemSimpleUpgrade;

public class ItemsAE
{
	public static final ItemSimpleUpgrade UPGRADES_ALCHEMICAL_BUFFER = new ItemSimpleUpgrade(UpgradablePart.ALCHEMICAL_BUFFER);
	public static final ItemSimpleUpgrade UPGRADES_ENERGY = new ItemSimpleUpgrade(UpgradablePart.ENERGY);
	public static final ItemSimpleUpgrade UPGRADES_RF_BUFFER = new ItemSimpleUpgrade(UpgradablePart.RF_BUFFER);
	public static final ItemSimpleUpgrade UPGRADES_SPEED = new ItemSimpleUpgrade(UpgradablePart.SPEED);
	public static final ItemSimpleUpgrade UPGRADES_STACK = new ItemSimpleUpgrade(UpgradablePart.STACK);

	public static void register()
	{
		SimpleRegistration.registerFieldItemsFrom(ItemsAE.class, InfoAE.MOD_ID, AlchemicalEnergyMod.TAB);
	}
}