package org.zeith.comm3.alcheng.proxy;

import com.zeitheron.hammercore.proxy.RenderProxy_Client;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.zeith.comm3.alcheng.client.model.BakedPipeModel;
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

		RenderProxy_Client.bakedModelStore.putConstant(BlocksAE.ALCHEMICAL_PIPES_BASIC.getDefaultState(), new BakedPipeModel(new ResourceLocation(InfoAE.MOD_ID, "blocks/alchemical_pipes/basic")));
		RenderProxy_Client.bakedModelStore.putConstant(BlocksAE.ALCHEMICAL_PIPES_ADVANCED.getDefaultState(), new BakedPipeModel(new ResourceLocation(InfoAE.MOD_ID, "blocks/alchemical_pipes/advanced")));
		RenderProxy_Client.bakedModelStore.putConstant(BlocksAE.ALCHEMICAL_PIPES_EXPERT.getDefaultState(), new BakedPipeModel(new ResourceLocation(InfoAE.MOD_ID, "blocks/alchemical_pipes/expert")));
		RenderProxy_Client.bakedModelStore.putConstant(BlocksAE.ALCHEMICAL_PIPES_CREATIVE.getDefaultState(), new BakedPipeModel(new ResourceLocation(InfoAE.MOD_ID, "blocks/alchemical_pipes/creative")));
	}

	@SubscribeEvent
	public void registerSprites(TextureStitchEvent.Pre e)
	{
		e.getMap().registerSprite(new ResourceLocation(InfoAE.MOD_ID, "blocks/alchemical_pipes/basic"));
		e.getMap().registerSprite(new ResourceLocation(InfoAE.MOD_ID, "blocks/alchemical_pipes/advanced"));
		e.getMap().registerSprite(new ResourceLocation(InfoAE.MOD_ID, "blocks/alchemical_pipes/expert"));
		e.getMap().registerSprite(new ResourceLocation(InfoAE.MOD_ID, "blocks/alchemical_pipes/creative"));
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