/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.AllowDespawn;
import thaumcraft.api.aspects.Aspect;
import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Auxiliary.Trackers.CommandableUpdateChecker;
import Reika.DragonAPI.Auxiliary.Trackers.DonatorController;
import Reika.DragonAPI.Auxiliary.Trackers.TickRegistry;
import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Base.DragonAPIMod.LoadProfiler.LoadPhase;
import Reika.DragonAPI.Instantiable.IO.ModLogger;
import Reika.DragonAPI.Instantiable.IO.SimpleConfig;
import Reika.DragonAPI.ModInteract.ReikaThaumHelper;
import Reika.VoidMonster.Entity.EntityVoidMonster;
import Reika.VoidMonster.World.AmbientSoundGenerator;
import Reika.VoidMonster.World.MonsterGenerator;

import com.xcompwiz.mystcraft.api.event.LinkEvent;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod( modid = "VoidMonster", name="Void Monster", version="beta", certificateFingerprint = "@GET_FINGERPRINT@", dependencies="required-after:DragonAPI")

public class VoidMonster extends DragonAPIMod {

	@Instance("VoidMonster")
	public static VoidMonster instance = new VoidMonster();

	public static ModLogger logger;

	@SidedProxy(clientSide="Reika.VoidMonster.VoidClient", serverSide="Reika.VoidMonster.VoidCommon")
	public static VoidCommon proxy;

	public static final SimpleConfig config = new SimpleConfig(instance);

	private static MonsterGenerator gen = new MonsterGenerator();

	@Override
	@EventHandler
	public void preload(FMLPreInitializationEvent evt) {
		this.startTiming(LoadPhase.PRELOAD);
		this.verifyVersions();
		logger = new ModLogger(instance, false);

		config.loadSubfolderedConfigFile(evt);
		config.loadDataFromFile(evt);
		ArrayList<Integer> dimensions = config.getIntList("Control Setup", "Banned Dimensions", 1);
		config.finishReading();

		gen.banDimensions(dimensions);

		this.basicSetup(evt);
		this.finishTiming();
	}

	@Override
	@EventHandler
	public void load(FMLInitializationEvent event) {
		this.startTiming(LoadPhase.LOAD);
		TickRegistry.instance.registerTickHandler(gen, Side.SERVER);
		TickRegistry.instance.registerTickHandler(new AmbientSoundGenerator(), Side.CLIENT);

		int id = EntityRegistry.findGlobalUniqueEntityId();
		//if (DragonAPICore.isReikasComputer())
		//	EntityList.addMapping(EntityVoidMonster.class, "Void Monster", id, 0x000000, 0x555555);
		EntityRegistry.registerGlobalEntityID(EntityVoidMonster.class, "Void Monster", id);
		EntityRegistry.registerModEntity(EntityVoidMonster.class, "Void Monster", id, instance, 64, 20, true);

		proxy.registerRenderers();

		DonatorController.instance.addDonation(instance, "Seiryn", 10.00F);
		this.finishTiming();
	}

	@Override
	@EventHandler
	public void postload(FMLPostInitializationEvent evt) {
		this.startTiming(LoadPhase.POSTLOAD);

		if (ModList.THAUMCRAFT.isLoaded()) {
			Object[] asp = {
					Aspect.ELDRITCH, 40,
					Aspect.VOID, 40,
					Aspect.DARKNESS, 40,
					Aspect.BEAST, 25,
					Aspect.DEATH, 25,
					Aspect.ENTROPY, 20,
					Aspect.AURA, 5,
					Aspect.ARMOR, 10,
					Aspect.CRYSTAL, 10
			};
			ReikaThaumHelper.addAspects(EntityVoidMonster.class, asp);
		}
		if (ModList.MINEFACTORY.isLoaded()) {
			try {
				Class c = Class.forName("powercrystals.minefactoryreloaded.MFRRegistry");
				Method m = c.getMethod("registerSafariNetBlacklist", Class.class);
				m.invoke(null, EntityVoidMonster.class);
				m = c.getMethod("registerAutoSpawnerBlacklistClass", Class.class);
				m.invoke(null, EntityVoidMonster.class);
			}
			catch (Exception e) {
				logger.logError("Could not blacklist Void Monster from MFR Safari Net!");
				e.printStackTrace();
			}
		}
		if (ModList.ENDERIO.isLoaded()) {
			try {
				Class c = Class.forName("crazypants.enderio.config.Config");
				Field f = c.getField("soulVesselBlackList");
				String[] arr = (String[])f.get(null);
				String[] newsg = new String[arr.length+1];
				System.arraycopy(arr, 0, newsg, 0, arr.length);
				newsg[arr.length] = "Void Monster";
				f.set(null, newsg);
			}
			catch (Exception e) {
				logger.logError("Could not blacklist Void Monster from EnderIO spawner!");
				e.printStackTrace();
			}
		}
		this.finishTiming();
	}

	public static boolean blacklistedIn(World world) {
		return gen.isDimensionBanned(world);
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

	@Override
	public String getDisplayName() {
		return "Void Monster";
	}

	@Override
	public String getModAuthorName() {
		return "Reika";
	}

	@Override
	public URL getDocumentationSite() {
		return DragonAPICore.getReikaForumPage();
	}

	@Override
	public String getWiki() {
		return null;
	}

	@Override
	public String getUpdateCheckURL() {
		return CommandableUpdateChecker.reikaURL;
	}

	@Override
	public ModLogger getModLogger() {
		return logger;
	}
}
