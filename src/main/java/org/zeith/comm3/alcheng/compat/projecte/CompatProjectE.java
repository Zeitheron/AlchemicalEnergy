package org.zeith.comm3.alcheng.compat.projecte;

import com.zeitheron.hammercore.mod.ModuleLoader;
import net.minecraft.block.Block;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.zeith.comm3.alcheng.ConfigAE;
import org.zeith.comm3.alcheng.blocks.BlockAlchemicalEnergy;
import org.zeith.comm3.alcheng.compat.BaseCompatAE;

@ModuleLoader(requiredModid = "projecte")
public class CompatProjectE
		extends BaseCompatAE
{
	@Override
	public void init()
	{
		Block tt = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("projecte", "transmutation_table"));

		if(ConfigAE.projectEFun)
			BlockAlchemicalEnergy.transmuteBlock(BlockAlchemicalEnergy.blockSource(tt), (w, p, r) ->
			{
				w.destroyBlock(p, false);
				int amt = 1 + r.nextInt(10);
				for(int i = 0; i < amt; ++i)
				{
					EntityCreeper creeper = new EntityCreeper(w);
					creeper.setPositionAndUpdate(p.getX() + 0.5F, p.getY() + 0.001F, p.getZ() + 0.5F);
					w.spawnEntity(creeper);
				}
			}, true);
	}
}