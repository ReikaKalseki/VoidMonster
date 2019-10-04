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

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.DragonOptions;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Auxiliary.Trackers.CommandableUpdateChecker;
import Reika.DragonAPI.Auxiliary.Trackers.DonatorController;
import Reika.DragonAPI.Auxiliary.Trackers.TickRegistry;
import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Base.DragonAPIMod.LoadProfiler.LoadPhase;
import Reika.DragonAPI.Instantiable.Data.Maps.MultiMap;
import Reika.DragonAPI.Instantiable.Data.Maps.MultiMap.CollectionType;
import Reika.DragonAPI.Instantiable.IO.ModLogger;
import Reika.DragonAPI.Instantiable.IO.SimpleConfig;
import Reika.DragonAPI.ModInteract.DeepInteract.ReikaThaumHelper;
import Reika.VoidMonster.Auxiliary.VoidMonsterBee;
import Reika.VoidMonster.Entity.EntityVoidMonster;
import Reika.VoidMonster.World.AmbientSoundGenerator;
import Reika.VoidMonster.World.MonsterGenerator;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import thaumcraft.api.aspects.Aspect;

@Mod( modid = "VoidMonster", name="Void Monster", version = "v@MAJOR_VERSION@@MINOR_VERSION@", certificateFingerprint = "@GET_FINGERPRINT@", dependencies="required-after:DragonAPI;before:Morph")
public class VoidMonster extends DragonAPIMod {

	@Instance("VoidMonster")
	public static VoidMonster instance = new VoidMonster();

	public static ModLogger logger;

	@SidedProxy(clientSide="Reika.VoidMonster.VoidClient", serverSide="Reika.VoidMonster.VoidCommon")
	public static VoidCommon proxy;

	public static final SimpleConfig config = new SimpleConfig(instance);

	private static final MultiMap<Integer, Integer> monsterList = new MultiMap(CollectionType.CONCURRENTSET);

	private int monsterSoundDelay;
	private float monsterDifficulty;
	private float voidFogStrength;

	@Override
	@EventHandler
	public void preload(FMLPreInitializationEvent evt) {
		this.startTiming(LoadPhase.PRELOAD);
		this.verifyInstallation();

		logger = new ModLogger(instance, false);
		if (DragonOptions.FILELOG.getState())
			logger.setOutput("**_Loading_Log.log");

		config.loadSubfolderedConfigFile(evt);
		config.loadDataFromFile(evt);
		config.finishReading();

		//ConfigMatcher.instance.addConfigList(this, config);

		monsterDifficulty = config.getFloat("Control Setup", "Void Monster Difficulty Factor", 1F);
		voidFogStrength = config.getFloat("Control Setup", "Void Fog Strength", 1F);

		MonsterGenerator.instance.loadConfig(config);

		proxy.registerSounds();

		monsterSoundDelay = Math.max(0, Math.min(1200, VoidMonster.config.getInteger("Control Setup", "Sound Interval in Ticks", 80)));

		this.basicSetup(evt);
		this.finishTiming();
	}

	@Override
	@EventHandler
	public void load(FMLInitializationEvent event) {
		this.startTiming(LoadPhase.LOAD);
		TickRegistry.instance.registerTickHandler(MonsterGenerator.instance);
		TickRegistry.instance.registerTickHandler(AmbientSoundGenerator.instance);

		int id = EntityRegistry.findGlobalUniqueEntityId();
		//if (DragonAPICore.isReikasComputer())
		//	EntityList.addMapping(EntityVoidMonster.class, "Void Monster", id, 0x000000, 0x555555);
		EntityRegistry.registerGlobalEntityID(EntityVoidMonster.class, "Void Monster", id);
		EntityRegistry.registerModEntity(EntityVoidMonster.class, "Void Monster", id, instance, 64, 20, true);

		proxy.registerRenderers();

		MinecraftForge.EVENT_BUS.register(VoidMonsterEvents.instance);

		DonatorController.instance.registerMod(this, DonatorController.reikaURL);
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

		if (ModList.FORESTRY.isLoaded()) {
			try {
				VoidMonsterBee bee = new VoidMonsterBee();
			}
			catch (IncompatibleClassChangeError e) {
				e.printStackTrace();
				logger.logError("Could not add Forestry integration. Check your versions; if you are up-to-date with both mods, notify Reika.");
			}
			catch (Exception e) {
				e.printStackTrace();
				logger.logError("Could not add Forestry integration. Check your versions; if you are up-to-date with both mods, notify Reika.");
			}
			catch (LinkageError e) {
				e.printStackTrace();
				logger.logError("Could not add Forestry integration. Check your versions; if you are up-to-date with both mods, notify Reika.");
			}
		}

		this.finishTiming();
	}

	public static boolean allowedIn(World world) {
		return MonsterGenerator.instance.isDimensionAllowed(world);
	}

	public int getMonsterSoundDelay() {
		return monsterSoundDelay;
	}

	public float getMonsterDifficulty() {
		return monsterDifficulty;
	}

	public float getFogStrength() {
		return voidFogStrength;
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

	@Override
	public File getConfigFolder() {
		return config.getConfigFolder();
	}

	public static void registerExistingMonster(EntityVoidMonster e) {
		monsterList.addValue(e.worldObj.provider.dimensionId, e.getEntityId());
	}

	public static Collection<EntityVoidMonster> getCurrentMonsterList(World world) {
		Iterator<Integer> it = monsterList.get(world.provider.dimensionId).iterator();
		ArrayList<EntityVoidMonster> li = new ArrayList();
		while (it.hasNext()) {
			int id = it.next();
			Entity e = world.getEntityByID(id);
			if (e instanceof EntityVoidMonster)
				li.add((EntityVoidMonster)e);
			else
				it.remove();
		}
		return li;

		/*
		 * for (Entity e : ((List<Entity>)world.loadedEntityList)) {
			if (e instanceof EntityVoidMonster) {
				return false;
			}
		}
		 */
	}
}
