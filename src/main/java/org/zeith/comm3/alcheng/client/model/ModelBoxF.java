package org.zeith.comm3.alcheng.client.model;

import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModelBoxF
{
	/**
	 * The (x,y,z) vertex positions and (u,v) texture coordinates for each of the 8 points on a cube
	 */
	private final PositionTextureVertex[] vertexPositions;
	/**
	 * An array of 6 TexturedQuads, one for each face of a cube
	 */
	private final TexturedQuadF[] quadList;
	/**
	 * X vertex coordinate of lower box corner
	 */
	public final float posX1;
	/**
	 * Y vertex coordinate of lower box corner
	 */
	public final float posY1;
	/**
	 * Z vertex coordinate of lower box corner
	 */
	public final float posZ1;
	/**
	 * X vertex coordinate of upper box corner
	 */
	public final float posX2;
	/**
	 * Y vertex coordinate of upper box corner
	 */
	public final float posY2;
	/**
	 * Z vertex coordinate of upper box corner
	 */
	public final float posZ2;
	public String boxName;

	public ModelBoxF(TextureAtlasSprite sprite, float x, float y, float z, float dx, float dy, float dz)
	{
		this.posX1 = x;
		this.posY1 = y;
		this.posZ1 = z;
		this.posX2 = x + dx;
		this.posY2 = y + dy;
		this.posZ2 = z + dz;
		this.vertexPositions = new PositionTextureVertex[8];
		this.quadList = new TexturedQuadF[6];
		float f = x + dx;
		float f1 = y + dy;
		float f2 = z + dz;

		PositionTextureVertex positiontexturevertex7 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
		PositionTextureVertex positiontexturevertex = new PositionTextureVertex(f, y, z, 0.0F, 8.0F);
		PositionTextureVertex positiontexturevertex1 = new PositionTextureVertex(f, f1, z, 8.0F, 8.0F);
		PositionTextureVertex positiontexturevertex2 = new PositionTextureVertex(x, f1, z, 8.0F, 0.0F);
		PositionTextureVertex positiontexturevertex3 = new PositionTextureVertex(x, y, f2, 0.0F, 0.0F);
		PositionTextureVertex positiontexturevertex4 = new PositionTextureVertex(f, y, f2, 0.0F, 8.0F);
		PositionTextureVertex positiontexturevertex5 = new PositionTextureVertex(f, f1, f2, 8.0F, 8.0F);
		PositionTextureVertex positiontexturevertex6 = new PositionTextureVertex(x, f1, f2, 8.0F, 0.0F);
		this.vertexPositions[0] = positiontexturevertex7;
		this.vertexPositions[1] = positiontexturevertex;
		this.vertexPositions[2] = positiontexturevertex1;
		this.vertexPositions[3] = positiontexturevertex2;
		this.vertexPositions[4] = positiontexturevertex3;
		this.vertexPositions[5] = positiontexturevertex4;
		this.vertexPositions[6] = positiontexturevertex5;
		this.vertexPositions[7] = positiontexturevertex6;

		this.quadList[0] = new TexturedQuadF(new PositionTextureVertex[]{
				positiontexturevertex4,
				positiontexturevertex,
				positiontexturevertex1,
				positiontexturevertex5
		}, sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV());

		this.quadList[1] = new TexturedQuadF(new PositionTextureVertex[]{
				positiontexturevertex7,
				positiontexturevertex3,
				positiontexturevertex6,
				positiontexturevertex2
		}, sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV());

		this.quadList[2] = new TexturedQuadF(new PositionTextureVertex[]{
				positiontexturevertex4,
				positiontexturevertex3,
				positiontexturevertex7,
				positiontexturevertex
		}, sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV());

		this.quadList[3] = new TexturedQuadF(new PositionTextureVertex[]{
				positiontexturevertex1,
				positiontexturevertex2,
				positiontexturevertex6,
				positiontexturevertex5
		}, sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV());

		this.quadList[4] = new TexturedQuadF(new PositionTextureVertex[]{
				positiontexturevertex,
				positiontexturevertex7,
				positiontexturevertex2,
				positiontexturevertex1
		}, sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV());

		this.quadList[5] = new TexturedQuadF(new PositionTextureVertex[]{
				positiontexturevertex3,
				positiontexturevertex4,
				positiontexturevertex5,
				positiontexturevertex6
		}, sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV());
	}

	@SideOnly(Side.CLIENT)
	public void render(BufferBuilder renderer, float scale)
	{
		for(TexturedQuadF texturedquad : this.quadList)
		{
			texturedquad.draw(renderer, scale);
		}
	}

	public ModelBoxF setBoxName(String name)
	{
		this.boxName = name;
		return this;
	}
}
