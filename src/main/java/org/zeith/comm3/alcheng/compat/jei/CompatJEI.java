package org.zeith.comm3.alcheng.compat.jei;


import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;
import org.zeith.comm3.alcheng.compat.jei.modules.AlchemicalCondenser;
import org.zeith.comm3.alcheng.compat.jei.modules.AlchemicalCrafter;
import org.zeith.comm3.alcheng.init.BlocksAE;
import org.zeith.comm3.alcheng.init.RegistriesAE;
import org.zeith.comm3.alcheng.recipes.types.RecipeAlchemicalCondenser;
import org.zeith.comm3.alcheng.recipes.types.RecipeAlchemicalCrafter;

@JEIPlugin
public class CompatJEI
		implements IModPlugin
{
	public static IIngredientRenderer<ItemStack> stackRenderer;

	{
		try
		{
			stackRenderer = (IIngredientRenderer) Class.forName("mezz.jei.plugins.vanilla.ingredients.item.ItemStackRenderer").newInstance();
		} catch(InstantiationException | IllegalAccessException | ClassNotFoundException e)
		{
			try
			{
				stackRenderer = (IIngredientRenderer) Class.forName("mezz.jei.plugins.vanilla.ingredients.ItemStackRenderer").newInstance();
			} catch(InstantiationException | IllegalAccessException | ClassNotFoundException e2)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void register(IModRegistry registry)
	{
		registry.handleRecipes(RecipeAlchemicalCondenser.class, AlchemicalCondenser.Wrapper::new, AlchemicalCondenser.UID);
		registry.handleRecipes(RecipeAlchemicalCrafter.class, AlchemicalCrafter.Wrapper::new, AlchemicalCrafter.UID);

		registry.addRecipeCatalyst(new ItemStack(BlocksAE.ALCHEMICAL_CONDENSER), AlchemicalCondenser.UID);
		registry.addRecipeCatalyst(new ItemStack(BlocksAE.ALCHEMICAL_CRAFTER), AlchemicalCrafter.UID);

		registry.addRecipes(RegistriesAE.ALCHEMICAL_CONDENSER_RECIPES.getRecipes(), AlchemicalCondenser.UID);
		registry.addRecipes(RegistriesAE.ALCHEMICAL_CRAFTER_RECIPES.getRecipes(), AlchemicalCrafter.UID);

		registry.addRecipeClickArea(org.zeith.comm3.alcheng.inventory.AlchemicalCondenser.Gui.class, 90, 35, 22, 15, AlchemicalCondenser.UID);
		registry.addRecipeClickArea(org.zeith.comm3.alcheng.inventory.AlchemicalCrafter.Gui.class, 99, 35, 22, 15, AlchemicalCrafter.UID);
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry)
	{
		registry.addRecipeCategories(new AlchemicalCondenser.Category());
		registry.addRecipeCategories(new AlchemicalCrafter.Category());
	}
}