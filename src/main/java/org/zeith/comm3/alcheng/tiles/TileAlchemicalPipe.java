package org.zeith.comm3.alcheng.tiles;

import com.zeitheron.hammercore.tile.TileSyncableTickable;
import com.zeitheron.hammercore.utils.math.vec.Cuboid6;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.zeith.comm3.alcheng.api.machines.IAlchemicalSink;
import org.zeith.comm3.alcheng.api.machines.IAlchemicalSource;
import org.zeith.comm3.alcheng.init.FluidsAE;
import org.zeith.comm3.alcheng.utils.SidedCapabilityProviderV2;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileAlchemicalPipe
		extends TileSyncableTickable
		implements IAlchemicalSink, IFluidHandler
{
	public int suction;

	public final FluidTank fluid;

	public boolean cubesDirty;

	public Cuboid6[] connections = new Cuboid6[1];

	public final boolean[] disableConnections = new boolean[6];

	public final SidedCapabilityProviderV2 caps = new SidedCapabilityProviderV2();

	public TileAlchemicalPipe(int capacity)
	{
		this.fluid = new FluidTank(capacity);
		this.connections[0] = new Cuboid6(5 / 16D, 5 / 16D, 5 / 16D, 11 / 16D, 11 / 16D, 11 / 16D);
		this.caps.putCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, this);
	}

	public TileAlchemicalPipe()
	{
		this(25);
	}

	private static final ThreadLocal<List<IFluidHandler>> threadAcceptors = ThreadLocal.withInitial(ArrayList::new);

	public boolean hasConnection(EnumFacing facing)
	{
		TileEntity te;
		return ((te = world.getTileEntity(pos.offset(facing))) instanceof IAlchemicalSink
				&& ((IAlchemicalSink) te).connectsTo(facing.getOpposite())) ||
				(te instanceof IAlchemicalSource) ||
				(te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite()));
	}

	public List<EnumFacing> getConnections()
	{
		List<EnumFacing> ef = new ArrayList<>();
		for(EnumFacing facing : EnumFacing.VALUES) if(hasConnection(facing)) ef.add(facing);
		return ef;
	}

	@Override
	public void tick()
	{
		if(atTickRate(20) || cubesDirty)
		{
			if(cubesDirty) sendChangesToNearby();
			cubesDirty = false;

			List<Cuboid6> cuboids = new ArrayList<>();

			cuboids.add(new Cuboid6(5 / 16D, 5 / 16D, 5 / 16D, 11 / 16D, 11 / 16D, 11 / 16D));

			if(hasConnection(EnumFacing.DOWN))
				cuboids.add(new Cuboid6(5 / 16D, 0D, 5 / 16D, 11 / 16D, 5 / 16D, 11 / 16D));
			if(hasConnection(EnumFacing.UP))
				cuboids.add(new Cuboid6(5 / 16D, 11 / 16D, 5 / 16D, 11 / 16D, 1D, 11 / 16D));
			if(hasConnection(EnumFacing.NORTH))
				cuboids.add(new Cuboid6(5 / 16D, 5 / 16D, 0D, 11 / 16D, 11 / 16D, 5 / 16D));
			if(hasConnection(EnumFacing.SOUTH))
				cuboids.add(new Cuboid6(5 / 16D, 5 / 16D, 11 / 16D, 11 / 16D, 11 / 16D, 1D));
			if(hasConnection(EnumFacing.WEST))
				cuboids.add(new Cuboid6(0D, 5 / 16D, 5 / 16D, 5 / 16D, 11 / 16D, 11 / 16D));
			if(hasConnection(EnumFacing.EAST))
				cuboids.add(new Cuboid6(11 / 16D, 5 / 16D, 5 / 16D, 1D, 11 / 16D, 11 / 16D));

			this.connections = cuboids.toArray(new Cuboid6[0]);
		}

		if(!world.isRemote)
		{
			this.suction = Math.max(0, gatherStrongestSuctionFromSides() - 1);

			if(suction > 0)
			{
				for(EnumFacing ef : EnumFacing.VALUES)
				{
					BlockPos pos = this.pos.offset(ef);
					TileEntity tile = world.getTileEntity(pos);
					if(tile instanceof IAlchemicalSource)
					{
						IFluidHandler handler = ((IAlchemicalSource) tile).alchemicalFluidHandler();
						if(FluidUtil.tryFluidTransfer(this, handler, fluid.getCapacity(), false) != null)
							sendChangesToNearby();
					}
				}
			}

			List<IFluidHandler> flHnds = threadAcceptors.get();

			flHnds.clear();

			for(EnumFacing ef : EnumFacing.VALUES)
			{
				if(!connectsTo(ef)) continue;
				BlockPos pos = this.pos.offset(ef);
				TileEntity tile = world.getTileEntity(pos);
				if(tile instanceof IAlchemicalSink)
				{
					IAlchemicalSink pipe = (IAlchemicalSink) tile;
					if(pipe.getSuction() > this.suction && pipe.connectsTo(ef.getOpposite()))
						flHnds.add(pipe.alchemicalFluidHandler());
				} else if(tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, ef.getOpposite()))
				{
					IFluidHandler fh = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, ef.getOpposite());
					if(fh != null && fh.fill(new FluidStack(FluidsAE.ALCHEMICAL_ENERGY, Fluid.BUCKET_VOLUME), false) > 0)
						flHnds.add(fh);
				}
			}

			int tot = flHnds.size();
			if(tot > 0 && fluid.getFluidAmount() > 0)
			{
				int per = fluid.getCapacity() / tot;
				for(IFluidHandler handler : flHnds)
					if(FluidUtil.tryFluidTransfer(handler, this, per, true) != null)
						sendChangesToNearby();
				if(fluid.getFluidAmount() > 0)
					for(IFluidHandler handler : flHnds)
						if(FluidUtil.tryFluidTransfer(handler, this, fluid.getCapacity(), true) != null)
							sendChangesToNearby();
			}
		}
	}

	@Override
	public void onSynced()
	{
		super.onSynced();
		this.cubesDirty = true;
	}

	public int gatherStrongestSuctionFromSides()
	{
		int s = suction;
		for(EnumFacing ef : EnumFacing.VALUES)
		{
			BlockPos pos = this.pos.offset(ef);
			TileEntity tile = world.getTileEntity(pos);
			if(tile instanceof IAlchemicalSink)
			{
				IAlchemicalSink pipe = (IAlchemicalSink) tile;
				s = Math.max(pipe.getSuction(), s);
			} else if(tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, ef.getOpposite()))
			{
				IFluidHandler fh = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, ef.getOpposite());
				if(fh != null && fh.fill(new FluidStack(FluidsAE.ALCHEMICAL_ENERGY, Fluid.BUCKET_VOLUME), false) > 0)
					s = 128;
			}
		}
		return s;
	}

	@Override
	public void writeNBT(NBTTagCompound nbt)
	{
		nbt.setInteger("Suction", suction);
		nbt.setInteger("Capacity", fluid.getCapacity());
		nbt.setTag("Fluid", fluid.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void readNBT(NBTTagCompound nbt)
	{
		this.suction = nbt.getInteger("Suction");
		this.fluid.setCapacity(nbt.getInteger("Capacity"));
		this.fluid.readFromNBT(nbt.getCompoundTag("Fluid"));
	}

	@Override
	public int getSuction()
	{
		return suction;
	}

	@Override
	public IFluidHandler alchemicalFluidHandler()
	{
		return this;
	}

	@Override
	public boolean connectsTo(EnumFacing towards)
	{
		return !disableConnections[towards.ordinal()];
	}

	@Override
	public IFluidTankProperties[] getTankProperties()
	{
		return fluid.getTankProperties();
	}

	@Override
	public int fill(FluidStack resource, boolean doFill)
	{
		if(resource.getFluid() != FluidsAE.ALCHEMICAL_ENERGY)
			return 0;
		return fluid.fill(resource, doFill);
	}

	@Nullable
	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain)
	{
		return fluid.drain(resource, doDrain);
	}

	@Nullable
	@Override
	public FluidStack drain(int maxDrain, boolean doDrain)
	{
		return fluid.drain(maxDrain, doDrain);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		return caps.hasCapability(capability, facing) || super.hasCapability(capability, facing);
	}

	@Nullable
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		return caps.getCapabilityOr(capability, facing, super::getCapability);
	}
}