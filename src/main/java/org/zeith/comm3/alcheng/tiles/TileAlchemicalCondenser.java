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

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class TileAlchemicalCondenser
		extends TileSyncableTickable
		implements ISidedInventory, ITileDroppable, IMachineTile
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
	public final NetPropertyItemStack onHold;
	public final NetPropertyNumber<Float> speedMult;
	public final NetPropertyNumber<Integer> fluidBuffer;
	public final NetPropertyNumber<Float> progressF;

	private float prevProgressF_cl;

	public TileAlchemicalCondenser()
	{
		this.recipeId = new NetPropertyString(this, "");
		this.fulfulledTicks = new NetPropertyNumber<>(this, 0);
		this.autoExtract = new NetPropertyBool(this, false);
		this.onHold = new NetPropertyItemStack(this, ItemStack.EMPTY);
		this.speedMult = new NetPropertyNumber<>(this, 1F);
		this.fluidBuffer = new NetPropertyNumber<>(this, 16000);
		this.progressF = new NetPropertyNumber<>(this, 0F);

		this.upgrades.validSlots = (slot, stack) ->
		{
			UpgradablePart p = IUpgradeItem.fromStack(stack);
			return p != null && upgrades().hasUpgrade(p) && countUpgrades(p) < p.getMax();
		};
	}

	FluidEnergyAccessPoint FEAP;

	@Override
	public void tickPrevValues()
	{
		prevProgressF_cl = progressF.get();
	}

	@Override
	public void tick()
	{
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

		if(!world.isRemote)
		{
			if(FEAP == null) FEAP = FluidEnergyAccessPoint.create(world, pos);
			if(fluid.getFluidAmount() > 0) fluid.drain(FEAP.emitFluid(fluid.getFluid()), true);

			if(atTickRate(4))
			{
				handleUpgrades();
				sendChangesToNearby();
			}

			if(onHold.get().isEmpty())
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
				float maxTicks = recipe.processTicks;

				if(energy.hasEnergy(recipe.energyRate) && fulfulledTicks.get() < maxTicks)
				{
					energy.extractEnergy(recipe.energyRate, false);
					fulfulledTicks.set(fulfulledTicks.get() + 1);
				}

				progressF.set(fulfulledTicks.get() / maxTicks);

				if(fulfulledTicks.get() >= maxTicks)
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
		nbt.setTag("Upgrades", upgrades.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void readNBT(NBTTagCompound nbt)
	{
		if(world == null)
			fluid.setCapacity(1600000);
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
		if(recipe != null)
			return Math.min(1F, (fulfulledTicks.get() + partialTicks) / (float) recipe.processTicks);
		return 0F;
	}

	private static final List<UpgradablePart> VALID_PARTS = Arrays.asList(UpgradablePart.SPEED, UpgradablePart.ENERGY, UpgradablePart.ALCHEMICAL_BUFFER, UpgradablePart.RF_BUFFER, UpgradablePart.STACK);
	public final IMachineUpgrades MUM = new MachineUpgradeManager(VALID_PARTS::contains);

	@Override
	public void handleUpgrades()
	{
		MUM.resetUpgrades();

		for(UpgradablePart part : VALID_PARTS)
		{
			MUM.upgradePart(part, part.computeUpgradability(this));
		}

		float speed = MUM.getUpgrade(UpgradablePart.SPEED);
		if(speed > 12F) speedMult.set(-1F);
		else speedMult.set(1F + 1.25F * countUpgrades(UpgradablePart.SPEED));

		int buf = 16000 + Math.round(MUM.getUpgrade(UpgradablePart.ALCHEMICAL_BUFFER) * 8000F);
		fluidBuffer.set(buf);

		MUM.getUpgrade(UpgradablePart.ENERGY);
	}

	@Override
	public IMachineUpgrades upgrades()
	{
		return MUM;
	}

	@Override
	public IInventory upgradeInventory()
	{
		return upgrades;
	}
}