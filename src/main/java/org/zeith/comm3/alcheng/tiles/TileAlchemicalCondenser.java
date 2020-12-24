package org.zeith.comm3.alcheng.tiles;

import com.zeitheron.hammercore.internal.blocks.base.BlockDeviceHC;
import com.zeitheron.hammercore.internal.blocks.base.IBlockEnableable;
import com.zeitheron.hammercore.internal.blocks.base.IBlockHorizontal;
import com.zeitheron.hammercore.net.props.NetPropertyAbstract;
import com.zeitheron.hammercore.net.props.NetPropertyBool;
import com.zeitheron.hammercore.net.props.NetPropertyNumber;
import com.zeitheron.hammercore.net.props.NetPropertyString;
import com.zeitheron.hammercore.tile.ITileDroppable;
import com.zeitheron.hammercore.tile.TileSyncableTickable;
import com.zeitheron.hammercore.utils.FluidEnergyAccessPoint;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.zeith.comm3.alcheng.api.machines.IAlchemicalSource;
import org.zeith.comm3.alcheng.api.machines.IMachineTile;
import org.zeith.comm3.alcheng.api.machines.upgrades.IMachineUpgrades;
import org.zeith.comm3.alcheng.api.machines.upgrades.IUpgradeItem;
import org.zeith.comm3.alcheng.api.machines.upgrades.MachineUpgradeManager;
import org.zeith.comm3.alcheng.api.machines.upgrades.UpgradablePart;
import org.zeith.comm3.alcheng.init.BlocksAE;
import org.zeith.comm3.alcheng.init.FluidsAE;
import org.zeith.comm3.alcheng.init.RegistriesAE;
import org.zeith.comm3.alcheng.init.SoundsAE;
import org.zeith.comm3.alcheng.inventory.AlchemicalCondenser;
import org.zeith.comm3.alcheng.net.PacketPlayMachineSound;
import org.zeith.comm3.alcheng.recipes.types.RecipeAlchemicalCondenser;
import org.zeith.comm3.alcheng.utils.DynamicBigEnergyStorage;
import org.zeith.comm3.alcheng.utils.SidedCapabilityProviderV2;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class TileAlchemicalCondenser
		extends TileSyncableTickable
		implements ISidedInventory, ITileDroppable, IMachineTile, IFluidHandler, IAlchemicalSource
{
	public final InventoryDummy items = new InventoryDummy(1);
	public final InventoryDummy upgrades = new InventoryDummy(5);
	public final DynamicBigEnergyStorage energy = new DynamicBigEnergyStorage(5000);
	public final FluidTank fluid = new FluidTank(16000);
	public RecipeAlchemicalCondenser recipe;

	// net-synced properties, they also get stored to NBT automagically.
	public final NetPropertyString recipeId;
	public final NetPropertyNumber<Integer> fulfulledTicks;
	public final NetPropertyBool autoExtract;
	public final NetPropertyNumber<Integer> fluidBuffer;
	public final NetPropertyNumber<Long> feBuffer;
	public final NetPropertyNumber<Integer> maxTicks;

	private EnumFacing face;

	public final SidedCapabilityProviderV2 caps = new SidedCapabilityProviderV2();

	public TileAlchemicalCondenser()
	{
		this.recipeId = new NetPropertyString(this, "");
		this.fulfulledTicks = new NetPropertyNumber<>(this, 0);
		this.autoExtract = new NetPropertyBool(this, false);
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

		caps.putCapability(CapabilityEnergy.ENERGY, energy);
		caps.putCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, this);
	}

	FluidEnergyAccessPoint FEAP;

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
					this.recipe = RegistriesAE.ALCHEMICAL_CONDENSER_RECIPES.getRecipe(new ResourceLocation(recipeId.get()));
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
			if(autoExtract.get())
			{
				if(FEAP == null) FEAP = FluidEnergyAccessPoint.create(world, pos);
				if(fluid.getFluidAmount() > 0) FEAP.emitFluid(fluid.getFluid());
			}

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
				this.fulfulledTicks.set(0);
			}

			boolean active = recipe != null;

			if(active)
			{
				int maxTicks = recipe.processTicks;

				int stacks = upgrades().getUpgrade(UpgradablePart.STACK);
				int speeds = upgrades().getUpgrade(UpgradablePart.SPEED);

				maxTicks *= Math.pow(1.264634, stacks);
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
					int times = stacks + 1;
					for(int i = 0; i < times; ++i)
					{
						int canFit = fluid.getCapacity() - fluid.getFluidAmount();
						if(canFit >= recipe.outputMb && !items.getStackInSlot(0).isEmpty())
						{
							fluid.fill(new FluidStack(FluidsAE.ALCHEMICAL_ENERGY, recipe.outputMb), true);
							fulfulledTicks.set(0);
							items.decrStackSize(0, 1);
						} else break;
					}
				}
			}

			boolean update;
			IBlockState state = world.getBlockState(pos);
			if(update = (state.getBlock() == BlocksAE.ALCHEMICAL_CONDENSER && state.getValue(IBlockEnableable.ENABLED).booleanValue() != active))
				BlockDeviceHC.updateStateKeepTile(world, pos, state.withProperty(IBlockEnableable.ENABLED, active));
			if(active && (update || atTickRate(30)))
				PacketPlayMachineSound.ensureStarted(world, pos, SoundsAE.MACHINES_ALCHEMICAL_CONDENSER, 1F, 1F);
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
				this.recipe = RegistriesAE.ALCHEMICAL_CONDENSER_RECIPES.getRecipe(new ResourceLocation(recipeId.get()));
		}

		super.notifyOfChange(prop);
	}

	public void setRecipe(RecipeAlchemicalCondenser recipe)
	{
		this.recipe = recipe;
		this.recipeId.set(recipe != null ? recipe.getRecipeName().toString() : "");
	}

	private boolean canCraft(RecipeAlchemicalCondenser recipe)
	{
		return recipe.input.test(items.getStackInSlot(0)) && energy.hasEnergy(computeEnergyRate(recipe.energyRate)) && fluid.getCapacity() - fluid.getFluidAmount() >= recipe.outputMb;
	}

	public int computeEnergyRate(int in)
	{
		int empty = fluid.getCapacity() - fluid.getFluidAmount();
		if(recipe != null) empty /= recipe.outputMb;
		int maxCraftTimes = Math.min(items.getStackInSlot(0).getCount(), empty);

		in *= Math.pow(1.5F, upgrades().getUpgrade(UpgradablePart.SPEED));
		in *= Math.pow(1.092535362, Math.min(upgrades().getUpgrade(UpgradablePart.STACK), maxCraftTimes));
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
		return AlchemicalCondenser.createGUI(this, player);
	}

	@Override
	public Object getServerGuiElement(EntityPlayer player)
	{
		return AlchemicalCondenser.createContainer(this, player);
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
		upgrades.readFromNBT(nbt.getCompoundTag("Upgrades"));
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

	public float getProgress(float partialTicks)
	{
		if(recipe != null && maxTicks.get() > 0)
			return Math.min(1F, (fulfulledTicks.get() + partialTicks) / (float) maxTicks.get());
		return 0F;
	}

	private static final List<UpgradablePart> VALID_PARTS = Arrays.asList(UpgradablePart.SPEED, UpgradablePart.ENERGY, UpgradablePart.ALCHEMICAL_BUFFER, UpgradablePart.FE_BUFFER, UpgradablePart.STACK);
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

	@Override
	public IFluidHandler alchemicalFluidHandler()
	{
		return this;
	}

	@Override
	public boolean connectsTo(EnumFacing towards)
	{
		return towards != face;
	}
}