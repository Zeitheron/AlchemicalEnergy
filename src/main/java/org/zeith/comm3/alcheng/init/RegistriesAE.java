package org.zeith.comm3.alcheng.init;

import com.zeitheron.hammercore.api.crafting.NamespacedRecipeRegistry;
import net.minecraft.util.ResourceLocation;
import org.zeith.comm3.alcheng.recipes.types.RecipeAlchemicalCondenser;

public class RegistriesAE
{
	public static NamespacedRecipeRegistry<RecipeAlchemicalCondenser> ALCHEMICAL_CONDENSER_RECIPES = new NamespacedRecipeRegistry<>(RecipeAlchemicalCondenser.class, new ResourceLocation(InfoAE.MOD_ID, "alchemical_condenser"));

	public static void init()
	{
	}
}