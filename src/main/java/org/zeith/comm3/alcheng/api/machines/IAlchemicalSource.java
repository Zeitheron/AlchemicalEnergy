package org.zeith.comm3.alcheng.api.machines;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.capability.IFluidHandler;

public interface IAlchemicalSource
{
	IFluidHandler alchemicalFluidHandler();

	boolean connectsTo(EnumFacing towards);
}