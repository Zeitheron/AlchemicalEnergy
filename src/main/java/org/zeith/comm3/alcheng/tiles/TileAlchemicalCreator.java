package org.zeith.comm3.alcheng.tiles;

import com.zeitheron.hammercore.net.props.NetPropertyBool;
import com.zeitheron.hammercore.tile.TileSyncableTickable;
import com.zeitheron.hammercore.utils.FluidEnergyAccessPoint;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.zeith.comm3.alcheng.init.FluidsAE;
import org.zeith.comm3.alcheng.inventory.AlchemicalCreator;
import org.zeith.comm3.alcheng.utils.SidedCapabilityProviderV2;

import javax.annotation.Nullable;

public class TileAlchemicalCreator
		extends TileSyncableTickable
		implements IFluidHandler
{
	public final FluidTank fluid = new FluidTank(8000);

	public final NetPropertyBool autoExtract;

	public final SidedCapabilityProviderV2 caps = new SidedCapabilityProviderV2();

	public TileAlchemicalCreator()
	{
		this.autoExtract = new NetPropertyBool(this, false);

		caps.putCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, this);
	}

	FluidEnergyAccessPoint FEAP;

	@Override
	public void tick()
	{
		fluid.fill(new FluidStack(FluidsAE.ALCHEMICAL_ENERGY, 400), true);

		if(autoExtract.get())
		{
			if(FEAP == null) FEAP = FluidEnergyAccessPoint.create(world, pos);
			if(fluid.getFluidAmount() > 0) FEAP.emitFluid(fluid.getFluid());
		}
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
	{
		return oldState.getBlock() != newSate.getBlock();
	}

	@Override
	public boolean hasGui()
	{
		return true;
	}

	@Override
	public Object getClientGuiElement(EntityPlayer player)
	{
		return AlchemicalCreator.createGUI(this, player);
	}

	@Override
	public Object getServerGuiElement(EntityPlayer player)
	{
		return AlchemicalCreator.createContainer(this, player);
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

	@Override
	public void writeNBT(NBTTagCompound nbt)
	{
		nbt.setTag("Fluids", fluid.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void readNBT(NBTTagCompound nbt)
	{
		fluid.readFromNBT(nbt.getCompoundTag("Fluids"));
	}

	@Override
	public IFluidTankProperties[] getTankProperties()
	{
		return fluid.getTankProperties();
	}

	@Override
	public int fill(FluidStack resource, boolean doFill)
	{
		return 0;
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
}