package org.zeith.comm3.alcheng.compat.jei.modules;

import com.zeitheron.hammercore.client.gui.GuiWidgets;
import com.zeitheron.hammercore.client.utils.RenderUtil;
import com.zeitheron.hammercore.client.utils.UtilsFX;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.zeith.comm3.alcheng.compat.jei.CompatJEI;
import org.zeith.comm3.alcheng.init.BlocksAE;
import org.zeith.comm3.alcheng.init.FluidsAE;
import org.zeith.comm3.alcheng.init.InfoAE;
import org.zeith.comm3.alcheng.recipes.types.RecipeAlchemicalCrafter;
import org.zeith.comm3.alcheng.utils.FluidRenderUtil;
import org.zeith.comm3.alcheng.utils.TickTimer;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AlchemicalCrafter
{
	static final ResourceLocation TEX = new ResourceLocation(InfoAE.MOD_ID, "textures/gui/alchemical_crafter.png");
	public static final String UID = InfoAE.MOD_ID + ":alchemical_crafter";

	public static class Wrapper
			implements IRecipeWrapper
	{
		public final RecipeAlchemicalCrafter recipe;

		public final TickTimer timer;

		public Wrapper(RecipeAlchemicalCrafter recipe)
		{
			this.recipe = recipe;
			this.timer = new TickTimer(recipe.processTicks, recipe.processTicks, false);
		}

		@Override
		public void getIngredients(IIngredients ingredients)
		{
			ingredients.setInputLists(VanillaTypes.ITEM, recipe.inputs.stream().map(i -> Arrays.asList(i.getMatchingStacks())).collect(Collectors.toList()));
			ingredients.setInput(VanillaTypes.FLUID, new FluidStack(FluidsAE.ALCHEMICAL_ENERGY, recipe.inputMb));
			ingredients.setOutput(VanillaTypes.ITEM, recipe.getResult().getBaseOutput());
		}

		@Override
		public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
		{
			UtilsFX.bindTexture(TEX);
			float craft = timer.getProgress() * 22F;
			RenderUtil.drawTexturedModalRect(progress.x + craft, progress.y, 176 + craft, 0, 22 - craft, 15);
		}

		static final Rectangle energy = new Rectangle(1, 1, 8, 52);
		static final Rectangle progress = new Rectangle(92, 19, 22, 15);

		@Override
		public List<String> getTooltipStrings(int mouseX, int mouseY)
		{
			if(energy.contains(mouseX, mouseY))
				return Arrays.asList(String.format("FE: %,d", recipe.energyRate * recipe.processTicks), String.format("FE/tick: %,d", recipe.energyRate));
			if(progress.contains(mouseX, mouseY))
			{
				if(GuiScreen.isShiftKeyDown())
					return Collections.singletonList(String.format("Time: %,d ticks", recipe.processTicks));
				return Collections.singletonList(String.format("Time: %.01f sec", recipe.processTicks / 20F));
			}
			return Collections.emptyList();
		}
	}

	public static class Category
			implements IRecipeCategory<Wrapper>
	{
		final Background bg = new Background();

		@Override
		public String getUid()
		{
			return UID;
		}

		@Override
		public String getTitle()
		{
			return BlocksAE.ALCHEMICAL_CRAFTER.getLocalizedName();
		}

		@Override
		public String getModName()
		{
			return InfoAE.MOD_NAME;
		}

		@Override
		public IDrawable getBackground()
		{
			return bg;
		}

		@Override
		public void setRecipe(IRecipeLayout recipeLayout, Wrapper recipeWrapper, IIngredients ingredients)
		{
			IGuiItemStackGroup items = recipeLayout.getItemStacks();

			if(recipeWrapper.recipe instanceof RecipeAlchemicalCrafter.Shaped)
			{
				RecipeAlchemicalCrafter.Shaped shaped = (RecipeAlchemicalCrafter.Shaped) recipeWrapper.recipe;

				for(int i = 0; i < shaped.recipeWidth; ++i)
					for(int j = 0; j < shaped.recipeHeight; ++j)
					{
						int idx = i + j * shaped.recipeWidth;
						items.init(idx, true, CompatJEI.stackRenderer, 32 + i * 18, 1 + j * 18, 16, 16, 0, 0);
						items.set(idx, Arrays.asList(shaped.inputs.get(idx).getMatchingStacks()));
					}
			} else if(recipeWrapper.recipe instanceof RecipeAlchemicalCrafter.Shapeless)
			{
				RecipeAlchemicalCrafter.Shapeless shapeless = (RecipeAlchemicalCrafter.Shapeless) recipeWrapper.recipe;

				for(int i = 0; i < Math.min(shapeless.inputs.size(), 9); ++i)
				{
					items.init(i, true, CompatJEI.stackRenderer, 32 + (i % 3) * 18, 1 + (i / 3) * 18, 16, 16, 0, 0);
					items.set(i, Arrays.asList(shapeless.inputs.get(i).getMatchingStacks()));
				}
			}

			items.init(10, false, 125, 18);
			items.set(10, recipeWrapper.recipe.getResult().getBaseOutput());

			int mb = recipeWrapper.recipe.inputMb;

			IGuiFluidStackGroup fluids = recipeLayout.getFluidStacks();
			fluids.init(11, true, 13, 1, 16, 52, mb, false, new AlchemicalCondenser.FluidOverlay());
			fluids.set(11, new FluidStack(FluidsAE.ALCHEMICAL_ENERGY, mb));
		}
	}

	public static class Background
			implements IDrawable
	{
		@Override
		public int getWidth()
		{
			return 147;
		}

		@Override
		public int getHeight()
		{
			return 54;
		}

		@Override
		public void draw(Minecraft minecraft, int guiLeft, int guiTop)
		{
			GlStateManager.disableBlend();
			FluidRenderUtil.drawFluid(minecraft, new FluidStack(FluidsAE.ALCHEMICAL_ENERGY, 100), guiLeft + 92, guiTop + 20, 22, 15);
			GlStateManager.enableBlend();

			UtilsFX.bindTexture(TEX);
			RenderUtil.drawTexturedModalRect(guiLeft, guiTop, 0, 166, 147, 54);
			GuiWidgets.drawEnergy(guiLeft + 1, guiTop + 1, 8, 52, GuiWidgets.EnumPowerAnimation.UP);
		}
	}
}