package org.zeith.comm3.alcheng.init;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class SoundsAE
{
	public static final SoundEvent MACHINES_ALCHEMICAL_CONDENSER = $("machines.alchemical_condenser");
	public static final SoundEvent MACHINES_ALCHEMICAL_CRAFTER = $("machines.alchemical_crafter");

	private static SoundEvent $(String name)
	{
		ResourceLocation id = new ResourceLocation(InfoAE.MOD_ID, name);
		SoundEvent se = new SoundEvent(id);
		se.setRegistryName(id);
		i.events.add(se);
		return se;
	}

	public static void register()
	{
		while(!i.events.isEmpty())
			ForgeRegistries.SOUND_EVENTS.register(i.events.remove(0));
	}

	private static class i
	{
		private static final List<SoundEvent> events = new ArrayList<>();
	}
}