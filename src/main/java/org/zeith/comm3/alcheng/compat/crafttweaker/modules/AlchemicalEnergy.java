package org.zeith.comm3.alcheng.compat.crafttweaker.modules;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import org.zeith.comm3.alcheng.blocks.BlockAlchemicalEnergy;
import org.zeith.comm3.alcheng.compat.crafttweaker.CompatCraftTweaker;
import org.zeith.comm3.alcheng.compat.crafttweaker.core.BaseAction;
import org.zeith.comm3.alcheng.compat.crafttweaker.core.InputHelper;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.function.Predicate;

@ZenClass("mods.alcheng.AlchemicalEnergy")
@ZenRegister
public class AlchemicalEnergy
{
	@ZenMethod
	public static void add(String input, String output, boolean instant)
	{
		CompatCraftTweaker
				.addLateAction(new Add(InputHelper.parseBlockMatcher(input), InputHelper.parseBlock(output), instant));
	}

	@ZenMethod
	public static void add(String input, IItemStack output, boolean instant)
	{
		CompatCraftTweaker
				.addLateAction(new Add2(InputHelper.parseBlockMatcher(input), InputHelper.toStack(output), instant));
	}

	@ZenMethod
	public static void remove(String input, boolean instant)
	{
		CompatCraftTweaker
				.addLateAction(new Remove(InputHelper.parseBlock(InputHelper.parseBlock(input)), instant));
	}

	private static final class Add
			extends BaseAction
	{
		private Add(Predicate<IBlockState> input, Tuple<IBlockState, Block> output, boolean instant)
		{
			super("AlchemicalEnergy", () -> BlockAlchemicalEnergy.transmuteBlock(input, InputHelper.parseBlock(output), instant));
		}
	}

	private static final class Add2
			extends BaseAction
	{
		private Add2(Predicate<IBlockState> input, ItemStack output, boolean instant)
		{
			super("AlchemicalEnergy", () -> BlockAlchemicalEnergy.transmuteBlock(input, (w, p, r) ->
			{
				w.destroyBlock(p, false);

				EntityItem item = new EntityItem(w, p.getX() + 0.5F, p.getY() + 0.001F, p.getZ() + 0.5F, output.copy());
				item.setPickupDelay(40);
				w.spawnEntity(item);
			}, instant));
		}
	}

	private static final class Remove
			extends BaseAction
	{
		private Remove(IBlockState input, boolean instant)
		{
			super("AlchemicalEnergy", () ->
					(instant ? BlockAlchemicalEnergy.INSTANT_TRANSMUTATIONS : BlockAlchemicalEnergy.TRANSMUTATIONS)
							.removeIf(entry -> entry.getFirst().test(input)));
		}
	}
}