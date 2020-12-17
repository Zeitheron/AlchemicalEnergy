package org.zeith.comm3.alcheng.net;

import com.zeitheron.hammercore.net.HCNet;
import com.zeitheron.hammercore.net.IPacket;
import com.zeitheron.hammercore.net.MainThreaded;
import com.zeitheron.hammercore.net.PacketContext;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.zeith.comm3.alcheng.client.MachineSound;

@MainThreaded
public class PacketPlayMachineSound
		implements IPacket
{
	ResourceLocation sound;
	BlockPos machinePos;
	float volume, pitch;

	public static void ensureStarted(World world, BlockPos pos, SoundEvent sound, float volume, float pitch)
	{
		if(!world.isRemote)
		{
			HCNet.INSTANCE.sendToAllAround(new PacketPlayMachineSound(sound.getRegistryName(), pos, volume, pitch), HCNet.point(world, new Vec3d(pos).add(0.5, 0.5, 0.5), 48));
		}
	}

	public PacketPlayMachineSound(ResourceLocation sound, BlockPos machinePos, float volume, float pitch)
	{
		this.sound = sound;
		this.machinePos = machinePos;
		this.volume = volume;
		this.pitch = pitch;
	}

	public PacketPlayMachineSound()
	{
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setString("s", sound.toString());
		nbt.setLong("r", machinePos.toLong());
		nbt.setFloat("v", volume);
		nbt.setFloat("p", pitch);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		sound = new ResourceLocation(nbt.getString("s"));
		machinePos = BlockPos.fromLong(nbt.getLong("r"));
		volume = nbt.getFloat("v");
		pitch = nbt.getFloat("p");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void executeOnClient2(PacketContext net)
	{
		SoundEvent se = ForgeRegistries.SOUND_EVENTS.getValue(sound);
		if(se != null && !MachineSound.isPlayingAt(machinePos))
			Minecraft.getMinecraft().getSoundHandler().playSound(new MachineSound(se, machinePos, pitch, volume));
	}
}