package org.zeith.comm3.alcheng.utils;

import com.zeitheron.hammercore.utils.energy.BigEnergyStorage;
import com.zeitheron.hammercore.utils.math.BigMath;

import java.math.BigDecimal;
import java.math.BigInteger;

public class DynamicBigEnergyStorage
		extends BigEnergyStorage
{
	public DynamicBigEnergyStorage(Number capacity)
	{
		super(BigInteger.valueOf(capacity.longValue()));
	}

	public DynamicBigEnergyStorage(BigInteger capacity)
	{
		super(capacity);
	}

	public DynamicBigEnergyStorage(BigInteger capacity, BigInteger maxTransfer)
	{
		super(capacity, maxTransfer);
	}

	public DynamicBigEnergyStorage(BigInteger capacity, BigInteger maxReceive, BigInteger maxExtract)
	{
		super(capacity, maxReceive, maxExtract);
	}

	public DynamicBigEnergyStorage(BigInteger capacity, BigInteger maxReceive, BigInteger maxExtract, BigInteger energy)
	{
		super(capacity, maxReceive, maxExtract, energy);
	}

	public DynamicBigEnergyStorage setFECapacity(BigInteger capacity)
	{
		this.capacity = capacity;
		return this;
	}

	public boolean hasEnergy(Number fe)
	{
		return BigMath.isAGreaterThenB(energy, BigInteger.valueOf(fe.longValue()), false);
	}

	public double getFilledProgress()
	{
		return new BigDecimal(energy).divide(new BigDecimal(capacity)).doubleValue();
	}
}