package org.zeith.comm3.alcheng.api.machines;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.capability.IFluidHandler;

public interface IAlchemicalSink
{
	int getSuction();

	IFluidHandler alchemicalFluidHandler();

	boolean connectsTo(EnumFacing towards);
}