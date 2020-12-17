package org.zeith.comm3.alcheng.compat.crafttweaker.core;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InputHelper
{
	public static Ingredient toIngredient(IIngredient iIng)
	{
		if(iIng == null)
			return Ingredient.EMPTY;

		Object internal = iIng.getInternal();

		if(iIng instanceof IItemStack)
			return Ingredient.fromStacks(toStack((IItemStack) iIng));

		if(!(internal instanceof Ingredient))
		{
			return Ingredient.EMPTY;
		}

		return (Ingredient) internal;
	}

	public static ItemStack toStack(IItemStack iStack)
	{
		if(iStack == null)
			return ItemStack.EMPTY;

		Object internal = iStack.getInternal();

		if(!(internal instanceof ItemStack))
		{
			return ItemStack.EMPTY;
		}

		return (ItemStack) internal;
	}

	public static Tuple<IBlockState, Block> parseBlock(String input)
	{
		String[] metas = input.split("[@]", 2);
		Block iblock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(metas[0]));
		return metas.length == 2 ? new Tuple<>(iblock.getStateFromMeta(Integer.parseInt(metas[1])), null) : new Tuple<>(null, iblock);
	}

	public static Predicate<IBlockState> parseBlockMatcher(String input)
	{
		if(input.startsWith("ore:"))
		{
			NonNullList<ItemStack> stacks = OreDictionary.getOres(input.substring(4));
			List<Block> match = stacks.stream().map(s -> Block.getBlockFromItem(s.getItem())).filter(b -> b != null).collect(Collectors.toList());
			return state -> match.contains(state.getBlock());
		}

		String[] metas = input.split("[@]", 2);
		Block iblock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(metas[0]));
		IBlockState state = metas.length == 2 ? iblock.getStateFromMeta(Integer.parseInt(metas[1])) : null;
		return state != null ? ts -> ts.equals(state) : ts -> ts.getBlock() == iblock;
	}

	public static IBlockState parseBlock(Tuple<IBlockState, Block> in)
	{
		IBlockState is = in.getFirst();
		Block ib = in.getSecond();
		if(is != null) return is;
		if(ib != null) return ib.getDefaultState();
		return null;
	}
}