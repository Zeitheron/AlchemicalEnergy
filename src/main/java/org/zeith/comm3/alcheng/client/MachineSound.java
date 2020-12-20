package org.zeith.comm3.alcheng.client;

import com.zeitheron.hammercore.internal.blocks.base.IBlockEnableable;
import com.zeitheron.hammercore.utils.math.MathHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class MachineSound
		extends MovingSound
{
	public static final Map<BlockPos, MachineSound> PLAYING = new HashMap<>();
	public final BlockPos machinePos;
	public final float masterVol;

	public MachineSound(SoundEvent soundIn, BlockPos machinePos, float pitch, float volume)
	{
		super(soundIn, SoundCategory.BLOCKS);
		this.machinePos = machinePos;
		this.attenuationType = AttenuationType.LINEAR;
		this.repeat = true;
		this.repeatDelay = 0;
		this.masterVol = volume;
		this.pitch = pitch;
		this.volume = 0.0001F;
		this.xPosF = machinePos.getX() + 0.5F;
		this.yPosF = machinePos.getY() + 0.5F;
		this.zPosF = machinePos.getZ() + 0.5F;
	}

	public static boolean isPlayingAt(BlockPos pos)
	{
		MachineSound s;
		return PLAYING.containsKey(pos) && (s = PLAYING.get(pos)).volume > 0F && !s.donePlaying;
	}

	@Override
	public void update()
	{
		PLAYING.put(machinePos, this);

		WorldClient wc = Minecraft.getMinecraft().world;
		if(wc == null || !wc.isBlockLoaded(machinePos))
		{
			stop();
			return;
		}
		IBlockState state = wc.getBlockState(machinePos);
		if(state.getPropertyKeys().contains(IBlockEnableable.ENABLED))
		{
			boolean on = state.getValue(IBlockEnableable.ENABLED);
			if(!on)
			{
				volume = MathHelper.approachLinear(volume / masterVol, 0, 0.035F) * masterVol;
				if(volume <= 0.0001F) stop();
			} else
			{
				volume = MathHelper.approachLinear(volume / masterVol, 1, 0.02F) * masterVol;
			}
		} else
			stop();
	}

	private void stop()
	{
		donePlaying = true;
		PLAYING.remove(machinePos);
	}
}