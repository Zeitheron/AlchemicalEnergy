package org.zeith.comm3.alcheng.utils;

import com.zeitheron.hammercore.utils.energy.BigEnergyStorage;
import com.zeitheron.hammercore.utils.math.BigMath;

import java.math.BigInteger;

public class BigFEEnergyStorage
		extends BigEnergyStorage
{
	public BigFEEnergyStorage(Number capacity)
	{
		super(BigInteger.valueOf(capacity.longValue()));
	}

	public BigFEEnergyStorage(BigInteger capacity)
	{
		super(capacity);
	}

	public BigFEEnergyStorage(BigInteger capacity, BigInteger maxTransfer)
	{
		super(capacity, maxTransfer);
	}

	public BigFEEnergyStorage(BigInteger capacity, BigInteger maxReceive, BigInteger maxExtract)
	{
		super(capacity, maxReceive, maxExtract);
	}

	public BigFEEnergyStorage(BigInteger capacity, BigInteger maxReceive, BigInteger maxExtract, BigInteger energy)
	{
		super(capacity, maxReceive, maxExtract, energy);
	}

	public BigFEEnergyStorage setFECapacity(BigInteger capacity)
	{
		this.capacity = capacity;
		return this;
	}

	public boolean hasEnergy(Number fe)
	{
		return BigMath.isAGreaterThenB(energy, BigInteger.valueOf(fe.longValue()), false);
	}
}