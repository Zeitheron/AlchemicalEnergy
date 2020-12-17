package org.zeith.comm3.alcheng.blocks;

import com.zeitheron.hammercore.api.INoItemBlock;
import com.zeitheron.hammercore.utils.ListUtils;
import com.zeitheron.hammercore.utils.java.itf.TriConsumer;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import org.apache.commons.lang3.ArrayUtils;
import org.zeith.comm3.alcheng.init.FluidsAE;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class BlockAlchemicalEnergy
		extends BlockFluidClassic
		implements INoItemBlock
{
	public static List<Tuple<Predicate<IBlockState>, TriConsumer<World, BlockPos, Random>>> TRANSMUTATIONS = new ArrayList<>();
	public static List<Tuple<Predicate<IBlockState>, TriConsumer<World, BlockPos, Random>>> INSTANT_TRANSMUTATIONS = new ArrayList<>();

	public BlockAlchemicalEnergy()
	{
		super(FluidsAE.ALCHEMICAL_ENERGY, Material.LAVA);
		setTranslationKey("alchemical_energy");
		setTickRandomly(true);
	}

	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items)
	{
	}

	public static EnumFacing[] ITER = ArrayUtils.add(EnumFacing.HORIZONTALS, EnumFacing.DOWN);

	@Override
	public void neighborChanged(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Block neighborBlock, @Nonnull BlockPos neighbourPos)
	{
		super.neighborChanged(state, world, pos, neighborBlock, neighbourPos);

		for(EnumFacing face : ListUtils.randomizeList(new ArrayList<>(Arrays.asList(ITER)), world.rand))
		{
			BlockPos target = pos.offset(face);
			IBlockState tstate = world.getBlockState(target);
			final Optional<TriConsumer<World, BlockPos, Random>> consumer = INSTANT_TRANSMUTATIONS.stream()
					.filter(t -> t.getFirst().test(tstate))
					.findFirst()
					.map(Tuple::getSecond);
			consumer.ifPresent(func -> func.accept(world, target, world.rand));
			if(consumer.isPresent()) break;
		}
	}

	@Override
	public void onBlockAdded(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state)
	{
		super.onBlockAdded(world, pos, state);

		for(EnumFacing face : ListUtils.randomizeList(new ArrayList<>(Arrays.asList(ITER)), world.rand))
		{
			BlockPos target = pos.offset(face);
			IBlockState tstate = world.getBlockState(target);
			final Optional<TriConsumer<World, BlockPos, Random>> consumer = INSTANT_TRANSMUTATIONS.stream()
					.filter(t -> t.getFirst().test(tstate))
					.findFirst()
					.map(Tuple::getSecond);
			consumer.ifPresent(func -> func.accept(world, target, world.rand));
			if(consumer.isPresent()) break;
		}
	}

	@Override
	public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random)
	{
		if(random.nextInt(5) == 0)
			for(EnumFacing face : ListUtils.randomizeList(new ArrayList<>(Arrays.asList(ITER)), random))
			{
				BlockPos target = pos.offset(face);
				IBlockState tstate = worldIn.getBlockState(target);
				final Optional<TriConsumer<World, BlockPos, Random>> consumer = Stream.concat(TRANSMUTATIONS.stream(), INSTANT_TRANSMUTATIONS.stream())
						.filter(t -> t.getFirst().test(tstate))
						.findFirst()
						.map(Tuple::getSecond);
				consumer.ifPresent(func -> func.accept(worldIn, target, worldIn.rand));
				if(consumer.isPresent()) break;
			}

		super.randomTick(worldIn, pos, state, random);
	}

	public static Predicate<IBlockState> blockSource(Block block)
	{
		return state -> state.getBlock() == block;
	}

	public static Predicate<IBlockState> blocksSource(Block... blocks)
	{
		List<Block> allBlocks = Arrays.asList(blocks);
		return state -> allBlocks.contains(state.getBlock());
	}

	public static void transmuteBlock(Predicate<IBlockState> source, IBlockState target, boolean instant)
	{
		transmuteBlock(source, state -> target, instant);
	}

	public static void transmuteBlock(Predicate<IBlockState> source, Function<IBlockState, IBlockState> converter, boolean instant)
	{
		transmuteBlock(source, (w, p, r) ->
		{
			IBlockState state = converter.apply(w.getBlockState(p));
			if(state != null) w.setBlockState(p, state);
		}, instant);
	}

	public static void transmuteBlock(Predicate<IBlockState> source, TriConsumer<World, BlockPos, Random> converter, boolean instant)
	{
		(instant ? INSTANT_TRANSMUTATIONS : TRANSMUTATIONS).add(new Tuple<>(source, converter));
	}
}