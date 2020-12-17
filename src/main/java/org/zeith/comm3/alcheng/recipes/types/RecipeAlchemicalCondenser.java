package org.zeith.comm3.alcheng.recipes.types;

import com.zeitheron.hammercore.api.EnergyUnit;
import com.zeitheron.hammercore.api.crafting.IBaseIngredient;
import com.zeitheron.hammercore.api.crafting.ICraftingResult;
import com.zeitheron.hammercore.api.crafting.INameableRecipe;
import com.zeitheron.hammercore.api.crafting.impl.EnergyIngredient;
import com.zeitheron.hammercore.api.crafting.impl.FluidStackResult;
import com.zeitheron.hammercore.api.crafting.impl.MCIngredient;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.zeith.comm3.alcheng.init.FluidsAE;

public class RecipeAlchemicalCondenser
		implements INameableRecipe
{
	public final FluidStackResult result;
	public final Ingredient input;
	public final int processTicks;
	public final int energyRate;
	public final int outputMb;

	public RecipeAlchemicalCondenser(Ingredient input, int processTicks, int energyRate, int outputMb)
	{
		this.input = input;
		this.processTicks = processTicks;
		this.energyRate = energyRate;
		this.outputMb = outputMb;

		this.result = new FluidStackResult(new FluidStack(FluidsAE.ALCHEMICAL_ENERGY, outputMb));
	}

	@Override
	public NonNullList<IBaseIngredient> getIngredients()
	{
		NonNullList<IBaseIngredient> ingredients = NonNullList.create();
		ingredients.add(new MCIngredient(input));
		ingredients.add(new EnergyIngredient(energyRate * processTicks, EnergyUnit.FE));
		return ingredients;
	}

	@Override
	public ICraftingResult<?> getResult()
	{
		return result;
	}

	private ResourceLocation id;

	@Override
	public ResourceLocation getRecipeName()
	{
		return id;
	}

	public RecipeAlchemicalCondenser setRecipeName(ResourceLocation id)
	{
		if(this.id == null)
			this.id = id;
		return this;
	}
}