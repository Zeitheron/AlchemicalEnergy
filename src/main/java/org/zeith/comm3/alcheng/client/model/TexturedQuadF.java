package org.zeith.comm3.alcheng.client.model;

import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TexturedQuadF
{
	public PositionTextureVertex[] vertexPositions;
	public int nVertices;
	private boolean invertNormal;

	public TexturedQuadF(PositionTextureVertex[] vertices)
	{
		this.vertexPositions = vertices;
		this.nVertices = vertices.length;
	}

	public TexturedQuadF(PositionTextureVertex[] vertices, float texcoordU1, float texcoordV1, float texcoordU2, float texcoordV2)
	{
		this(vertices);
		vertices[0] = vertices[0].setTexturePosition(texcoordU2, texcoordV1);
		vertices[1] = vertices[1].setTexturePosition(texcoordU1, texcoordV1);
		vertices[2] = vertices[2].setTexturePosition(texcoordU1, texcoordV2);
		vertices[3] = vertices[3].setTexturePosition(texcoordU2, texcoordV2);
	}

	public void flipFace()
	{
		PositionTextureVertex[] apositiontexturevertex = new PositionTextureVertex[this.vertexPositions.length];

		for(int i = 0; i < this.vertexPositions.length; ++i)
		{
			apositiontexturevertex[i] = this.vertexPositions[this.vertexPositions.length - i - 1];
		}

		this.vertexPositions = apositiontexturevertex;
	}

	/**
	 * Draw this primitve. This is typically called only once as the generated drawing instructions are saved by the
	 * renderer and reused later.
	 */
	@SideOnly(Side.CLIENT)
	public void draw(BufferBuilder renderer, float scale)
	{
		Vec3d vec3d = this.vertexPositions[1].vector3D.subtractReverse(this.vertexPositions[0].vector3D);
		Vec3d vec3d1 = this.vertexPositions[1].vector3D.subtractReverse(this.vertexPositions[2].vector3D);
		Vec3d vec3d2 = vec3d1.crossProduct(vec3d).normalize();
		float f = (float) vec3d2.x;
		float f1 = (float) vec3d2.y;
		float f2 = (float) vec3d2.z;

		if(this.invertNormal)
		{
			f = -f;
			f1 = -f1;
			f2 = -f2;
		}

		renderer.begin(7, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);

		for(int i = 0; i < 4; ++i)
		{
			PositionTextureVertex v = this.vertexPositions[i];
			renderer.pos(v.vector3D.x * (double) scale, v.vector3D.y * (double) scale, v.vector3D.z * (double) scale).tex(v.texturePositionX, v.texturePositionY).normal(f, f1, f2).endVertex();
		}

		Tessellator.getInstance().draw();
	}
}