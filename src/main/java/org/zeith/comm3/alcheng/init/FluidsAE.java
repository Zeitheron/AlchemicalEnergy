package org.zeith.comm3.alcheng.init;

import net.minecraftforge.fluids.FluidRegistry;
import org.zeith.comm3.alcheng.fluids.FluidAlchemicalEnergy;

public class FluidsAE
{
	public static final FluidAlchemicalEnergy ALCHEMICAL_ENERGY = new FluidAlchemicalEnergy();

	public static void register()
	{
		FluidRegistry.registerFluid(ALCHEMICAL_ENERGY);
		FluidRegistry.addBucketForFluid(ALCHEMICAL_ENERGY);
	}
}