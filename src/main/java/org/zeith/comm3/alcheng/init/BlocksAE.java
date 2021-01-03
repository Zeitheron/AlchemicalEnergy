package org.zeith.comm3.alcheng.init;

import com.zeitheron.hammercore.internal.SimpleRegistration;
import org.zeith.comm3.alcheng.AlchemicalEnergyMod;
import org.zeith.comm3.alcheng.blocks.BlockAlchemicalCondenser;
import org.zeith.comm3.alcheng.blocks.BlockAlchemicalCreator;
import org.zeith.comm3.alcheng.blocks.BlockAlchemicalEnergy;
import org.zeith.comm3.alcheng.blocks.BlockAlchemicalPipe;

public class BlocksAE
{
	public static final BlockAlchemicalEnergy ALCHEMICAL_ENERGY = new BlockAlchemicalEnergy();
	public static final BlockAlchemicalCondenser ALCHEMICAL_CONDENSER = new BlockAlchemicalCondenser();
	public static final BlockAlchemicalCreator ALCHEMICAL_CREATOR = new BlockAlchemicalCreator();
	public static final BlockAlchemicalPipe ALCHEMICAL_PIPES_BASIC = new BlockAlchemicalPipe(25, "basic");
	public static final BlockAlchemicalPipe ALCHEMICAL_PIPES_ADVANCED = new BlockAlchemicalPipe(100, "advanced");
	public static final BlockAlchemicalPipe ALCHEMICAL_PIPES_EXPERT = new BlockAlchemicalPipe(100, "expert");
	public static final BlockAlchemicalPipe ALCHEMICAL_PIPES_CREATIVE = new BlockAlchemicalPipe(Integer.MAX_VALUE, "creative");

	public static void register()
	{
		SimpleRegistration.registerFieldBlocksFrom(BlocksAE.class, InfoAE.MOD_ID, AlchemicalEnergyMod.TAB);
	}
}