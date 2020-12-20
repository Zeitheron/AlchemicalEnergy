package org.zeith.comm3.alcheng.init;

import com.zeitheron.hammercore.internal.SimpleRegistration;
import org.zeith.comm3.alcheng.AlchemicalEnergyMod;
import org.zeith.comm3.alcheng.blocks.BlockAlchemicalCondenser;
import org.zeith.comm3.alcheng.blocks.BlockAlchemicalEnergy;

public class BlocksAE
{
	public static final BlockAlchemicalEnergy ALCHEMICAL_ENERGY = new BlockAlchemicalEnergy();
	public static final BlockAlchemicalCondenser ALCHEMICAL_CONDENSER = new BlockAlchemicalCondenser();

	public static void register()
	{
		SimpleRegistration.registerFieldBlocksFrom(BlocksAE.class, InfoAE.MOD_ID, AlchemicalEnergyMod.TAB);
	}
}