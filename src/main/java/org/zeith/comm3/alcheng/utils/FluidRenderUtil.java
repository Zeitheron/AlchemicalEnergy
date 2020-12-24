package org.zeith.comm3.alcheng.utils;

import com.zeitheron.hammercore.client.utils.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class FluidRenderUtil
{
	public static void drawFluid(Minecraft minecraft, FluidStack fluidStack, float xPosition, float yPosition, float width, float height)
	{
		if(fluidStack == null)
			return;
		Fluid fluid = fluidStack.getFluid();
		if(fluid == null)
			return;
		TextureAtlasSprite tas = getStillFluidSprite(minecraft, fluid);
		int fluidColor = fluid.getColor(fluidStack);

		minecraft.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		setGLColorFromInt(fluidColor);

		int start = 0;

		float BarHeight = height;

		while(true)
		{
			float y = 0;

			if(BarHeight > 16)
			{
				y = 16;
				BarHeight -= 16;
			} else
			{
				y = BarHeight;
				BarHeight = 0;
			}

			RenderUtil.drawTexturedModalRect(xPosition, yPosition + height - y - start, tas, width, y);
			start = start + 16;

			if(y == 0 || BarHeight == 0)
				break;
		}
	}

	public static void setGLColorFromInt(int color)
	{
		float red = (color >> 16 & 0xFF) / 255.0F;
		float green = (color >> 8 & 0xFF) / 255.0F;
		float blue = (color & 0xFF) / 255.0F;

		GlStateManager.color(red, green, blue, 1.0F);
	}

	public static TextureAtlasSprite getStillFluidSprite(Minecraft minecraft, Fluid fluid)
	{
		TextureMap textureMapBlocks = minecraft.getTextureMapBlocks();
		ResourceLocation fluidStill = fluid.getStill();
		TextureAtlasSprite fluidStillSprite = null;
		if(fluidStill != null)
			fluidStillSprite = textureMapBlocks.getTextureExtry(fluidStill.toString());
		if(fluidStillSprite == null)
			fluidStillSprite = textureMapBlocks.getMissingSprite();
		return fluidStillSprite;
	}
}