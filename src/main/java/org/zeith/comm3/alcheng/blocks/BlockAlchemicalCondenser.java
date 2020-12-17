package org.zeith.comm3.alcheng.blocks;

import com.zeitheron.hammercore.internal.GuiManager;
import com.zeitheron.hammercore.internal.blocks.base.BlockDeviceHC;
import com.zeitheron.hammercore.internal.blocks.base.IBlockEnableable;
import com.zeitheron.hammercore.internal.blocks.base.IBlockHorizontal;
import com.zeitheron.hammercore.tile.TileSyncable;
import com.zeitheron.hammercore.utils.base.Cast;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.zeith.comm3.alcheng.tiles.TileAlchemicalCondenser;

public class BlockAlchemicalCondenser
		extends BlockDeviceHC<TileAlchemicalCondenser>
		implements IBlockHorizontal, IBlockEnableable
{
	public BlockAlchemicalCondenser()
	{
		super(Material.IRON, TileAlchemicalCondenser.class, "alchemical_condenser");
		reactsToRedstone = false;
	}

	@Override
	public boolean enableableDefault()
	{
		return false;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		GuiManager.openGui(playerIn, Cast.cast(worldIn.getTileEntity(pos), TileSyncable.class));
		return true;
	}
}