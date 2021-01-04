package org.zeith.comm3.alcheng.recipes.types;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zeitheron.hammercore.api.EnergyUnit;
import com.zeitheron.hammercore.api.crafting.IBaseIngredient;
import com.zeitheron.hammercore.api.crafting.ICraftingExecutor;
import com.zeitheron.hammercore.api.crafting.ICraftingResult;
import com.zeitheron.hammercore.api.crafting.INameableRecipe;
import com.zeitheron.hammercore.api.crafting.impl.EnergyIngredient;
import com.zeitheron.hammercore.api.crafting.impl.FluidStackIngredient;
import com.zeitheron.hammercore.api.crafting.impl.ItemStackResult;
import com.zeitheron.hammercore.api.crafting.impl.MCIngredient;
import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreIngredient;
import org.zeith.comm3.alcheng.init.FluidsAE;
import org.zeith.comm3.alcheng.utils.InventoryDummyCrafting;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

public abstract class RecipeAlchemicalCrafter
		implements INameableRecipe
{
	private ResourceLocation id;

	public final NonNullList<Ingredient> inputs;
	public final ICraftingResult<ItemStack> result;
	public final int processTicks;
	public final int energyRate;
	public final int inputMb;

	public RecipeAlchemicalCrafter(int inputMb, int processTicks, int energyRate, ICraftingResult<ItemStack> result, NonNullList<Ingredient> inputs)
	{
		this.inputMb = inputMb;
		this.processTicks = processTicks;
		this.energyRate = energyRate;
		this.result = result;
		this.inputs = inputs;
	}

	public abstract boolean matches(InventoryDummyCrafting inv, ICraftingExecutor exec);

	@Override
	public ResourceLocation getRecipeName()
	{
		return id;
	}

	public RecipeAlchemicalCrafter setRecipeName(ResourceLocation id)
	{
		if(this.id == null)
			this.id = id;
		return this;
	}

	@Override
	public NonNullList<IBaseIngredient> getIngredients()
	{
		NonNullList<IBaseIngredient> ingredients = NonNullList.create();

		ingredients.add(new FluidStackIngredient(new FluidStack(FluidsAE.ALCHEMICAL_ENERGY, inputMb)));
		ingredients.add(new EnergyIngredient(energyRate * processTicks, EnergyUnit.FE));

		inputs.stream()
				.filter(i -> i != Ingredient.EMPTY && i.getMatchingStacks().length > 0)
				.forEach(i -> ingredients.add(new MCIngredient(i)));

		return ingredients;
	}

	@Override
	public ICraftingResult<ItemStack> getResult()
	{
		return result;
	}

	public static RecipeAlchemicalCrafter parseShapedInputs(int inputMb, int processTicks, int energyRate, ItemStack result, Object... recipeComponents)
	{
		return parseShapedInputs(inputMb, processTicks, energyRate, new ItemStackResult(result), recipeComponents);
	}

	public static RecipeAlchemicalCrafter parseShapedInputs(int inputMb, int processTicks, int energyRate, ICraftingResult<ItemStack> result, Object... recipeComponents)
	{
		StringBuilder s = new StringBuilder();
		int i = 0;
		int j = 0;
		int k = 0;

		if(recipeComponents[i] instanceof String[])
		{
			String[] astring = ((String[]) recipeComponents[i++]);

			for(String s2 : astring)
			{
				++k;
				j = s2.length();
				s.append(s2);
			}
		} else
		{
			while(recipeComponents[i] instanceof String)
			{
				String s1 = (String) recipeComponents[i++];
				++k;
				j = s1.length();
				s.append(s1);
			}
		}

		Map<Character, Ingredient> map;

		for(map = Maps.newHashMap(); i < recipeComponents.length; i += 2)
		{
			Character character = (Character) recipeComponents[i];
			Ingredient ingr = null;

			if(recipeComponents[i + 1] instanceof Item)
				ingr = Ingredient.fromItem((Item) recipeComponents[i + 1]);
			else if(recipeComponents[i + 1] instanceof Block)
				ingr = Ingredient.fromItem(Item.getItemFromBlock((Block) recipeComponents[i + 1]));
			else if(recipeComponents[i + 1] instanceof ItemStack)
				ingr = Ingredient.fromStacks(((ItemStack) recipeComponents[i + 1]).copy());
			else if(recipeComponents[i + 1] instanceof ItemStack[])
			{
				ItemStack[] items = ((ItemStack[]) recipeComponents[i + 1]).clone();
				for(int l = 0; l < items.length; ++l)
					items[l] = items[l].copy();
				ingr = Ingredient.fromStacks(items);
			} else if(recipeComponents[i + 1] instanceof String)
				ingr = new OreIngredient(recipeComponents[i + 1] + "");
			else if(recipeComponents[i + 1] instanceof Ingredient)
				ingr = (Ingredient) recipeComponents[i + 1];

			map.put(character, ingr);
		}

		NonNullList<Ingredient> inputs = NonNullList.withSize(j * k, Ingredient.EMPTY);

		for(int l = 0; l < j * k; ++l)
		{
			char c0 = s.charAt(l);
			if(map.containsKey(c0))
				inputs.set(l, map.get(c0));
		}

		return new Shaped(inputMb, processTicks, energyRate, result, j, k, inputs);
	}

	public static RecipeAlchemicalCrafter parseShapelessInputs(int inputMb, int processTicks, int energyRate, ItemStack result, Object... recipeComponents)
	{
		return parseShapelessInputs(inputMb, processTicks, energyRate, new ItemStackResult(result), recipeComponents);
	}

	public static RecipeAlchemicalCrafter parseShapelessInputs(int inputMb, int processTicks, int energyRate, ICraftingResult<ItemStack> result, Object... recipeComponents)
	{
		NonNullList<Ingredient> list = NonNullList.create();

		for(Object object : recipeComponents)
		{
			Ingredient ingr = null;

			if(object instanceof Item)
				ingr = Ingredient.fromItem((Item) object);
			else if(object instanceof Block)
				ingr = Ingredient.fromItem(Item.getItemFromBlock((Block) object));
			else if(object instanceof ItemStack)
				ingr = Ingredient.fromStacks(((ItemStack) object).copy());
			else if(object instanceof ItemStack[])
			{
				ItemStack[] items = ((ItemStack[]) object).clone();
				for(int l = 0; l < items.length; ++l)
					items[l] = items[l].copy();
				ingr = Ingredient.fromStacks(items);
			} else if(object instanceof String)
				ingr = new OreIngredient(object + "");
			else if(object instanceof Ingredient)
				ingr = (Ingredient) object;

			if(ingr != null)
				list.add(ingr);
			else
				throw new IllegalArgumentException("Invalid shapeless recipe: unknown type " + object.getClass().getName() + "!");
		}

		return new Shapeless(inputMb, processTicks, energyRate, result, list);
	}

	public static class Shaped
			extends RecipeAlchemicalCrafter
	{
		public final int recipeWidth;
		public final int recipeHeight;

		public Shaped(int inputMb, int processTicks, int energyRate, ICraftingResult<ItemStack> result, int width, int height, NonNullList<Ingredient> inputs)
		{
			super(inputMb, processTicks, energyRate, result, inputs);
			this.recipeWidth = width;
			this.recipeHeight = height;
		}

		@Override
		public boolean matches(InventoryDummyCrafting inv, ICraftingExecutor exec)
		{
			for(int i = 0; i <= inv.getWidth() - this.recipeWidth; ++i)
				for(int j = 0; j <= inv.getHeight() - this.recipeHeight; ++j)
				{
					if(checkMatch(inv, i, j, true))
						return true;
					if(checkMatch(inv, i, j, false))
						return true;
				}

			return false;
		}

		private boolean checkMatch(InventoryDummyCrafting craftingInventory, int x, int y, boolean owo)
		{
			for(int i = 0; i < craftingInventory.getWidth(); ++i)
			{
				for(int j = 0; j < craftingInventory.getHeight(); ++j)
				{
					int k = i - x;
					int l = j - y;
					Ingredient ingredient = Ingredient.EMPTY;

					if(k >= 0 && l >= 0 && k < this.recipeWidth && l < this.recipeHeight)
					{
						if(owo) ingredient = this.inputs.get(this.recipeWidth - k - 1 + l * this.recipeWidth);
						else ingredient = this.inputs.get(k + l * this.recipeWidth);
					}

					if(!ingredient.apply(craftingInventory.getStackInRowAndColumn(i, j)))
						return false;
				}
			}

			return true;
		}
	}

	public static class Shapeless
			extends RecipeAlchemicalCrafter
	{
		private final boolean isSimple;

		public Shapeless(int inputMb, int processTicks, int energyRate, ICraftingResult<ItemStack> result, NonNullList<Ingredient> inputs)
		{
			super(inputMb, processTicks, energyRate, result, inputs);

			boolean simple = true;
			for(Ingredient i : inputs)
				simple &= i.isSimple();
			this.isSimple = simple;
		}

		@Override
		public boolean matches(InventoryDummyCrafting inv, ICraftingExecutor exec)
		{
			int ingredientCount = 0;
			RecipeItemHelper recipeItemHelper = new RecipeItemHelper();
			List<ItemStack> inputs = Lists.newArrayList();

			for(int i = 0; i < inv.getHeight(); ++i)
			{
				for(int j = 0; j < inv.getWidth(); ++j)
				{
					ItemStack itemstack = inv.getStackInRowAndColumn(j, i);

					if(!itemstack.isEmpty())
					{
						++ingredientCount;
						if(this.isSimple)
							recipeItemHelper.accountStack(itemstack, 1);
						else
							inputs.add(itemstack);
					}
				}
			}

			if(ingredientCount != this.inputs.size())
				return false;

			if(this.isSimple)
				return recipeItemHelper.canCraft(this, null);

			return net.minecraftforge.common.util.RecipeMatcher.findMatches(inputs, this.inputs) != null;
		}

		static class RecipeItemHelper
		{
			/**
			 * Map from {@link #pack} packed ids to counts
			 */
			public final Int2IntMap itemToCount = new Int2IntOpenHashMap();

			public void accountStack(ItemStack stack)
			{
				this.accountStack(stack, -1);
			}

			public void accountStack(ItemStack stack, int forceCount)
			{
				if(!stack.isEmpty() && !stack.isItemDamaged() && !stack.isItemEnchanted() && !stack.hasDisplayName())
				{
					int i = pack(stack);
					int j = forceCount == -1 ? stack.getCount() : forceCount;
					this.increment(i, j);
				}
			}

			public static int pack(ItemStack stack)
			{
				Item item = stack.getItem();
				int i = item.getHasSubtypes() ? stack.getMetadata() : 0;
				return Item.REGISTRY.getIDForObject(item) << 16 | i & 65535;
			}

			public boolean containsItem(int packedItem)
			{
				return this.itemToCount.get(packedItem) > 0;
			}

			public int tryTake(int packedItem, int maximum)
			{
				int i = this.itemToCount.get(packedItem);

				if(i >= maximum)
				{
					this.itemToCount.put(packedItem, i - maximum);
					return packedItem;
				} else
				{
					return 0;
				}
			}

			private void increment(int packedItem, int amount)
			{
				this.itemToCount.put(packedItem, this.itemToCount.get(packedItem) + amount);
			}

			public boolean canCraft(RecipeAlchemicalCrafter recipe, @Nullable IntList packedItemList)
			{
				return this.canCraft(recipe, packedItemList, 1);
			}

			public boolean canCraft(RecipeAlchemicalCrafter recipe, @Nullable IntList packedItemList, int maxAmount)
			{
				return (new RecipeItemHelper.RecipePicker(recipe)).tryPick(maxAmount, packedItemList);
			}

			public int getBiggestCraftableStack(RecipeAlchemicalCrafter recipe, @Nullable IntList packedItemList)
			{
				return this.getBiggestCraftableStack(recipe, Integer.MAX_VALUE, packedItemList);
			}

			public int getBiggestCraftableStack(RecipeAlchemicalCrafter recipe, int maxAmount, @Nullable IntList packedItemList)
			{
				return (new RecipeItemHelper.RecipePicker(recipe)).tryPickAll(maxAmount, packedItemList);
			}

			public static ItemStack unpack(int packedItem)
			{
				return packedItem == 0 ? ItemStack.EMPTY : new ItemStack(Item.getItemById(packedItem >> 16 & 65535), 1, packedItem & 65535);
			}

			public void clear()
			{
				this.itemToCount.clear();
			}

			class RecipePicker
			{
				private final RecipeAlchemicalCrafter recipe;
				private final List<Ingredient> ingredients = Lists.<Ingredient> newArrayList();
				private final int ingredientCount;
				private final int[] possessedIngredientStacks;
				private final int possessedIngredientStackCount;
				private final BitSet data;
				private IntList path = new IntArrayList();

				public RecipePicker(RecipeAlchemicalCrafter recipeIn)
				{
					this.recipe = recipeIn;
					this.ingredients.addAll(recipeIn.inputs);
					this.ingredients.removeIf((p_194103_0_) ->
					{
						return p_194103_0_ == Ingredient.EMPTY;
					});
					this.ingredientCount = this.ingredients.size();
					this.possessedIngredientStacks = this.getUniqueAvailIngredientItems();
					this.possessedIngredientStackCount = this.possessedIngredientStacks.length;
					this.data = new BitSet(this.ingredientCount + this.possessedIngredientStackCount + this.ingredientCount + this.ingredientCount * this.possessedIngredientStackCount);

					for(int i = 0; i < this.ingredients.size(); ++i)
					{
						IntList intlist = ((Ingredient) this.ingredients.get(i)).getValidItemStacksPacked();

						for(int j = 0; j < this.possessedIngredientStackCount; ++j)
						{
							if(intlist.contains(this.possessedIngredientStacks[j]))
							{
								this.data.set(this.getIndex(true, j, i));
							}
						}
					}
				}

				public boolean tryPick(int maxAmount, @Nullable IntList listIn)
				{
					if(maxAmount <= 0)
					{
						return true;
					} else
					{
						int k;

						for(k = 0; this.dfs(maxAmount); ++k)
						{
							RecipeItemHelper.this.tryTake(this.possessedIngredientStacks[this.path.getInt(0)], maxAmount);
							int l = this.path.size() - 1;
							this.setSatisfied(this.path.getInt(l));

							for(int i1 = 0; i1 < l; ++i1)
							{
								this.toggleResidual((i1 & 1) == 0, ((Integer) this.path.get(i1)).intValue(), ((Integer) this.path.get(i1 + 1)).intValue());
							}

							this.path.clear();
							this.data.clear(0, this.ingredientCount + this.possessedIngredientStackCount);
						}

						boolean flag = k == this.ingredientCount;
						boolean flag1 = flag && listIn != null;

						if(flag1)
						{
							listIn.clear();
						}

						this.data.clear(0, this.ingredientCount + this.possessedIngredientStackCount + this.ingredientCount);
						int j1 = 0;
						List<Ingredient> list = this.recipe.inputs;

						for(int k1 = 0; k1 < list.size(); ++k1)
						{
							if(flag1 && list.get(k1) == Ingredient.EMPTY)
							{
								listIn.add(0);
							} else
							{
								for(int l1 = 0; l1 < this.possessedIngredientStackCount; ++l1)
								{
									if(this.hasResidual(false, j1, l1))
									{
										this.toggleResidual(true, l1, j1);
										RecipeItemHelper.this.increment(this.possessedIngredientStacks[l1], maxAmount);

										if(flag1)
										{
											listIn.add(this.possessedIngredientStacks[l1]);
										}
									}
								}

								++j1;
							}
						}

						return flag;
					}
				}

				private int[] getUniqueAvailIngredientItems()
				{
					IntCollection intcollection = new IntAVLTreeSet();

					for(Ingredient ingredient : this.ingredients)
					{
						intcollection.addAll(ingredient.getValidItemStacksPacked());
					}

					IntIterator intiterator = intcollection.iterator();

					while(intiterator.hasNext())
					{
						if(!RecipeItemHelper.this.containsItem(intiterator.nextInt()))
						{
							intiterator.remove();
						}
					}

					return intcollection.toIntArray();
				}

				private boolean dfs(int amount)
				{
					int k = this.possessedIngredientStackCount;

					for(int l = 0; l < k; ++l)
					{
						if(RecipeItemHelper.this.itemToCount.get(this.possessedIngredientStacks[l]) >= amount)
						{
							this.visit(false, l);

							while(!this.path.isEmpty())
							{
								int i1 = this.path.size();
								boolean flag = (i1 & 1) == 1;
								int j1 = this.path.getInt(i1 - 1);

								if(!flag && !this.isSatisfied(j1))
								{
									break;
								}

								int k1 = flag ? this.ingredientCount : k;

								for(int l1 = 0; l1 < k1; ++l1)
								{
									if(!this.hasVisited(flag, l1) && this.hasConnection(flag, j1, l1) && this.hasResidual(flag, j1, l1))
									{
										this.visit(flag, l1);
										break;
									}
								}

								int i2 = this.path.size();

								if(i2 == i1)
								{
									this.path.removeInt(i2 - 1);
								}
							}

							if(!this.path.isEmpty())
							{
								return true;
							}
						}
					}

					return false;
				}

				private boolean isSatisfied(int p_194091_1_)
				{
					return this.data.get(this.getSatisfiedIndex(p_194091_1_));
				}

				private void setSatisfied(int p_194096_1_)
				{
					this.data.set(this.getSatisfiedIndex(p_194096_1_));
				}

				private int getSatisfiedIndex(int p_194094_1_)
				{
					return this.ingredientCount + this.possessedIngredientStackCount + p_194094_1_;
				}

				private boolean hasConnection(boolean p_194093_1_, int p_194093_2_, int p_194093_3_)
				{
					return this.data.get(this.getIndex(p_194093_1_, p_194093_2_, p_194093_3_));
				}

				private boolean hasResidual(boolean p_194100_1_, int p_194100_2_, int p_194100_3_)
				{
					return p_194100_1_ != this.data.get(1 + this.getIndex(p_194100_1_, p_194100_2_, p_194100_3_));
				}

				private void toggleResidual(boolean p_194089_1_, int p_194089_2_, int p_194089_3_)
				{
					this.data.flip(1 + this.getIndex(p_194089_1_, p_194089_2_, p_194089_3_));
				}

				private int getIndex(boolean p_194095_1_, int p_194095_2_, int p_194095_3_)
				{
					int k = p_194095_1_ ? p_194095_2_ * this.ingredientCount + p_194095_3_ : p_194095_3_ * this.ingredientCount + p_194095_2_;
					return this.ingredientCount + this.possessedIngredientStackCount + this.ingredientCount + 2 * k;
				}

				private void visit(boolean p_194088_1_, int p_194088_2_)
				{
					this.data.set(this.getVisitedIndex(p_194088_1_, p_194088_2_));
					this.path.add(p_194088_2_);
				}

				private boolean hasVisited(boolean p_194101_1_, int p_194101_2_)
				{
					return this.data.get(this.getVisitedIndex(p_194101_1_, p_194101_2_));
				}

				private int getVisitedIndex(boolean p_194099_1_, int p_194099_2_)
				{
					return (p_194099_1_ ? 0 : this.ingredientCount) + p_194099_2_;
				}

				public int tryPickAll(int p_194102_1_, @Nullable IntList list)
				{
					int k = 0;
					int l = Math.min(p_194102_1_, this.getMinIngredientCount()) + 1;

					while(true)
					{
						int i1 = (k + l) / 2;

						if(this.tryPick(i1, (IntList) null))
						{
							if(l - k <= 1)
							{
								if(i1 > 0)
								{
									this.tryPick(i1, list);
								}

								return i1;
							}

							k = i1;
						} else
						{
							l = i1;
						}
					}
				}

				private int getMinIngredientCount()
				{
					int k = Integer.MAX_VALUE;

					for(Ingredient ingredient : this.ingredients)
					{
						int l = 0;
						int i1;

						for(IntListIterator intlistiterator = ingredient.getValidItemStacksPacked().iterator(); intlistiterator.hasNext(); l = Math.max(l, RecipeItemHelper.this.itemToCount.get(i1)))
						{
							i1 = ((Integer) intlistiterator.next()).intValue();
						}

						if(k > 0)
						{
							k = Math.min(k, l);
						}
					}

					return k;
				}
			}
		}
	}
}