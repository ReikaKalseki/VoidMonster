package Reika.VoidMonster.Render;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;
import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.DragonAPI.Libraries.Java.ReikaGLHelper.BlendMode;
import Reika.DragonAPI.Libraries.Rendering.ReikaRenderHelper;
import Reika.VoidMonster.VoidMonster;
import Reika.VoidMonster.Entity.EntityVoidMonster;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VoidGrowthRenderer {

	public static final VoidGrowthRenderer instance = new VoidGrowthRenderer();

	private static final int MAX_VALUE = 20*12;

	private final HashMap<WorldLocation, Integer> data = new HashMap();

	private VoidGrowthRenderer() {

	}

	public Set<WorldLocation> getLocations() {
		return Collections.unmodifiableSet(data.keySet());
	}

	public float getBlockLevel(World world, int x, int y, int z) {
		return this.getBlockLevel(new WorldLocation(world, x, y, z));
	}

	public float getBlockLevel(WorldLocation loc) {
		Integer get = data.get(loc);
		return get != null ? get.intValue()/(float)MAX_VALUE : 0;
	}

	public void tickMonster(EntityVoidMonster e) {
		if (e.getDistanceSqToEntity(Minecraft.getMinecraft().thePlayer) >= 4096)
			return;
		int r = 5;
		if (e.posY < 2)
			r = 7;
		for (int j = -r; j <= r; j++) {
			int y = MathHelper.floor_double(e.posY)+j;
			if (y >= 0 && y < 256) {
				for (int i = -r; i <= r; i++) {
					for (int k = -r; k <= r; k++) {
						double d = i*i+j*j+k*k;
						double md = r*r+1.5;
						if (d <= md) {
							int x = MathHelper.floor_double(e.posX)+i;
							int z = MathHelper.floor_double(e.posZ)+k;
							this.addBlockLevel(e.worldObj, x, y, z, d/md);
						}
					}
				}
			}
		}
	}

	public void addBlockLevel(World world, int x, int y, int z, double edge) {
		Block b = world.getBlock(x, y, z);
		if ((b == Blocks.bedrock && y < 6) || b == Blocks.air || b.getRenderType() != 0)
			return;
		WorldLocation loc = new WorldLocation(world, x, y, z);
		Integer get = data.get(loc);
		int s = 2;
		int next = get != null ? get.intValue()+s : s;
		int max = MAX_VALUE;
		if (edge >= 0.25) {
			max *= 1-(edge-0.25)/0.75;
			if (get != null)
				max = Math.max(max, get.intValue());
		}
		data.put(loc, Math.min(next, max));
	}
	/*
	public void tick() {
		Iterator<Entry<WorldLocation, Integer>> it = data.entrySet().iterator();
		while (it.hasNext()) {
			Entry<WorldLocation, Integer> e = it.next();
			int v = e.getValue();
			if (v == 1)
				it.remove();
			else
				e.setValue(v-1);
		}
	}
	 */

	public void renderAndTick(World world) {
		if (data.isEmpty())
			return;

		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glPushMatrix();

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LIGHTING);
		//GL11.glDisable(GL11.GL_CULL_FACE);
		ReikaTextureHelper.bindFinalTexture(VoidMonster.class, "vines3d.png");
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		BlendMode.DEFAULT.apply();
		GL11.glDepthMask(false);
		ReikaRenderHelper.disableEntityLighting();
		GL11.glTranslated(-RenderManager.renderPosX, -RenderManager.renderPosY, -RenderManager.renderPosZ);

		Tessellator v5 = Tessellator.instance;
		v5.startDrawingQuads();

		Iterator<Entry<WorldLocation, Integer>> it = data.entrySet().iterator();
		while (it.hasNext()) {
			Entry<WorldLocation, Integer> e = it.next();
			WorldLocation loc = e.getKey();
			int v = e.getValue();
			if (loc.dimensionID == world.provider.dimensionId)
				this.render(loc, v, v5);
			if (v <= 0)
				it.remove();
			else
				e.setValue(v-1);
		}

		v5.draw();
		GL11.glPopMatrix();
		GL11.glPopAttrib();
	}

	private void render(WorldLocation c, int value, Tessellator v5) {
		//int a = 255*v/MAX_VALUE;
		int frame = 49*value/MAX_VALUE;
		int row = frame/5;
		int col = frame%10;
		float u = col/5F;
		float v = row/10F;
		float du = u+0.2F;
		float dv = v+0.1F;

		v5.setColorRGBA_I(0xffffff, 255);
		double o = 0.01;

		if (this.render(c, ForgeDirection.DOWN)) {
			v5.addVertexWithUV(c.xCoord-o, c.yCoord-o, c.zCoord-o, u, v);
			v5.addVertexWithUV(c.xCoord+1+o, c.yCoord-o, c.zCoord-o, du, v);
			v5.addVertexWithUV(c.xCoord+1+o, c.yCoord-o, c.zCoord+1+o, du, dv);
			v5.addVertexWithUV(c.xCoord-o, c.yCoord-o, c.zCoord+1+o, u, dv);
		}

		if (this.render(c, ForgeDirection.UP)) {
			v5.addVertexWithUV(c.xCoord-o, c.yCoord+1+o, c.zCoord+1+o, u, v);
			v5.addVertexWithUV(c.xCoord+1+o, c.yCoord+1+o, c.zCoord+1+o, du, v);
			v5.addVertexWithUV(c.xCoord+1+o, c.yCoord+1+o, c.zCoord-o, du, dv);
			v5.addVertexWithUV(c.xCoord-o, c.yCoord+1+o, c.zCoord-o, u, dv);
		}

		if (this.render(c, ForgeDirection.EAST)) {
			v5.addVertexWithUV(c.xCoord+1+o, c.yCoord-o, c.zCoord-o, u, v);
			v5.addVertexWithUV(c.xCoord+1+o, c.yCoord+1+o, c.zCoord-o, du, v);
			v5.addVertexWithUV(c.xCoord+1+o, c.yCoord+1+o, c.zCoord+1+o, du, dv);
			v5.addVertexWithUV(c.xCoord+1+o, c.yCoord-o, c.zCoord+1+o, u, dv);
		}

		if (this.render(c, ForgeDirection.WEST)) {
			v5.addVertexWithUV(c.xCoord-o, c.yCoord-o, c.zCoord+1+o, u, v);
			v5.addVertexWithUV(c.xCoord-o, c.yCoord+1+o, c.zCoord+1+o, du, v);
			v5.addVertexWithUV(c.xCoord-o, c.yCoord+1+o, c.zCoord-o, du, dv);
			v5.addVertexWithUV(c.xCoord-o, c.yCoord-o, c.zCoord-o, u, dv);
		}

		if (this.render(c, ForgeDirection.SOUTH)) {
			v5.addVertexWithUV(c.xCoord+1+o, c.yCoord-o, c.zCoord+1+o, u, v);
			v5.addVertexWithUV(c.xCoord+1+o, c.yCoord+1+o, c.zCoord+1+o, du, v);
			v5.addVertexWithUV(c.xCoord-o, c.yCoord+1+o, c.zCoord+1+o, du, dv);
			v5.addVertexWithUV(c.xCoord-o, c.yCoord-o, c.zCoord+1+o, u, dv);
		}

		if (this.render(c, ForgeDirection.NORTH)) {
			v5.addVertexWithUV(c.xCoord-o, c.yCoord-o, c.zCoord-o, u, v);
			v5.addVertexWithUV(c.xCoord-o, c.yCoord+1+o, c.zCoord-o, du, v);
			v5.addVertexWithUV(c.xCoord+1+o, c.yCoord+1+o, c.zCoord-o, du, dv);
			v5.addVertexWithUV(c.xCoord+1+o, c.yCoord-o, c.zCoord-o, u, dv);
		}
	}

	private boolean render(WorldLocation c, ForgeDirection dir) {
		return c.getBlock().shouldSideBeRendered(c.getWorld(), c.xCoord+dir.offsetX, c.yCoord+dir.offsetY, c.zCoord+dir.offsetZ, dir.ordinal());
	}

}
