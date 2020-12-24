package org.zeith.comm3.alcheng.utils;

import com.zeitheron.hammercore.internal.capabilities.SidedCapabilityProvider;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import java.util.HashMap;
import java.util.function.BiFunction;

public class SidedCapabilityProviderV2
		extends SidedCapabilityProvider
{
	public <T> void putCapability(Capability<T> cap, T instance)
	{
		for(EnumFacing f : EnumFacing.VALUES)
			putCapability(f, cap, instance);
	}

	@Override
	public <T> void putCapability(EnumFacing side, Capability<T> cap, T instance)
	{
		CAPS.computeIfAbsent(side, k -> new HashMap<>()).put(cap, instance);
	}

	public <T> T getCapabilityOr(Capability<T> capability, EnumFacing facing, BiFunction<Capability<T>, EnumFacing, T> def)
	{
		if(!hasCapability(capability, facing)) return def.apply(capability, facing);
		return super.getCapability(capability, facing);
	}
}