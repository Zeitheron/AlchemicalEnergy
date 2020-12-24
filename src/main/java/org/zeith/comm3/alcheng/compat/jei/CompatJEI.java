package org.zeith.comm3.alcheng.compat.jei;


import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;
import org.zeith.comm3.alcheng.compat.jei.modules.AlchemicalCondenser;
import org.zeith.comm3.alcheng.init.BlocksAE;
import org.zeith.comm3.alcheng.init.RegistriesAE;
import org.zeith.comm3.alcheng.recipes.types.RecipeAlchemicalCondenser;

@JEIPlugin
public class CompatJEI
		implements IModPlugin
{
	public static IIngredientRenderer<ItemStack> stackRenderer;

	{
		try
		{
			stackRenderer = (IIngredientRenderer) Class.forName("mezz.jei.plugins.vanilla.ingredients.ItemStackRenderer").newInstance();
		} catch(InstantiationException | IllegalAccessException | ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void register(IModRegistry registry)
	{
		registry.handleRecipes(RecipeAlchemicalCondenser.class, AlchemicalCondenser.Wrapper::new, AlchemicalCondenser.UID);
		registry.addRecipeCatalyst(new ItemStack(BlocksAE.ALCHEMICAL_CONDENSER), AlchemicalCondenser.UID);
		registry.addRecipes(RegistriesAE.ALCHEMICAL_CONDENSER_RECIPES.getRecipes(), AlchemicalCondenser.UID);

		registry.addRecipeClickArea(org.zeith.comm3.alcheng.inventory.AlchemicalCondenser.Gui.class, 90, 35, 22, 15, AlchemicalCondenser.UID);
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry)
	{
		registry.addRecipeCategories(new AlchemicalCondenser.Category());
	}
}