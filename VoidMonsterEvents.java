/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.AllowDespawn;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Instantiable.Event.ConfigReloadEvent;
import Reika.DragonAPI.Instantiable.Event.Client.BossColorEvent;
import Reika.DragonAPI.Instantiable.Event.Client.EntityRenderingLoopEvent;
import Reika.DragonAPI.Instantiable.Event.Client.FarClippingPlaneEvent;
import Reika.DragonAPI.Instantiable.Event.Client.FogDistanceEvent;
import Reika.DragonAPI.Instantiable.Event.Client.SkyColorEvent;
import Reika.DragonAPI.Libraries.IO.ReikaColorAPI;
import Reika.VoidMonster.Entity.EntityVoidMonster;
import Reika.VoidMonster.World.MonsterGenerator;

import com.xcompwiz.mystcraft.api.event.LinkEvent;

import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class VoidMonsterEvents {

	public static final VoidMonsterEvents instance = new VoidMonsterEvents();

	private VoidMonsterEvents() {

	}

	@SubscribeEvent
	public void reloadConfig(ConfigReloadEvent d) {
		MonsterGenerator.instance.loadConfig(VoidMonster.config);
	}

	@SubscribeEvent
	@ModDependent(ModList.MYSTCRAFT)
	public void noLinking(LinkEvent.LinkEventAllow evt) {
		if (evt.entity instanceof EntityVoidMonster)
			evt.setCanceled(true);
	}

	@SubscribeEvent
	public void disallowDespawn(AllowDespawn d) {
		EntityLivingBase e = d.entityLiving;
		if (e instanceof EntityVoidMonster)
			d.setResult(Result.DENY);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void dynamicFog(FogDistanceEvent evt) {
		evt.fogDistance = MonsterFX.rampFog(evt.fogDistance);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void dynamicFog(FarClippingPlaneEvent evt) {
		evt.farClippingPlaneDistance = MonsterFX.rampFog(evt.farClippingPlaneDistance);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void dynamicFog(EntityViewRenderEvent.FogColors evt) {
		//if (MonsterFX.isMonsterVisible()) {
		//	evt.red = evt.green = evt.blue = 0.03125F*1.5F;
		//}
		int c0 = ReikaColorAPI.RGBtoHex((int)(evt.red*255), (int)(evt.green*255), (int)(evt.blue*255));
		int c1 = ReikaColorAPI.GStoHex((int)(255*0.03125F*1.5F));
		int c = MonsterFX.rampColor(c1, c0);//ReikaColorAPI.mixColors(c1, c0, MathHelper.clamp_float(Minecraft.getMinecraft().entityRenderer.bossColorModifier, 0, 1));
		evt.red = ReikaColorAPI.getRed(c)/255F;
		evt.green = ReikaColorAPI.getGreen(c)/255F;
		evt.blue = ReikaColorAPI.getBlue(c)/255F;
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void dynamicFog(BossColorEvent evt) {
		//if (MonsterFX.isMonsterVisible()) {
		//evt.red = evt.green = evt.blue = evt.isLightmap ? 0.1F : 0.0F;
		//}
		//int c0 = ReikaColorAPI.RGBtoHex((int)(evt.red*255), (int)(evt.green*255), (int)(evt.blue*255));
		//int c1 = evt.isLightmap ? 0x101010 : 0x000000;
		//int c = MonsterFX.rampColor(c1, c0);//ReikaColorAPI.mixColors(c1, c0, MathHelper.clamp_float(Minecraft.getMinecraft().entityRenderer.bossColorModifier, 0, 1));
		//evt.red = ReikaColorAPI.getRed(c)/255F;
		//evt.green = ReikaColorAPI.getGreen(c)/255F;
		//evt.blue = ReikaColorAPI.getBlue(c)/255F;
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void dynamicFog(SkyColorEvent evt) {
		//if (MonsterFX.isMonsterVisible()) {
		//ReikaJavaLibrary.pConsole(Integer.toHexString(evt.color));
		evt.color = MonsterFX.rampColor(0x101010, evt.color);
		//}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onRenderLoop(EntityRenderingLoopEvent evt) {
		MonsterFX.onRenderLoop();
	}

	/*
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void clearCachedMonster(BossColorResetEvent evt) {
		//MonsterFX.clearCache();
	}
	 */

	/*
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void clearCachedMonster(ProfileEvent evt) {
		if (evt.sectionName.equals("entities")) {
			MonsterFX.clearCache();
		}
	}
	 */

}
