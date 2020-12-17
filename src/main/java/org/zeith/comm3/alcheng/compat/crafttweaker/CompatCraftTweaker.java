package org.zeith.comm3.alcheng.compat.crafttweaker;

import com.zeitheron.hammercore.mod.ModuleLoader;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import org.zeith.comm3.alcheng.compat.BaseCompatAE;

import java.util.LinkedList;

@ModuleLoader(requiredModid = "crafttweaker")
public class CompatCraftTweaker
		extends BaseCompatAE
{
	private static final LinkedList<IAction> lateActions = new LinkedList<>();

	@Override
	public void onLoadComplete()
	{
		lateActions.forEach(CraftTweakerAPI::apply);
		lateActions.clear();
	}

	@Override
	public void init()
	{
		lateActions.forEach(CraftTweakerAPI::apply);
		lateActions.clear();
	}

	public static void addLateAction(IAction action)
	{
		lateActions.addLast(action);
	}
}