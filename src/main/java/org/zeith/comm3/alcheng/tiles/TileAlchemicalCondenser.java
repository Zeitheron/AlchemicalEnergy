package org.zeith.comm3.alcheng.tiles;

import com.zeitheron.hammercore.internal.blocks.base.BlockDeviceHC;
import com.zeitheron.hammercore.internal.blocks.base.IBlockEnableable;
import com.zeitheron.hammercore.net.props.*;
import com.zeitheron.hammercore.tile.ITileDroppable;
import com.zeitheron.hammercore.tile.TileSyncableTickable;
import com.zeitheron.hammercore.utils.FluidEnergyAccessPoint;
import com.zeitheron.hammercore.utils.inventory.InventoryDummy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.zeith.comm3.alcheng.init.BlocksAE;
import org.zeith.comm3.alcheng.init.FluidsAE;
import org.zeith.comm3.alcheng.init.RegistriesAE;
import org.zeith.comm3.alcheng.init.SoundsAE;
import org.zeith.comm3.alcheng.net.PacketPlayMachineSound;
import org.zeith.comm3.alcheng.recipes.types.RecipeAlchemicalCondenser;
import org.zeith.comm3.alcheng.utils.BigFEEnergyStorage;

import javax.annotation.Nullable;

public class TileAlchemicalCondenser
		extends TileSyncableTickable
		implements ISidedInventory, ITileDroppable
{
	public final InventoryDummy items = new InventoryDummy(1);
	public final BigFEEnergyStorage energy = new BigFEEnergyStorage(5000);
	public final FluidTank fluid = new FluidTank(16000);
	public RecipeAlchemicalCondenser recipe;

	// net-synced properties, they also get stored to NBT automagically.
	public final NetPropertyString recipeId;
	public final NetPropertyNumber<Integer> fulfulledTicks;
	public final NetPropertyBool autoExtract;
	public final NetPropertyItemStack onHold;

	public TileAlchemicalCondenser()
	{
		this.recipeId = new NetPropertyString(this, "");
		this.fulfulledTicks = new NetPropertyNumber<>(this, 0);
		this.autoExtract = new NetPropertyBool(this, false);
		this.onHold = new NetPropertyItemStack(this, ItemStack.EMPTY);
	}

	FluidEnergyAccessPoint FEAP;

	@Override
	public void tick()
	{
		if(FEAP == null) FEAP = FluidEnergyAccessPoint.create(world, pos);
		if(fluid.getFluidAmount() > 0) fluid.drain(FEAP.emitFluid(fluid.getFluid()), true);

		if(!world.isRemote && onHold.get().isEmpty())
			if(!items.getStackInSlot(0).isEmpty())
			{
				if(atTickRate(30))
					RegistriesAE.ALCHEMICAL_CONDENSER_RECIPES
							.getRecipes()
							.stream()
							.filter(this::canCraft)
							.findFirst()
							.ifPresent(this::setRecipe);
			} else
			{
				setRecipe(null);
			}

		boolean active = recipe != null;

		if(active)
		{
			if(energy.hasEnergy(recipe.energyRate) && fulfulledTicks.get() < recipe.processTicks)
			{
				energy.extractEnergy(recipe.energyRate, false);
				fulfulledTicks.set(fulfulledTicks.get() + 1);
			}

			if(fulfulledTicks.get() >= recipe.processTicks)
			{
				int canFit = fluid.getCapacity() - fluid.getFluidAmount();
				if(canFit >= recipe.outputMb)
				{
					fluid.fill(new FluidStack(FluidsAE.ALCHEMICAL_ENERGY, recipe.outputMb), true);
					fulfulledTicks.set(0);
					onHold.set(ItemStack.EMPTY);
				}
			}
		}

		if(!world.isRemote)
		{
			boolean update;
			IBlockState state = world.getBlockState(pos);
			if(update = (state.getBlock() == BlocksAE.ALCHEMICAL_CONDENSER && state.getValue(IBlockEnableable.ENABLED).booleanValue() != active))
				BlockDeviceHC.updateStateKeepTile(world, pos, state.withProperty(IBlockEnableable.ENABLED, active));
			if(active && (update || atTickRate(30)))
				PacketPlayMachineSound.ensureStarted(world, pos, SoundsAE.MACHINES_ALCHEMICAL_CONDENSER, 1F, 1F);
		}
	}

	@Override
	public void notifyOfChange(NetPropertyAbstract prop)
	{
		if(prop == recipeId)
		{
			if(recipeId.get().isEmpty())
				this.recipe = null;
			else
				this.recipe = RegistriesAE.ALCHEMICAL_CONDENSER_RECIPES.getRecipe(new ResourceLocation(recipeId.get()));
		}

		super.notifyOfChange(prop);
	}

	public void setRecipe(RecipeAlchemicalCondenser recipe)
	{
		this.recipe = recipe;
		this.recipeId.set(recipe != null ? recipe.getRecipeName().toString() : "");
		this.fulfulledTicks.set(0);
		if(recipe != null)
			onHold.set(items.decrStackSize(0, 1));
	}

	private boolean canCraft(RecipeAlchemicalCondenser recipe)
	{
		ItemStack held = onHold.get();
		if(!held.isEmpty())
			return recipe.input.test(held);
		return recipe.input.test(items.getStackInSlot(0)) && energy.hasEnergy(recipe.energyRate);
	}

	@Override
	public void createDrop(EntityPlayer player, World world, BlockPos pos)
	{
		InventoryHelper.dropInventoryItems(world, pos, items);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		return capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
	}

	@Nullable
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		return capability == CapabilityEnergy.ENERGY ? (T) energy : super.getCapability(capability, facing);
	}

	@Override
	public void writeNBT(NBTTagCompound nbt)
	{
		energy.writeToNBT(nbt);
		nbt.setTag("Fluids", fluid.writeToNBT(new NBTTagCompound()));
		nbt.setTag("Items", items.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void readNBT(NBTTagCompound nbt)
	{
		energy.readFromNBT(nbt);
		fluid.readFromNBT(nbt.getCompoundTag("Fluids"));
		items.readFromNBT(nbt.getCompoundTag("Items"));
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side)
	{
		return new int[]{ 0 };
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack)
	{
		return RegistriesAE.ALCHEMICAL_CONDENSER_RECIPES.getRecipes().stream().anyMatch(r -> r.input.test(stack));
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction)
	{
		return isItemValidForSlot(index, itemStackIn);
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction)
	{
		return true;
	}

	@Override
	public int getSizeInventory()
	{
		return items.getSizeInventory();
	}

	@Override
	public boolean isEmpty()
	{
		return items.isEmpty();
	}

	@Override
	public ItemStack getStackInSlot(int index)
	{
		return items.getStackInSlot(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count)
	{
		return items.decrStackSize(index, count);
	}

	@Override
	public ItemStack removeStackFromSlot(int index)
	{
		return items.removeStackFromSlot(index);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack)
	{
		items.setInventorySlotContents(index, stack);
	}

	@Override
	public int getInventoryStackLimit()
	{
		return items.getInventoryStackLimit();
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player)
	{
		return items.isUsableByPlayer(player);
	}

	@Override
	public void openInventory(EntityPlayer player)
	{
		items.openInventory(player);
	}

	@Override
	public void closeInventory(EntityPlayer player)
	{
		items.closeInventory(player);
	}

	@Override
	public int getField(int id)
	{
		return 0;
	}

	@Override
	public void setField(int id, int value)
	{

	}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear()
	{
		items.clear();
	}

	@Override
	public String getName()
	{
		return getBlockType().getLocalizedName();
	}

	@Override
	public boolean hasCustomName()
	{
		return false;
	}
}