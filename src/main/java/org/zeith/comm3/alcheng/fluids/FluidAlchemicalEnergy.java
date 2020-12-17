package org.zeith.comm3.alcheng.fluids;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import org.zeith.comm3.alcheng.init.InfoAE;

public class FluidAlchemicalEnergy
		extends Fluid
{
	public FluidAlchemicalEnergy()
	{
		super("alchemical_energy", new ResourceLocation(InfoAE.MOD_ID, "blocks/alchemical_energy_still"), new ResourceLocation(InfoAE.MOD_ID, "blocks/alchemical_energy_flow"));
		setUnlocalizedName(InfoAE.MOD_ID + ":alchemical_energy");
		viscosity = 2000;
	}
}