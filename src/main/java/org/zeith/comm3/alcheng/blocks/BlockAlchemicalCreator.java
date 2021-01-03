package org.zeith.comm3.alcheng.blocks;

import com.zeitheron.hammercore.internal.GuiManager;
import com.zeitheron.hammercore.internal.blocks.base.BlockDeviceHC;
import com.zeitheron.hammercore.tile.TileSyncable;
import com.zeitheron.hammercore.utils.base.Cast;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.zeith.comm3.alcheng.tiles.TileAlchemicalCreator;

public class BlockAlchemicalCreator
		extends BlockDeviceHC<TileAlchemicalCreator>
{
	public BlockAlchemicalCreator()
	{
		super(Material.IRON, TileAlchemicalCreator.class, "alchemical_creator");
		setSoundType(SoundType.METAL);
		setHarvestLevel("pickaxe", 1);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if(!worldIn.isRemote)
		{
			ItemStack stack = playerIn.getHeldItem(hand);
			if(playerIn.capabilities.isCreativeMode) stack = stack.copy();
			IFluidHandlerItem fh = FluidUtil.getFluidHandler(stack);
			if(fh != null)
			{
				TileAlchemicalCreator alch = Cast.cast(worldIn.getTileEntity(pos), TileAlchemicalCreator.class);
				if(alch != null && alch.fluid.getFluidAmount() > 0 && fh.fill(alch.fluid.getFluid(), false) > 0)
				{
					alch.fluid.drain(fh.fill(alch.fluid.getFluid(), true), true);
					if(!playerIn.capabilities.isCreativeMode)
						playerIn.setHeldItem(hand, fh.getContainer());
					return true;
				}
			}
			GuiManager.openGui(playerIn, Cast.cast(worldIn.getTileEntity(pos), TileSyncable.class));
		}
		return true;
	}
}