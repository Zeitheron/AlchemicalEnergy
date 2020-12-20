package org.zeith.comm3.alcheng.api.machines.upgrades;

import net.minecraft.util.ResourceLocation;
import org.zeith.comm3.alcheng.api.machines.IMachineTile;
import org.zeith.comm3.alcheng.init.InfoAE;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class UpgradablePart
{
	public static final UpgradablePart ALCHEMICAL_BUFFER = new UpgradablePart(new ResourceLocation(InfoAE.MOD_ID, "alchemical_buffer"), 12, (m, u) -> Math.min(12F, m.countUpgrades(u)));
	public static final UpgradablePart ENERGY = new UpgradablePart(new ResourceLocation(InfoAE.MOD_ID, "energy"), 12, (m, u) -> Math.min(12F, m.countUpgrades(u)));
	public static final UpgradablePart RF_BUFFER = new UpgradablePart(new ResourceLocation(InfoAE.MOD_ID, "rf_buffer"), 13, (m, u) -> m.countUpgrades(u) / 12F);
	public static final UpgradablePart SPEED = new UpgradablePart(new ResourceLocation(InfoAE.MOD_ID, "speed"), 13, (m, u) -> m.countUpgrades(u) / 12F);
	public static final UpgradablePart STACK = new UpgradablePart(new ResourceLocation(InfoAE.MOD_ID, "stack"), 12, (m, u) -> Math.min(12F, m.countUpgrades(u)));

	private final int max;
	private final ResourceLocation id;
	private final Function<IMachineTile, Float> handler;

	public UpgradablePart(ResourceLocation id, int max, BiFunction<IMachineTile, UpgradablePart, Float> handler)
	{
		this.id = id;
		this.max = max;
		this.handler = m -> handler.apply(m, UpgradablePart.this);
	}

	public int getMax()
	{
		return max;
	}

	public float computeUpgradability(IMachineTile machine)
	{
		return handler.apply(machine);
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		UpgradablePart that = (UpgradablePart) o;
		return id.equals(that.id);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id);
	}

	@Override
	public String toString()
	{
		return "UpgradablePart{" +
				"id=" + id +
				'}';
	}

	public ResourceLocation itemID()
	{
		return new ResourceLocation(id.getNamespace(), "upgrades/" + id.getPath());
	}
}