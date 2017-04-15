package Reika.VoidMonster;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;

import org.lwjgl.opengl.GL11;

import Reika.DragonAPI.Instantiable.RayTracer;
import Reika.DragonAPI.Libraries.ReikaEntityHelper;
import Reika.DragonAPI.Libraries.IO.ReikaColorAPI;
import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.DragonAPI.Libraries.Java.ReikaGLHelper.BlendMode;
import Reika.DragonAPI.Libraries.MathSci.ReikaPhysicsHelper;
import Reika.VoidMonster.Entity.EntityVoidMonster;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MonsterFX {

	private static final RayTracer LOS = RayTracer.getVisualLOS();
	//private static boolean isVisible;
	private static double monsterDist;
	//private static long lastMonsterRender;
	private static float monsterScreenFactor;

	//private static float rampedFog;

	/*
	public static boolean isMonsterVisible() {
		return Minecraft.getMinecraft().theWorld != null && Minecraft.getMinecraft().theWorld.getTotalWorldTime()-lastMonsterRender < 5/*BossStatus.hasColorModifier && *//*isVisible*//*;
	}
		 */

	private static float getFogDistance() {
		return 3+1.1F*(float)monsterDist;
	}

	public static void onRender(EntityVoidMonster ev, float par2) {
		if (ReikaEntityHelper.isInWorld(ev)) {
			LOS.setOrigins(ev.posX, ev.posY, ev.posZ, RenderManager.renderPosX, RenderManager.renderPosY, RenderManager.renderPosZ);
			boolean los = LOS.isClearLineOfSight(ev.worldObj);
			if (!los) {
				LOS.setOrigins(ev.posX, ev.posY+1, ev.posZ, RenderManager.renderPosX, RenderManager.renderPosY, RenderManager.renderPosZ);
				los = LOS.isClearLineOfSight(ev.worldObj);
			}

			if (los) { //not isVisible, since may have multiple monsters, and flare is per-monster, but isVisible is global for things like fog
				renderFlare(ev, par2);
				//BossStatus.hasColorModifier = true;
				//lastMonsterRender = ev.worldObj.getTotalWorldTime();
				monsterScreenFactor = Math.min(monsterScreenFactor+0.05F, 1);
				//isVisible = true;
				monsterDist = ev.getDistance(RenderManager.renderPosX, RenderManager.renderPosY, RenderManager.renderPosZ);
			}
		}
	}

	public static void onRenderLoop() {
		monsterScreenFactor = Math.max(monsterScreenFactor-0.0125F, 0);
	}

	private static void renderFlare(EntityVoidMonster ev, float par2) {
		double tick = (ev.ticksExisted+par2)/1D;
		int idx = (int)(tick%32);
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glPushMatrix();

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		BlendMode.DEFAULT.apply();
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);

		GL11.glTranslated(0, 0.8, 0);

		if (!ev.isDead) {
			RenderManager rm = RenderManager.instance;
			double dx = ev.posX-RenderManager.renderPosX;
			double dy = ev.posY-RenderManager.renderPosY;
			double dz = ev.posZ-RenderManager.renderPosZ;
			double[] angs = ReikaPhysicsHelper.cartesianToPolar(dx, dy, dz);
			GL11.glRotated(-angs[2]+35*0, 0, 1, 0);
			GL11.glRotated(90-angs[1]+10*0, 1, 0, 0);
		}

		GL11.glTranslated(-0.125, 0, 0);

		double u = (idx%8)/8D;
		double v = (idx/8)/4D;
		double du = u+1/8D;
		double dv = v+1/4D;

		ReikaTextureHelper.bindTexture(VoidMonster.class, "flare.png");

		Tessellator v5 = Tessellator.instance;
		v5.startDrawingQuads();
		double s = 2.125;
		v5.addVertexWithUV(-s, s, 0, u, dv);
		v5.addVertexWithUV(s, s, 0, du, dv);
		v5.addVertexWithUV(s, -s, 0, du, v);
		v5.addVertexWithUV(-s, -s, 0, u, v);
		v5.draw();

		GL11.glPopMatrix();
		GL11.glPopAttrib();
	}

	/*
	public static void clearCache() {
		//if (Minecraft.getMinecraft().theWorld != null && Minecraft.getMinecraft().theWorld.getTotalWorldTime()%32 == 0 && ReikaRenderHelper.getPartialTickTime() < 0.1)
		isVisible = false;
		//BossStatus.hasColorModifier = false;
		monsterDist = Double.POSITIVE_INFINITY;
	}
	 */

	public static int rampColor(int c, int original) {
		return ReikaColorAPI.mixColors(c, original, monsterScreenFactor/*MathHelper.clamp_float(Minecraft.getMinecraft().entityRenderer.bossColorModifier, 0, 1)*/);
	}

	public static float rampFog(float original) {
		/*
		if (isVisible) {
			rampedFog = Math.max(getFogDistance(), rampedFog-0.5F);
		}
		else {
			rampedFog = Math.min(original, rampedFog+0.5F);
		}
		 */
		//float fac = isMonsterVisible() ? MathHelper.clamp_float(Minecraft.getMinecraft().entityRenderer.bossColorModifier, 0, 1) : 0;
		return getFogDistance()*monsterScreenFactor+(1-monsterScreenFactor)*original;
	}

}
