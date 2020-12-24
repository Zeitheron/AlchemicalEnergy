package org.zeith.comm3.alcheng.compat.jei.modules;

import com.zeitheron.hammercore.client.gui.GuiWidgets;
import com.zeitheron.hammercore.client.utils.RenderUtil;
import com.zeitheron.hammercore.client.utils.UtilsFX;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;
import org.zeith.comm3.alcheng.compat.jei.CompatJEI;
import org.zeith.comm3.alcheng.init.BlocksAE;
import org.zeith.comm3.alcheng.init.FluidsAE;
import org.zeith.comm3.alcheng.init.InfoAE;
import org.zeith.comm3.alcheng.recipes.types.RecipeAlchemicalCondenser;
import org.zeith.comm3.alcheng.utils.FluidRenderUtil;
import org.zeith.comm3.alcheng.utils.TickTimer;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AlchemicalCondenser
{
	static final ResourceLocation TEX = new ResourceLocation(InfoAE.MOD_ID, "textures/gui/alchemical_condenser.png");
	public static final String UID = InfoAE.MOD_ID + ":alchemical";

	public static class Wrapper
			implements IRecipeWrapper
	{
		public final RecipeAlchemicalCondenser recipe;

		public final TickTimer timer;

		public Wrapper(RecipeAlchemicalCondenser recipe)
		{
			this.recipe = recipe;
			this.timer = new TickTimer(recipe.processTicks, recipe.processTicks, false);
		}

		@Override
		public void getIngredients(IIngredients ingredients)
		{
			ingredients.setInputs(ItemStack.class, Arrays.asList(recipe.input.getMatchingStacks()));
			ingredients.setOutput(FluidStack.class, new FluidStack(FluidsAE.ALCHEMICAL_ENERGY, recipe.outputMb));
		}

		@Override
		public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
		{
			UtilsFX.bindTexture(TEX);
			float craft = timer.getProgress() * 22F;
			RenderUtil.drawTexturedModalRect(50 + craft, 25, 176 + craft, 0, 22 - craft, 15);
		}

		static final Rectangle energy = new Rectangle(1, 1, 8, 64);
		static final Rectangle progress = new Rectangle(50, 25, 22, 15);

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
			return BlocksAE.ALCHEMICAL_CONDENSER.getLocalizedName();
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
			items.init(0, true, CompatJEI.stackRenderer, 22, 25, 16, 16, 0, 0);
			items.set(0, ingredients.getInputs(ItemStack.class).get(0));

			int mb = recipeWrapper.recipe.outputMb;

			IGuiFluidStackGroup fluids = recipeLayout.getFluidStacks();
			fluids.init(1, false, 80, 1, 16, 64, mb, false, new FluidOverlay());
			fluids.set(1, new FluidStack(FluidsAE.ALCHEMICAL_ENERGY, mb));
		}
	}

	public static class Background
			implements IDrawable
	{
		@Override
		public int getWidth()
		{
			return 97;
		}

		@Override
		public int getHeight()
		{
			return 66;
		}

		@Override
		public void draw(Minecraft minecraft, int guiLeft, int guiTop)
		{
			GlStateManager.disableBlend();
			FluidRenderUtil.drawFluid(minecraft, new FluidStack(FluidsAE.ALCHEMICAL_ENERGY, 100), guiLeft + 50, guiTop + 25, 22, 15);
			GlStateManager.enableBlend();

			UtilsFX.bindTexture(TEX);
			RenderUtil.drawTexturedModalRect(guiLeft, guiTop, 0, 166, 97, 66);
			GuiWidgets.drawEnergy(guiLeft + 1, guiTop + 1, 8, 64, GuiWidgets.EnumPowerAnimation.UP);
		}
	}

	public static class FluidOverlay
			implements IDrawable
	{

		@Override
		public int getWidth()
		{
			return 16;
		}

		@Override
		public int getHeight()
		{
			return 64;
		}

		@Override
		public void draw(Minecraft minecraft, int x, int y)
		{
			int totalLines = Math.round(getHeight() / 5F);
			int half = totalLines / 2;
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			for(int l = 0; l < totalLines; ++l)
				RenderUtil.drawColoredModalRect(x, y + l * 5, 16F / (l == half ? 1 : 2), 1, 0xFF7F0000);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
	}
}