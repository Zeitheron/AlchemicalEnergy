package org.zeith.comm3.alcheng;

import com.zeitheron.hammercore.cfg.HCModConfigurations;
import com.zeitheron.hammercore.cfg.IConfigReloadListener;
import com.zeitheron.hammercore.cfg.fields.ModConfigPropertyBool;

@HCModConfigurations(modid = "alcheng")
public class ConfigAE
		implements IConfigReloadListener
{
	@ModConfigPropertyBool(category = "common", name = "ProjectE Fun", defaultValue = true, comment = "Should we make an integration with ProjectE transmutation table?")
	public static boolean projectEFun;
}