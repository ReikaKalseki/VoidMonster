package Reika.VoidMonster;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldType;
import Reika.DragonAPI.Instantiable.Interpolation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VoidFogManager {

	private static final Interpolation fogLimit = new Interpolation(false);
	private static final Interpolation colorMix = new Interpolation(false);
	private static final double MINDIST = VoidMonster.instance.getFogStrength() <= 1 ? 12 : 12/VoidMonster.instance.getFogStrength();
	private static final int MAXY = 24;

	static {
		fogLimit.addPoint(0, MINDIST);
		fogLimit.addPoint(4, 24);
		fogLimit.addPoint(16, 32);
		fogLimit.addPoint(MAXY, 512);

		colorMix.addPoint(0, 1);
		colorMix.addPoint(4, 1);
		colorMix.addPoint(16, 0.5);
		colorMix.addPoint(MAXY, 0);
	}

	private static double getEffectiveHeight() {
		EntityPlayer ep = Minecraft.getMinecraft().thePlayer;
		if (ep.noClip)
			return Double.POSITIVE_INFINITY;
		if (ep.worldObj.getWorldInfo().getTerrainType() == WorldType.FLAT)
			return Double.POSITIVE_INFINITY;
		float f = VoidMonster.instance.getFogStrength();
		if (f <= 0)
			return Double.POSITIVE_INFINITY;
		double y = ep.posY/f;
		if (y < MAXY) {
			return y;
		}
		else {
			return Double.POSITIVE_INFINITY;
		}
	}

	public static float getFogDistance() {
		double y = getEffectiveHeight();
		if (y == Double.POSITIVE_INFINITY) {
			return Float.MAX_VALUE;
		}
		else {
			return (float)fogLimit.getValue(y);
		}
	}

	public static float getColorFactor() {
		double y = getEffectiveHeight();
		if (y == Double.POSITIVE_INFINITY) {
			return 0;
		}
		else {
			return (float)colorMix.getValue(y);
		}
	}

}
