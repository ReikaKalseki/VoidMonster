/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster.Render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.MathHelper;

import Reika.DragonAPI.IO.AppIconManager;
import Reika.DragonAPI.IO.AppIconManager.AppIcon;
import Reika.DragonAPI.IO.ReikaImageLoader;
import Reika.DragonAPI.Instantiable.RayTracer;
import Reika.DragonAPI.Instantiable.RayTracer.RayTracerWithCache;
import Reika.DragonAPI.Instantiable.Math.Noise.Simplex3DGenerator;
import Reika.DragonAPI.Libraries.ReikaEntityHelper;
import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.DragonAPI.Libraries.Java.ReikaGLHelper.BlendMode;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.MathSci.ReikaPhysicsHelper;
import Reika.VoidMonster.VoidClient;
import Reika.VoidMonster.VoidMonster;
import Reika.VoidMonster.Entity.EntityVoidMonster;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MonsterFX {

	private static final RayTracerWithCache LOS = RayTracer.getVisualLOSForRenderCulling();
	// private static boolean isVisible;
	private static double monsterDist;
	// private static long lastMonsterRender;
	private static float monsterScreenFactor;
	private static float monsterScreenFactor05;

	private static int renderFrame;
	private static long lastOSTitleTick;
	private static long lastOSIconTick;
	private static String ICON_ID = "VoidMonster";

	private static GraphicsDevice currentMonitor;
	private static Insets osBorder;
	private static volatile int gameOuterLeft;
	private static volatile int gameOuterTop;
	private static volatile int gameOuterRight;
	private static volatile int gameOuterBottom;
	private static long lastPaintNS = System.nanoTime();
	private static long lastRepaintMillis = 0;

	private static JWindow leakingFXWindow;
	private static boolean wasActive = false;
	private static long lastZFixTime = 0;
	private static final BufferedImage vines = ReikaImageLoader.readImage(VoidMonster.class, "vines3d.png", null);
	private static final int CELL_IMG_SIZE = 32;
	private static final int CELL_SIZE = 64;
	private static final float MAX_SPREAD = 640;
	private static final Simplex3DGenerator crackNoise = (Simplex3DGenerator)new Simplex3DGenerator(System.currentTimeMillis()).setFrequency(0.04F);

	private static final List<CloudFX> clouds = new ArrayList();
	private static final Random embRand = new Random();
	private static final BufferedImage glowSprite = createSoftCloudSprite();
	//private static final BufferedImage[] largeAuraTexture = createWindowAura();

	public static final int MAX_BRANCHES = 9;
	private static double[] cloudAngle = new double[MAX_BRANCHES];
	private static double[] cloudAngleVel = new double[MAX_BRANCHES];

	public static class CloudFX {
		public float posX;
		public float posY;
		public float motionX;
		public float motionY;

		public int particleAge;
		public int particleMaxAge;
		public float particleScale;

		public float particleAlpha = 1;

		private final float phaseOffset;
		private final double waverSpeed;

		private final long randomSeed;

		public CloudFX(double x, double y, double vx, double vy, int maxAgeTicks, double scale) {
			posX = (float)x;
			posY = (float)y;
			motionX = (float)vx;
			motionY = (float)vy;
			particleAge = 0;
			particleMaxAge = maxAgeTicks;
			particleScale = (float)scale;
			phaseOffset = (float)(embRand.nextDouble() * 2 * Math.PI);
			waverSpeed = ReikaRandomHelper.getRandomBetween(0.25, 0.45, embRand);
			randomSeed = embRand.nextLong();
		}

		public boolean onUpdate() {
			if (particleAge++ >= particleMaxAge)
				return false;

			//motionX += Math.sin(particleAge * waverSpeed + phaseOffset) * 0.25;
			//motionY += Math.cos(particleAge * waverSpeed + phaseOffset) * 0.25;

			posX += motionX;
			posY += motionY;

			float lifeRatio = (float) particleAge / (float) particleMaxAge;
			particleAlpha = lifeRatio < 0.5f ? 1 : 1 - ((lifeRatio - 0.5f) * 2);

			return true;
		}

		public void renderParticle(Graphics2D g2, float intensity) {
			float alpha = MathHelper.clamp_float(particleAlpha * intensity, 0, 1);
			if (alpha <= 0 || glowSprite == null)
				return;

			int drawSize = Math.max(2, Math.round(particleScale * 3));

			int drawX = Math.round(posX - drawSize / 2);
			int drawY = Math.round(posY - drawSize / 2);

			Composite prev = g2.getComposite();
			Random rand = new Random(randomSeed);
			rand.nextBoolean();
			int r = ReikaRandomHelper.getRandomBetween(4, 12, rand);
			float a = MathHelper.clamp_float((float)ReikaRandomHelper.getRandomBetween(alpha/4, alpha, rand), 0, 1);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
			int s = Math.max(r*2, ReikaRandomHelper.getRandomPlusMinus(drawSize, 2, rand));
			int x = ReikaRandomHelper.getRandomPlusMinus(drawX, r, rand)+(int)(Math.sin(particleAge * 0.05 + phaseOffset*2373) * 2);
			int y = ReikaRandomHelper.getRandomPlusMinus(drawY, r, rand)+(int)(Math.sin(particleAge * 0.05 + phaseOffset*1261) * 2);
			g2.drawImage(glowSprite, x, y, s, s, null);
			g2.setComposite(prev);
		}
	}

	private static BufferedImage[] createWindowAura() {
		BufferedImage raw = ReikaImageLoader.readImage(VoidMonster.class, "leak-flare.png", null);
		BufferedImage[] ret = new BufferedImage[8];
		int w = 960;
		int h = 540;
		for (int i = 0; i < ret.length; i++) {
			int col = i%4;
			int row = i/4;
			int x = w*col;
			int y = row*h;
			ret[i] = raw.getSubimage(x, y, w, h);
		}
		return ret;
	}

	private static BufferedImage createSoftCloudSprite() {
		int size = 128;
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = img.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		float center = size / 2;
		RadialGradientPaint rgp = new RadialGradientPaint(
				center, center, center,
				new float[]{0, 0.25F, 0.4F, 1},
				new Color[]{new Color(0, 0, 0, 255), new Color(0, 0, 0, 160), new Color(0, 0, 0, 16), new Color(0, 0, 0, 0)}
				);
		g2.setPaint(rgp);
		g2.fillOval(0, 0, size, size);
		g2.dispose();
		return img;
	}

	private static void updateAndDrawClouds(Graphics2D g2, int gLeft, int gTop, int gWidth, int gHeight, float intensity) {
		spawnEdgeCloud(gLeft, gTop, gWidth, gHeight);

		Iterator<CloudFX> it = clouds.iterator();
		while (it.hasNext()) {
			CloudFX cloud = it.next();
			if (!cloud.onUpdate()) {
				it.remove();
				continue;
			}
			cloud.renderParticle(g2, intensity);
		}
	}

	private static void spawnEdgeCloud(int gLeft, int gTop, int gWidth, int gHeight) {
		double dr = 128;
		for (int i = 0; i < MAX_BRANCHES; i++) {
			for (int k = 0; k < 3; k++) {
				int maxAge = ReikaRandomHelper.getRandomBetween(20, 60, embRand);
				double scale = ReikaRandomHelper.getRandomBetween(50D, 100D, embRand);
				double ang = ReikaRandomHelper.getRandomPlusMinus(cloudAngle[i], 2, embRand);
				double v = ReikaRandomHelper.getRandomBetween(8, 10, embRand);
				double dx = Math.cos(Math.toRadians(ang));
				double dy = Math.sin(Math.toRadians(ang));
				double vx = v*dx*1.5;
				double vy = v*dy;
				clouds.add(new CloudFX(gLeft+gWidth/2+dx*dr*1.5, gTop+gHeight/2+dy*dr, vx, vy, maxAge, scale));
			}

			cloudAngle[i] += cloudAngleVel[i];

			if (embRand.nextInt(200) == 0) {
				cloudAngleVel[i] = ReikaRandomHelper.getRandomPlusMinus(0, 1.5D);
			}
		}
	}

	static {
		ByteBuffer large = AppIconManager.generateIcon(ReikaImageLoader.readImage(VoidMonster.class, "flare-icon-0-32.png", null));
		AppIconManager.registerIcon(VoidMonster.instance, new AppIcon(ICON_ID, 32, large));
		ByteBuffer[] small = AppIconManager.generateIconFrames(ReikaImageLoader.readImage(VoidMonster.class, "flare-icon-small.png", null), 16);
		AppIconManager.registerIcon(VoidMonster.instance, new AppIcon(ICON_ID, 16, small));

		SwingUtilities.invokeLater(() -> {
			try {
				GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
				if (!gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT)) {
					return;
				}

				osBorder = computeOSInsets();

				leakingFXWindow = new JWindow();
				leakingFXWindow.setFocusableWindowState(false);
				leakingFXWindow.setAutoRequestFocus(false);
				leakingFXWindow.setBackground(new Color(0, 0, 0, 0));

				JPanel canvas = new JPanel() {
					@Override
					protected void paintComponent(Graphics g) {
						super.paintComponent(g);
						long now = System.nanoTime();
						float dt = Math.min(0.1f, (now - lastPaintNS) / 1000000000F);
						lastPaintNS = now;

						Graphics2D g2 = (Graphics2D)g;
						int w = this.getWidth();
						int h = this.getHeight();
						g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

						g2.setComposite(AlphaComposite.Clear);
						g2.fillRect(0, 0, this.getWidth(), this.getHeight());
						g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25F));

						if (monsterScreenFactor05 <= 4)
							return;

						int winX = leakingFXWindow.getX();
						int winY = leakingFXWindow.getY();
						int gLeft   = gameOuterLeft - winX;
						int gTop    = gameOuterTop - winY;
						int gRight  = gameOuterRight - winX;
						int gBottom = gameOuterBottom - winY;
						int gWidth  = gRight - gLeft;
						int gHeight = gBottom - gTop;

						Area outerClip = new Area(new Rectangle(0, 0, w, h));
						outerClip.subtract(new Area(new Rectangle(gLeft, gTop, gWidth, gHeight)));
						g2.setClip(outerClip);

						float f = MathHelper.clamp_float((monsterScreenFactor05 - 4)/5.875F, 0, 1);
						/*
						if (largeAuraTexture != null) {
							int pad = (int) MAX_SPREAD;
							Composite prev = g2.getComposite();
							g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, f));
							g2.drawImage(largeAuraTexture[renderFrame%largeAuraTexture.length], gLeft - pad, gTop - pad, gWidth + pad * 2, gHeight + pad * 2, null);
							g2.setComposite(prev);
						}
						 */
						drawLeakingTendrils(g2, w, h, gLeft, gTop, gRight, gBottom, f);
						updateAndDrawClouds(g2, gLeft, gTop, gWidth, gHeight, f);

						//cut out game window
						g2.setComposite(AlphaComposite.Clear);
						g2.fillRect(gLeft, gTop, gWidth, gHeight);
					}
				};
				canvas.setOpaque(false);
				leakingFXWindow.setContentPane(canvas);
			}
			catch (Throwable t) {

			}
		});
	}

	private static Insets computeOSInsets() {
		Frame dummy = new Frame();
		try {
			dummy.addNotify();
			Insets insets = dummy.getInsets();
			// insets.top = title bar height (e.g., 31px on Win10/11, 28px on macOS)
			// insets.left = left border thickness (e.g., 8px on Windows, 0px on macOS)
			// insets.right = right border thickness
			// insets.bottom = bottom border thickness
			return insets;
		}
		catch (Throwable t) {
			return new Insets(31, 8, 8, 8);
		}
		finally {
			dummy.dispose();
		}
	}

	private static void drawLeakingTendrils(Graphics2D g2, int screenW, int screenH, int gLeft, int gTop, int gRight, int gBottom, float intensity) {
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

		int minCol = MathHelper.floor_double((Math.max(0, gLeft - MAX_SPREAD) - gLeft) / CELL_SIZE);
		int maxCol = MathHelper.ceiling_double_int((Math.min(screenW, gRight + MAX_SPREAD) - gLeft) / CELL_SIZE);
		int minRow = MathHelper.floor_double((Math.max(0, gTop - MAX_SPREAD) - gTop) / CELL_SIZE);
		int maxRow = MathHelper.ceiling_double_int((Math.min(screenH, gBottom + MAX_SPREAD) - gTop) / CELL_SIZE);

		for (int row = minRow; row <= maxRow; row++) {
			int cellY = gTop + row * CELL_SIZE;
			int centerY = cellY + CELL_SIZE / 2;

			for (int col = minCol; col <= maxCol; col++) {
				int cellX = gLeft + col * CELL_SIZE;
				int centerX = cellX + CELL_SIZE / 2;

				if (cellX >= gLeft && cellX + CELL_SIZE <= gRight && cellY >= gTop  && cellY + CELL_SIZE <= gBottom)
					;//continue;

				int dx = Math.max(0, Math.max(gLeft - centerX, centerX - gRight));
				int dy = Math.max(0, Math.max(gTop - centerY, centerY - gBottom));
				float dist = MathHelper.sqrt_double(dx * dx + dy * dy);
				double distanceFactor = 1 - dist*1.5/MAX_SPREAD;
				if (distanceFactor <= 0)
					continue;

				float time = (System.currentTimeMillis()%36000000)/50F;//Minecraft.getMinecraft().thePlayer.ticksExisted;
				double localIntensity = ReikaMathLibrary.normalizeToBounds(crackNoise.getValue(col * 8, row * 8, time)*4, 0, intensity * distanceFactor);

				if (localIntensity <= 0)
					continue;

				int stage = Math.min(49, (int)(localIntensity * 50));

				int sx1 = (stage % 5) * CELL_IMG_SIZE;
				int sy1 = (stage / 5) * CELL_IMG_SIZE;
				int sx2 = sx1 + CELL_IMG_SIZE;
				int sy2 = sy1 + CELL_IMG_SIZE;

				g2.drawImage(vines, cellX, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, sx1, sy1, sx2, sy2, null);
			}
		}
	}

	private static float getFogDistance() {
		return 3 + 1.1F * (float)monsterDist * 5F;
	}

	public static void onRender(EntityVoidMonster ev, float par2) {
		if (ReikaEntityHelper.isInWorld(ev)) {
			boolean los = clearLOS(ev);
			if (!los) {
				LOS.setOrigins(ev.posX, ev.posY + 1, ev.posZ, RenderManager.renderPosX, RenderManager.renderPosY, RenderManager.renderPosZ);
				los = LOS.isClearLineOfSight(ev);
			}

			if (los) { // not isVisible, since may have multiple monsters, and flare is per-monster, but isVisible is global for things like fog
				renderFlare(ev, par2);
				VoidClient.getShader().setMatricesToCurrent();
				VoidClient.getShader().setFocus(ev);
				monsterScreenFactor = Math.min(monsterScreenFactor + 0.05F, 1);
				monsterDist = ev.getDistance(RenderManager.renderPosX, RenderManager.renderPosY, RenderManager.renderPosZ);
				monsterScreenFactor05 = (float)Math.min(Math.max(0, (80D - monsterDist) / 64D * 10D), Math.min(monsterScreenFactor05 + 0.05F, 10));
				VoidClient.getShader().setField("distance", monsterDist);
			}
		}
	}

	public static boolean clearLOS(EntityVoidMonster ev) {
		LOS.setOrigins(ev.posX, ev.posY, ev.posZ, RenderManager.renderPosX, RenderManager.renderPosY, RenderManager.renderPosZ);
		return LOS.isClearLineOfSight(ev);
	}

	public static void onRenderLoop(int pass) {
		monsterScreenFactor = Math.max(monsterScreenFactor - 0.0125F, 0);
		monsterScreenFactor05 = Math.min(Math.max(monsterScreenFactor05 - 0.0125F, 0), monsterScreenFactor * 10);
		if (monsterScreenFactor <= 0)
			monsterScreenFactor05 = 0;
		VoidClient.getShader().setEnabled(monsterScreenFactor > 0);
		VoidClient.getShader().setIntensity(monsterScreenFactor);
		VoidClient.getShader().setField("longIntensity", monsterScreenFactor05);
		if (pass == 1) {
			for (EntityVoidMonster ev : VoidMonster.getCurrentMonsterList(Minecraft.getMinecraft().theWorld))
				VoidGrowthRenderer.instance.tickMonster(ev);
			VoidGrowthRenderer.instance.renderAndTick(Minecraft.getMinecraft().theWorld);
		}
		if (pass == 1) {
			float m05 = Math.min(monsterScreenFactor05, 5);
			long time = System.currentTimeMillis();
			if (time - lastOSTitleTick >= 67) { // max 15FPS shuffle
				lastOSTitleTick = time;
				final String real = AppIconManager.getDefaultTitle();
				if (m05 > 0) {
					int n = real.length() + Math.max(0, (int)((m05 - 2) * 10));
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < n; i++) {
						char letter = i < real.length() ? real.charAt(i) : ' ';
						if (ReikaRandomHelper.doWithChance(m05 / 5D)) {
							do {
								letter = (char)ReikaRandomHelper.getRandomBetween(0x0041, 0x10000); // 32-126 for plain ascii
							} while (!Character.isLetterOrDigit(letter) && !Character.isAlphabetic(letter) && !Character.isIdeographic(letter));
						}
						sb.append(letter);
					}
					AppIconManager.setCurrentTitle(sb.toString());
				}
				else {
					AppIconManager.setCurrentTitle(null);
				}
			}

			if (time - lastOSIconTick >= 50) { // max 20FPS cycle
				lastOSIconTick = time;
				renderFrame++;
				boolean icons = m05 >= 3 || ReikaRandomHelper.doWithChance((m05 / 3D));
				AppIconManager.setCurrentIcon(icons ? ICON_ID : null, icons ? ICON_ID : null, renderFrame % 32, 0);
			}

			if (leakingFXWindow != null) {
				boolean isActive = Display.isActive();
				final boolean justFocused = isActive && !wasActive;
				wasActive = isActive;
				boolean shouldShow = m05 >= 4 && isActive && !Display.isFullscreen();
				if (!shouldShow)
					clouds.clear();
				if (!isActive)
					monsterScreenFactor05 = Math.min(monsterScreenFactor05, 4);

				int clientX = Display.getX();
				int clientY = Display.getY();
				int clientW = Display.getWidth();
				int clientH = Display.getHeight();
				int centerX = clientX + clientW / 2;
				int centerY = clientY + clientH / 2;

				GraphicsDevice activeDevice = null;
				Rectangle monitorBounds = null;
				for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
					Rectangle b = gd.getDefaultConfiguration().getBounds();
					if (b.contains(centerX, centerY)) {
						activeDevice = gd;
						monitorBounds = b;
						break;
					}
				}
				if (monitorBounds != null) {
					final boolean monitorChanged = activeDevice != currentMonitor;
					if (monitorChanged)
						currentMonitor = activeDevice;

					gameOuterLeft   = clientX + 7;
					gameOuterTop    = clientY;
					gameOuterRight  = clientX + clientW + osBorder.left + osBorder.right - 7;
					gameOuterBottom = clientY + clientH + osBorder.top + osBorder.bottom - 7;

					// Keep leakWindow 1px shorter than full monitor height to prevent Windows exclusive-fullscreen promotion
					final Rectangle targetBounds = new Rectangle(monitorBounds.x, monitorBounds.y, monitorBounds.width, monitorBounds.height - 1);

					long now = System.currentTimeMillis();
					final boolean shouldRepaint = (now - lastRepaintMillis >= 33);
					if (shouldRepaint)
						lastRepaintMillis = now;

					SwingUtilities.invokeLater(() -> {
						if (monitorChanged)
							osBorder = computeOSInsets();

						if (shouldShow) {
							if (!leakingFXWindow.getBounds().equals(targetBounds))
								leakingFXWindow.setBounds(targetBounds);

							if (!leakingFXWindow.isVisible())
								leakingFXWindow.setVisible(true);
							if (!leakingFXWindow.isAlwaysOnTop())
								leakingFXWindow.setAlwaysOnTop(true);
							if (shouldRepaint)
								leakingFXWindow.repaint();
						}
						else if (leakingFXWindow.isVisible()) {
							leakingFXWindow.setAlwaysOnTop(false);
							leakingFXWindow.setVisible(false);
						}
					});
				}
			}
		}
	}

	private static void renderFlare(EntityVoidMonster ev, float par2) {
		double tick = (ev.ticksExisted + par2) / 1D;
		int idx = (int)(tick % 32);
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
			double dx = ev.posX - RenderManager.renderPosX;
			double dy = ev.posY - RenderManager.renderPosY;
			double dz = ev.posZ - RenderManager.renderPosZ;
			double[] angs = ReikaPhysicsHelper.cartesianToPolar(dx, dy, dz);
			GL11.glRotated(-angs[2] + 35 * 0, 0, 1, 0);
			GL11.glRotated(90 - angs[1] + 10 * 0, 1, 0, 0);
		}

		GL11.glTranslated(-0.125, 0, 0);

		double u = (idx % 8) / 8D;
		double v = (idx / 8) / 4D;
		double du = u + 1 / 8D;
		double dv = v + 1 / 4D;

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

	public static float getColorFactor() {
		return monsterScreenFactor;
	}

	public static float rampFog(float original) {
		float f = monsterScreenFactor;
		return getFogDistance() * f + (1 - f) * original;
	}

	public static void clearFX() {
		monsterScreenFactor = 0;
		monsterScreenFactor05 = 0;
		clouds.clear();
		AppIconManager.setCurrentTitle(null);
		AppIconManager.setCurrentIcon(null, null);
		//if (leakWindow != null && leakWindow.isVisible())
		//	SwingUtilities.invokeLater(() -> leakWindow.setVisible(false));
	}

}
