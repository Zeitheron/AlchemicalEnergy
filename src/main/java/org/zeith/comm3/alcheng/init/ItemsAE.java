package org.zeith.comm3.alcheng.init;

import com.zeitheron.hammercore.internal.SimpleRegistration;
import net.minecraft.creativetab.CreativeTabs;

public class ItemsAE
{
	public static void register()
	{
		SimpleRegistration.registerFieldItemsFrom(ItemsAE.class, InfoAE.MOD_ID, CreativeTabs.BREWING);
	}
}