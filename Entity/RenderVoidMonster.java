/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2016
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster.Entity;

import java.util.HashMap;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import Reika.DragonAPI.Libraries.Java.ReikaGLHelper.BlendMode;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderVoidMonster extends RenderLiving {

	private final HashMap<String, ResourceLocation> map = new HashMap();
	private static final ResourceLocation enderCrystalTextures = new ResourceLocation("textures/entity/endercrystal/endercrystal.png");
	private static final ResourceLocation armoredCreeperTextures = new ResourceLocation("textures/entity/creeper/creeper_armor.png");

	public RenderVoidMonster()
	{
		super(new ModelVoidMonster(), 0.5F);
	}

	/**
	 * Pre-Renders the VoidMonster.
	 */
	protected void preRenderVoidMonster(EntityVoidMonster ev, float par2)
	{
		float rot = ev.innerRotation + par2;
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glPushMatrix();
		double s = 2+Math.sin(Math.toRadians(rot*4));
		GL11.glTranslated(0, -1.5-s/2, 0);
		GL11.glScaled(s, s, s);
		this.bindTexture(this.getTexture("end_portal"));
		mainModel.render(ev, 0.0F, rot * 3.0F, 0, 0.0F, 0.0F, 0.0625F);

		if (ev.isHealing()) {
			float f1 = ev.ticksExisted + par2;
			this.bindTexture(armoredCreeperTextures);
			GL11.glMatrixMode(GL11.GL_TEXTURE);
			GL11.glPushMatrix();
			GL11.glLoadIdentity();
			float f2 = f1 * 0.01F;
			float f3 = f1 * 0.01F;
			GL11.glTranslatef(f2, f3, 0.0F);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glEnable(GL11.GL_BLEND);
			float f4 = 0.5F;
			GL11.glColor4f(f4, f4, f4, 1.0F);
			GL11.glDisable(GL11.GL_LIGHTING);
			BlendMode.ADDITIVE.apply();
			double d = 1.05;
			GL11.glScaled(d, d, d);
			mainModel.render(ev, 0.0F, rot * 3.0F, 0, 0.0F, 0.0F, 0.0625F);
			GL11.glMatrixMode(GL11.GL_TEXTURE);
			GL11.glPopMatrix();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);

		}

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

	/**
	 * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
	 * entityLiving, partialTickTime
	 */
	@Override
	protected void preRenderCallback(EntityLivingBase elb, float par2)
	{
		this.preRenderVoidMonster((EntityVoidMonster)elb, par2);
	}

	private ResourceLocation getTexture(String s) {
		ResourceLocation r = map.get(s);
		if (r == null) {
			r = new ResourceLocation("textures/entity/"+s+".png");
			map.put(s, r);
		}
		return r;
	}

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	@Override
	protected ResourceLocation getEntityTexture(Entity e)
	{
		return enderCrystalTextures;
	}
}
