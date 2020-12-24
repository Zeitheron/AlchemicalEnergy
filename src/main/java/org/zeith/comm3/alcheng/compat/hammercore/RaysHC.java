package org.zeith.comm3.alcheng.compat.hammercore;

import com.zeitheron.hammercore.api.mhb.IRayCubeRegistry;
import com.zeitheron.hammercore.api.mhb.IRayRegistry;
import com.zeitheron.hammercore.api.mhb.RaytracePlugin;
import org.zeith.comm3.alcheng.init.BlocksAE;

@RaytracePlugin
public class RaysHC
		implements IRayRegistry
{
	@Override
	public void registerCubes(IRayCubeRegistry cube)
	{
		cube.bindBlockCubeManager(BlocksAE.ALCHEMICAL_PIPES_BASIC, BlocksAE.ALCHEMICAL_PIPES_BASIC);
		cube.bindBlockCubeManager(BlocksAE.ALCHEMICAL_PIPES_ADVANCED, BlocksAE.ALCHEMICAL_PIPES_ADVANCED);
		cube.bindBlockCubeManager(BlocksAE.ALCHEMICAL_PIPES_EXPERT, BlocksAE.ALCHEMICAL_PIPES_EXPERT);
		cube.bindBlockCubeManager(BlocksAE.ALCHEMICAL_PIPES_CREATIVE, BlocksAE.ALCHEMICAL_PIPES_CREATIVE);
	}
}