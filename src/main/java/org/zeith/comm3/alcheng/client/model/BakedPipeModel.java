package org.zeith.comm3.alcheng.client.model;

import com.zeitheron.hammercore.utils.PositionedStateImplementation;
import com.zeitheron.hammercore.utils.base.Cast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.util.vector.Vector3f;
import org.zeith.comm3.alcheng.tiles.TileAlchemicalPipe;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BakedPipeModel
		implements IBakedModel
{
	public static final FaceBakery $ = new FaceBakery();
	public final ResourceLocation texture;

	public BakedPipeModel(ResourceLocation texture)
	{
		this.texture = texture;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
	{
		List<BakedQuad> quads = new ArrayList<>();

		if(side != null && state instanceof PositionedStateImplementation)
		{
			PositionedStateImplementation pstate = (PositionedStateImplementation) state;
			IBlockAccess world = pstate.getWorld();
			BlockPos pos = pstate.getPos();

			TileAlchemicalPipe pipe = Cast.cast(world.getTileEntity(pos), TileAlchemicalPipe.class);
			if(pipe != null)
			{
				TextureAtlasSprite tas = getParticleTexture();

				List<EnumFacing> faces = pipe.getConnections();

				// Core part
				if(faces.size() == 2 && faces.get(0).getOpposite() == faces.get(1))
				{
					EnumFacing f = faces.get(0);
					if(side.getAxis() != f.getAxis())
					{
						int r = 0;

						if(f.getAxis() == EnumFacing.Axis.X) r = 90;
						if(f.getAxis() == EnumFacing.Axis.Z && side.getAxis() != EnumFacing.Axis.Y) r = 90;

						quads.add($.makeBakedQuad(
								new Vector3f(5, 5, 5),
								new Vector3f(11, 11, 11),
								new BlockPartFace(side, 0, "0", new BlockFaceUV(new float[]{
										10,
										0,
										16,
										6
								}, r)),
								tas,
								side,
								ModelRotation.X0_Y0,
								null,
								true,
								true));
					}
				} else if(!faces.contains(side))
				{
					quads.add($.makeBakedQuad(
							new Vector3f(5, 5, 5),
							new Vector3f(11, 11, 11),
							new BlockPartFace(side, 0, "0",
									new BlockFaceUV(new float[]{
											0,
											0,
											6,
											6
									}, 0)),
							tas,
							side,
							ModelRotation.X0_Y0,
							null,
							true,
							true));
				}

				EnumFacing ef;

				if(faces.contains(ef = EnumFacing.DOWN) && side.getAxis() != ef.getAxis())
				{
					boolean thePipe = world.getBlockState(pos.offset(ef)).getBlock() == state.getBlock();

					quads.add($.makeBakedQuad(
							new Vector3f(5, 0, 5),
							new Vector3f(11, 5, 11),
							new BlockPartFace(side, 0, "0",
									new BlockFaceUV(new float[]{
											thePipe ? 10 : 0,
											11,
											thePipe ? 16 : 6,
											16
									}, 0)),
							tas,
							side,
							ModelRotation.X0_Y0,
							null,
							true,
							true));
				}

				if(faces.contains(ef = EnumFacing.UP) && side.getAxis() != ef.getAxis())
				{
					boolean thePipe = world.getBlockState(pos.offset(ef)).getBlock() == state.getBlock();

					quads.add($.makeBakedQuad(
							new Vector3f(5, 11, 5),
							new Vector3f(11, 16, 11),
							new BlockPartFace(side, 0, "0",
									new BlockFaceUV(new float[]{
											thePipe ? 10 : 0,
											11,
											thePipe ? 16 : 6,
											16
									}, 180)),
							tas,
							side,
							ModelRotation.X0_Y0,
							null,
							true,
							true));
				}

				if(faces.contains(ef = EnumFacing.NORTH) && side.getAxis() != ef.getAxis())
				{
					boolean thePipe = world.getBlockState(pos.offset(ef)).getBlock() == state.getBlock();

					quads.add($.makeBakedQuad(
							new Vector3f(5, 5, 0),
							new Vector3f(11, 11, 5),
							new BlockPartFace(side, 0, "0",
									new BlockFaceUV(new float[]{
											thePipe ? 10 : 0,
											11,
											thePipe ? 16 : 6,
											16
									}, side == EnumFacing.UP ? 180 : side == EnumFacing.EAST ? 90 : 270)),
							tas,
							side,
							ModelRotation.X0_Y0,
							null,
							true,
							true));
				}

				if(faces.contains(ef = EnumFacing.SOUTH) && side.getAxis() != ef.getAxis())
				{
					boolean thePipe = world.getBlockState(pos.offset(ef)).getBlock() == state.getBlock();

					quads.add($.makeBakedQuad(
							new Vector3f(5, 5, 11),
							new Vector3f(11, 11, 16),
							new BlockPartFace(side, 0, "0",
									new BlockFaceUV(new float[]{
											thePipe ? 10 : 0,
											11,
											thePipe ? 16 : 6,
											16
									}, side == EnumFacing.UP ? 0 : side == EnumFacing.EAST ? 270 : 90)),
							tas,
							side,
							ModelRotation.X0_Y0,
							null,
							true,
							true));
				}

				if(faces.contains(ef = EnumFacing.WEST) && side.getAxis() != ef.getAxis())
				{
					boolean thePipe = world.getBlockState(pos.offset(ef)).getBlock() == state.getBlock();

					quads.add($.makeBakedQuad(
							new Vector3f(0, 5, 5),
							new Vector3f(5, 11, 11),
							new BlockPartFace(side, 0, "0",
									new BlockFaceUV(new float[]{
											thePipe ? 10 : 0,
											11,
											thePipe ? 16 : 6,
											16
									}, side == EnumFacing.NORTH ? 90 : 270)),
							tas,
							side,
							ModelRotation.X0_Y0,
							null,
							true,
							true));
				}

				if(faces.contains(ef = EnumFacing.EAST) && side.getAxis() != ef.getAxis())
				{
					boolean thePipe = world.getBlockState(pos.offset(ef)).getBlock() == state.getBlock();

					quads.add($.makeBakedQuad(
							new Vector3f(11, 5, 5),
							new Vector3f(16, 11, 11),
							new BlockPartFace(side, 0, "0",
									new BlockFaceUV(new float[]{
											thePipe ? 10 : 0,
											11,
											thePipe ? 16 : 6,
											16
									}, side == EnumFacing.NORTH ? 270 : 90)),
							tas,
							side,
							ModelRotation.X0_Y0,
							null,
							true,
							true));
				}
			}
		}

		return quads;
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return false;
	}

	@Override
	public boolean isGui3d()
	{
		return false;
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texture.toString());
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return ItemOverrideList.NONE;
	}
}