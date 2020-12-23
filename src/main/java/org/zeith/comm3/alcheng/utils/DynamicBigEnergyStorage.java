package org.zeith.comm3.alcheng.utils;

import com.zeitheron.hammercore.utils.energy.BigEnergyStorage;
import com.zeitheron.hammercore.utils.math.BigMath;

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
		this.energy = BigMath.min(this.energy, this.capacity);
		return this;
	}

	public DynamicBigEnergyStorage setMaxReceive(BigInteger receive)
	{
		this.maxReceive = receive;
		return this;
	}

	public DynamicBigEnergyStorage setMaxExtract(BigInteger extract)
	{
		this.maxExtract = extract;
		return this;
	}

	public boolean hasEnergy(Number fe)
	{
		return BigMath.isAGreaterThenB(energy, BigInteger.valueOf(fe.longValue()), false);
	}

	public double getFilledProgress()
	{
		return energy.multiply(BigInteger.valueOf(1000L)).divide(capacity).doubleValue() / 1000D;
	}
}