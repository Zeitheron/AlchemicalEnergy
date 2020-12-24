package org.zeith.comm3.alcheng.client.tesr;

import com.zeitheron.hammercore.client.render.tesr.TESR;
import com.zeitheron.hammercore.client.utils.UtilsFX;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import org.zeith.comm3.alcheng.client.model.ModelBoxF;
import org.zeith.comm3.alcheng.init.FluidsAE;
import org.zeith.comm3.alcheng.tiles.TileAlchemicalPipe;
import org.zeith.comm3.alcheng.utils.FluidRenderUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

//@AtTESR(TileAlchemicalPipe.class)
public class TESRAlchemicalPipe
		extends TESR<TileAlchemicalPipe>
{
	@Override
	public void renderTileEntityAt(@Nonnull TileAlchemicalPipe te, double x, double y, double z, float partialTicks, @Nullable ResourceLocation destroyStage, float alpha)
	{
		float fill = te.fluid.getCapacity();
		if(fill > 0F) fill = te.fluid.getFluidAmount() / fill;

		if(fill >= 0F)
		{
			TextureAtlasSprite tas = FluidRenderUtil.getStillFluidSprite(Minecraft.getMinecraft(), FluidsAE.ALCHEMICAL_ENERGY);

			UtilsFX.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
			GlStateManager.scale(0.99F * fill, 0.99F * fill, 0.99F * fill);
			GlStateManager.translate(-0.5, -0.5, -0.5);

			new ModelBoxF(tas, 5 / 16F, 5 / 16F, 5 / 16F, 6 / 16F, 6 / 16F, 6 / 16F)
					.render(Tessellator.getInstance().getBuffer(), 1F);
			GlStateManager.popMatrix();
		}
	}
}