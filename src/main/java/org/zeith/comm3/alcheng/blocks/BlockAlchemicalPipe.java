package org.zeith.comm3.alcheng.blocks;

import com.zeitheron.hammercore.api.ITileBlock;
import com.zeitheron.hammercore.api.inconnect.InConnectAPI;
import com.zeitheron.hammercore.api.mhb.BlockTraceable;
import com.zeitheron.hammercore.api.mhb.ICubeManager;
import com.zeitheron.hammercore.utils.base.Cast;
import com.zeitheron.hammercore.utils.math.vec.Cuboid6;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.zeith.comm3.alcheng.tiles.TileAlchemicalPipe;

import javax.annotation.Nullable;

public class BlockAlchemicalPipe
		extends BlockTraceable
		implements ICubeManager, ITileBlock<TileAlchemicalPipe>
{
	public final int capacity;

	public BlockAlchemicalPipe(int capacity, String tier)
	{
		super(Material.IRON);
		this.capacity = capacity;
		setSoundType(SoundType.METAL);
		setHardness(0.5F);
		setHarvestLevel("pickaxe", 1);
		setTranslationKey("alchemical_pipes/" + tier);
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
	{
		TileAlchemicalPipe pipe = Cast.cast(worldIn.getTileEntity(pos), TileAlchemicalPipe.class);
		if(pipe != null) pipe.cubesDirty = true;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean isFullBlock(IBlockState state)
	{
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state)
	{
		return false;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face)
	{
		return BlockFaceShape.UNDEFINED;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		return new TileAlchemicalPipe(capacity);
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		return InConnectAPI.makeExtendedPositionedState(world, pos, state);
	}

	@Override
	public Cuboid6[] getCuboids(World world, BlockPos pos, IBlockState state)
	{
		TileAlchemicalPipe pipe = Cast.cast(world.getTileEntity(pos), TileAlchemicalPipe.class);
		if(pipe != null)
			return pipe.connections;
		return new Cuboid6[0];
	}

	@Override
	public Class<TileAlchemicalPipe> getTileClass()
	{
		return TileAlchemicalPipe.class;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
	{
		return true;
	}
}