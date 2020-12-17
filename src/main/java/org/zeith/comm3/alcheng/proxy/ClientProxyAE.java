package org.zeith.comm3.alcheng.proxy;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.BlockFluidBase;
import org.zeith.comm3.alcheng.init.BlocksAE;
import org.zeith.comm3.alcheng.init.InfoAE;

public class ClientProxyAE
		extends CommonProxyAE
{
	@Override
	public void preInit()
	{
		ModelLoader.setCustomStateMapper(BlocksAE.ALCHEMICAL_ENERGY, new StateMap.Builder().ignore(BlockFluidBase.LEVEL).build());
		super.preInit();
	}

	@Override
	public void init()
	{
		super.init();

		mapFluid(BlocksAE.ALCHEMICAL_ENERGY);
	}

	private static void mapFluid(BlockFluidBase fluidBlock)
	{
		ModelResourceLocation modelResourceLocation = new ModelResourceLocation(InfoAE.MOD_ID + ":fluid", fluidBlock.getFluid().getName());
		ModelLoader.setCustomStateMapper(fluidBlock, new StateMapperBase()
		{
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state)
			{
				return modelResourceLocation;
			}
		});
	}
}