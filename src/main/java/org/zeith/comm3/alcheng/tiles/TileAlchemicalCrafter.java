package org.zeith.comm3.alcheng.tiles;

import com.zeitheron.hammercore.api.crafting.ICraftingExecutor;
import com.zeitheron.hammercore.internal.blocks.base.BlockDeviceHC;
import com.zeitheron.hammercore.internal.blocks.base.IBlockEnableable;
import com.zeitheron.hammercore.internal.blocks.base.IBlockHorizontal;
import com.zeitheron.hammercore.net.props.NetPropertyAbstract;
import com.zeitheron.hammercore.net.props.NetPropertyNumber;
import com.zeitheron.hammercore.net.props.NetPropertyString;
import com.zeitheron.hammercore.tile.ITileDroppable;
import com.zeitheron.hammercore.tile.TileSyncableTickable;
import com.zeitheron.hammercore.utils.inventory.InventoryDummy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
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
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.zeith.comm3.alcheng.api.machines.IAlchemicalSink;
import org.zeith.comm3.alcheng.api.machines.IMachineTile;
import org.zeith.comm3.alcheng.api.machines.upgrades.IMachineUpgrades;
import org.zeith.comm3.alcheng.api.machines.upgrades.IUpgradeItem;
import org.zeith.comm3.alcheng.api.machines.upgrades.MachineUpgradeManager;
import org.zeith.comm3.alcheng.api.machines.upgrades.UpgradablePart;
import org.zeith.comm3.alcheng.init.BlocksAE;
import org.zeith.comm3.alcheng.init.FluidsAE;
import org.zeith.comm3.alcheng.init.RegistriesAE;
import org.zeith.comm3.alcheng.init.SoundsAE;
import org.zeith.comm3.alcheng.inventory.AlchemicalCrafter;
import org.zeith.comm3.alcheng.net.PacketPlayMachineSound;
import org.zeith.comm3.alcheng.recipes.types.RecipeAlchemicalCrafter;
import org.zeith.comm3.alcheng.utils.DynamicBigEnergyStorage;
import org.zeith.comm3.alcheng.utils.InventoryDummyCrafting;
import org.zeith.comm3.alcheng.utils.SidedCapabilityProviderV2;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class TileAlchemicalCrafter
		extends TileSyncableTickable
		implements ISidedInventory, IEnergyStorage, IFluidHandler, IMachineTile, IAlchemicalSink, ITileDroppable, ICraftingExecutor
{
	public final InventoryDummyCrafting items = new InventoryDummyCrafting(3, 3);
	public final InventoryDummy itemOutput = new InventoryDummy(1);

	public final InventoryDummy upgrades = new InventoryDummy(5);
	public final DynamicBigEnergyStorage energy = new DynamicBigEnergyStorage(5000);
	public final FluidTank fluid = new FluidTank(16000);
	public RecipeAlchemicalCrafter recipe;

	// net-synced properties, they also get stored to NBT automagically.
	public final NetPropertyString recipeId;
	public final NetPropertyNumber<Integer> fulfulledTicks;
	public final NetPropertyNumber<Integer> fluidBuffer;
	public final NetPropertyNumber<Long> feBuffer;
	public final NetPropertyNumber<Integer> maxTicks;

	private EnumFacing face;

	public final SidedCapabilityProviderV2 caps = new SidedCapabilityProviderV2();

	public TileAlchemicalCrafter()
	{
		this.recipeId = new NetPropertyString(this, "");
		this.fulfulledTicks = new NetPropertyNumber<>(this, 0);
		this.fluidBuffer = new NetPropertyNumber<>(this, 16000);
		this.maxTicks = new NetPropertyNumber<>(this, 0);
		this.feBuffer = new NetPropertyNumber<>(this, 5000L);

		this.energy.setMaxReceive(BigInteger.valueOf(Integer.MAX_VALUE));
		this.energy.setMaxExtract(BigInteger.valueOf(Integer.MAX_VALUE));

		this.upgrades.validSlots = (slot, stack) ->
		{
			UpgradablePart p = IUpgradeItem.fromStack(stack);
			return p != null && upgrades().hasUpgrade(p) && countUpgrades(p) < p.getMax();
		};

		caps.putCapability(CapabilityEnergy.ENERGY, this);
		caps.putCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, this);
		for(EnumFacing f : EnumFacing.VALUES)
			caps.putCapability(f, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, new SidedInvWrapper(this, f));

		items.listener = (slot, stack) ->
		{
			setRecipe(RegistriesAE.ALCHEMICAL_CRAFTER_RECIPES
					.getRecipes()
					.stream()
					.filter(this::canCraft)
					.findFirst()
					.orElse(null));
		};
	}

	@Override
	public void tick()
	{
		{
			IBlockState state = world.getBlockState(pos);
			if(state.getBlock() == BlocksAE.ALCHEMICAL_CONDENSER)
				face = state.getValue(IBlockHorizontal.FACING);
		}

		if(world.isRemote)
		{
			if(atTickRate(5))
			{
				if(recipeId.get().isEmpty())
					this.recipe = null;
				else
					this.recipe = RegistriesAE.ALCHEMICAL_CRAFTER_RECIPES.getRecipe(new ResourceLocation(recipeId.get()));
			}
		}

		fluid.setCapacity(fluidBuffer.get());
		energy.setFECapacity(BigInteger.valueOf(feBuffer.get()));

		if(atTickRate(4))
		{
			handleUpgrades();
			sendChangesToNearby();
		}

		if(!world.isRemote)
		{
			if(atTickRate(30))
				setRecipe(RegistriesAE.ALCHEMICAL_CRAFTER_RECIPES
						.getRecipes()
						.stream()
						.filter(this::canCraft)
						.findFirst()
						.orElse(null));

			boolean active = recipe != null;

			if(active)
			{
				int maxTicks = recipe.processTicks;

				int speeds = upgrades().getUpgrade(UpgradablePart.SPEED);

				maxTicks /= Math.pow(1.25F, speeds);
				if(speeds >= UpgradablePart.SPEED.getMax()) maxTicks = 10;

				int ept = computeEnergyRate(recipe.energyRate);

				if(energy.hasEnergy(ept) && fulfulledTicks.get() < maxTicks)
				{
					energy.extractEnergy(ept, false);
					fulfulledTicks.set(fulfulledTicks.get() + 1);
				}

				this.maxTicks.set(maxTicks);

				if(fulfulledTicks.get() >= maxTicks)
				{
					RecipeAlchemicalCrafter recipe = this.recipe;

					ItemStack output = recipe.getResult().getOutput(this);

					ItemStack slot9 = itemOutput.getStackInSlot(0);
					if(slot9.isEmpty() || (slot9.isItemEqual(output) && slot9.getCount() + output.getCount() < slot9.getMaxStackSize()))
					{
						if(slot9.isEmpty())
							itemOutput.setInventorySlotContents(0, output.copy());
						else
						{
							ItemStack out = slot9.copy();
							out.grow(output.getCount());
							itemOutput.setInventorySlotContents(0, out);
						}

						fluid.drain(recipe.inputMb, true);
						fulfulledTicks.set(0);
						for(int i = 0; i < 9; ++i) items.decrStackSize(i, 1);

						if(!recipe.matches(items, this))
							setRecipe(null);
					}
				}
			}

			boolean update;
			IBlockState state = world.getBlockState(pos);
			if(update = (state.getBlock() == BlocksAE.ALCHEMICAL_CONDENSER && state.getValue(IBlockEnableable.ENABLED).booleanValue() != active))
				BlockDeviceHC.updateStateKeepTile(world, pos, state.withProperty(IBlockEnableable.ENABLED, active));
			if(active && (update || atTickRate(30)))
				PacketPlayMachineSound.ensureStarted(world, pos, SoundsAE.MACHINES_ALCHEMICAL_CRAFTER, 1F, 1F);
		}
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
	{
		return oldState.getBlock() != newSate.getBlock();
	}

	@Override
	public void notifyOfChange(NetPropertyAbstract prop)
	{
		if(prop == recipeId)
		{
			if(recipeId.get().isEmpty())
				this.recipe = null;
			else
				this.recipe = RegistriesAE.ALCHEMICAL_CRAFTER_RECIPES.getRecipe(new ResourceLocation(recipeId.get()));
		}

		super.notifyOfChange(prop);
	}

	public void setRecipe(RecipeAlchemicalCrafter recipe)
	{
		if(recipe == null) fulfulledTicks.set(0);
		this.recipe = recipe;
		this.recipeId.set(recipe != null ? recipe.getRecipeName().toString() : "");
	}

	private boolean canCraft(RecipeAlchemicalCrafter recipe)
	{
		return recipe.matches(items, this)
				&& energy.hasEnergy(computeEnergyRate(recipe.energyRate))
				&& fluid.getFluidAmount() >= recipe.inputMb;
	}

	public int computeEnergyRate(int in)
	{
		in *= Math.pow(1.5F, upgrades().getUpgrade(UpgradablePart.SPEED));
		in /= Math.pow(1.25F, upgrades().getUpgrade(UpgradablePart.ENERGY));
		return in;
	}

	@Override
	public void createDrop(EntityPlayer player, World world, BlockPos pos)
	{
		InventoryHelper.dropInventoryItems(world, pos, items);
		InventoryHelper.dropInventoryItems(world, pos, upgrades);
	}

	@Override
	public boolean hasGui()
	{
		return true;
	}

	@Override
	public Object getClientGuiElement(EntityPlayer player)
	{
		return AlchemicalCrafter.createGUI(this, player);
	}

	@Override
	public Object getServerGuiElement(EntityPlayer player)
	{
		return AlchemicalCrafter.createContainer(this, player);
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
		nbt.setByteArray("Capacity", energy.getCapacity().toByteArray());
		energy.writeToNBT(nbt);
		nbt.setTag("Fluids", fluid.writeToNBT(new NBTTagCompound()));
		nbt.setTag("Items", items.writeToNBT(new NBTTagCompound()));
		nbt.setTag("ItemsOut", itemOutput.writeToNBT(new NBTTagCompound()));
		nbt.setTag("Upgrades", upgrades.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void readNBT(NBTTagCompound nbt)
	{
		if(world == null)
			fluid.setCapacity(1600000);
		energy.setFECapacity(new BigInteger(nbt.getByteArray("Capacity")));
		energy.readFromNBT(nbt);
		fluid.readFromNBT(nbt.getCompoundTag("Fluids"));
		items.readFromNBT(nbt.getCompoundTag("Items"));
		itemOutput.readFromNBT(nbt.getCompoundTag("ItemsOut"));
		upgrades.readFromNBT(nbt.getCompoundTag("Upgrades"));
	}

	public float getProgress(float partialTicks)
	{
		if(recipe != null && maxTicks.get() > 0)
			return Math.min(1F, (fulfulledTicks.get() + partialTicks) / (float) maxTicks.get());
		return 0F;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side)
	{
		return new int[]{
				0,
				1,
				2,
				3,
				4,
				5,
				6,
				7,
				8,
				9
		};
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction)
	{
		return index < 9;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction)
	{
		return index == 9;
	}

	@Override
	public int getSizeInventory()
	{
		return items.getSizeInventory() + 1;
	}

	@Override
	public boolean isEmpty()
	{
		return items.isEmpty();
	}

	@Override
	public ItemStack getStackInSlot(int index)
	{
		if(index >= 9) return itemOutput.getStackInSlot(index - 9);
		return items.getStackInSlot(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count)
	{
		if(index >= 9) return itemOutput.decrStackSize(index - 9, count);
		return items.decrStackSize(index, count);
	}

	@Override
	public ItemStack removeStackFromSlot(int index)
	{
		if(index >= 9) return itemOutput.removeStackFromSlot(index - 9);
		return items.removeStackFromSlot(index);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack)
	{
		if(index >= 9) itemOutput.setInventorySlotContents(index - 9, stack);
		items.setInventorySlotContents(index, stack);
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player)
	{
		return items.isUsableByPlayer(player, pos);
	}

	@Override
	public void openInventory(EntityPlayer player)
	{
	}

	@Override
	public void closeInventory(EntityPlayer player)
	{
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack)
	{
		return index < 9;
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

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate)
	{
		return energy.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate)
	{
		return 0;
	}

	@Override
	public int getEnergyStored()
	{
		return energy.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored()
	{
		return energy.getMaxEnergyStored();
	}

	@Override
	public boolean canExtract()
	{
		return false;
	}

	@Override
	public boolean canReceive()
	{
		return true;
	}

	@Override
	public IFluidTankProperties[] getTankProperties()
	{
		return fluid.getTankProperties();
	}

	@Override
	public int fill(FluidStack resource, boolean doFill)
	{
		if(resource == null || resource.getFluid() != FluidsAE.ALCHEMICAL_ENERGY)
			return 0;
		return fluid.fill(resource, doFill);
	}

	@Nullable
	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Nullable
	@Override
	public FluidStack drain(int maxDrain, boolean doDrain)
	{
		return null;
	}

	private static final List<UpgradablePart> VALID_PARTS = Arrays.asList(UpgradablePart.SPEED, UpgradablePart.ENERGY, UpgradablePart.ALCHEMICAL_BUFFER, UpgradablePart.FE_BUFFER);
	public final IMachineUpgrades upgr = new MachineUpgradeManager(VALID_PARTS::contains);

	@Override
	public void handleUpgrades()
	{
		upgr.resetUpgrades();
		for(UpgradablePart part : VALID_PARTS) upgr.upgradePart(part, part.computeUpgradability(this));

		int buf = 16000 + Math.round(upgr.getUpgrade(UpgradablePart.ALCHEMICAL_BUFFER) * 8000F);
		fluidBuffer.set(buf);

		int c = upgr.getUpgrade(UpgradablePart.FE_BUFFER);
		long ebuf = 5000L;
		ebuf *= Math.pow(2, c);
		if(c >= UpgradablePart.FE_BUFFER.getMax())
			ebuf = Long.MAX_VALUE;
		feBuffer.set(ebuf);
	}

	@Override
	public IMachineUpgrades upgrades()
	{
		return upgr;
	}

	@Override
	public IInventory upgradeInventory()
	{
		return upgrades;
	}

	@Override
	public int getSuction()
	{
		return fluid.getFluidAmount() < fluid.getCapacity() ? 256 : 0;
	}

	@Override
	public IFluidHandler alchemicalFluidHandler()
	{
		return fluid;
	}

	@Override
	public boolean connectsTo(EnumFacing towards)
	{
		return true;
	}
}