/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster;

import java.net.URL;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.AllowDespawn;
import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.Auxiliary.DonatorController;
import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Instantiable.IO.ModLogger;
import Reika.DragonAPI.Instantiable.IO.SimpleConfig;
import Reika.DragonAPI.Libraries.ReikaRegistryHelper;
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
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod( modid = "VoidMonster", name="Void Monster", version="beta", certificateFingerprint = "@GET_FINGERPRINT@", dependencies="required-after:DragonAPI")
@NetworkMod(clientSideRequired = true, serverSideRequired = true/*,
clientPacketHandlerSpec = @SidedPacketHandler(channels = { "VoidMonsterData" }, packetHandler = ClientPackets.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = { "VoidMonsterData" }, packetHandler = ServerPackets.class)*/)

public class VoidMonster extends DragonAPIMod {

	@Instance("VoidMonster")
	public static VoidMonster instance = new VoidMonster();

	public static ModLogger logger;

	@SidedProxy(clientSide="Reika.VoidMonster.VoidClient", serverSide="Reika.VoidMonster.VoidCommon")
	public static VoidCommon proxy;

	public static final SimpleConfig config = new SimpleConfig(instance);

	@Override
	@EventHandler
	public void preload(FMLPreInitializationEvent evt) {
		//config.loadSubfolderedConfigFile(evt);
		//config.loadDataFromFile(evt);
		//config.finishReading();

		MinecraftForge.EVENT_BUS.register(this);

		ReikaRegistryHelper.setupModData(instance, evt);
		ReikaRegistryHelper.setupVersionChecking(evt);

		logger = new ModLogger(instance, true, false, false);
	}

	@Override
	@EventHandler
	public void load(FMLInitializationEvent event) {
		TickRegistry.registerTickHandler(new MonsterGenerator(), Side.SERVER);
		TickRegistry.registerTickHandler(new AmbientSoundGenerator(), Side.CLIENT);

		int id = EntityRegistry.findGlobalUniqueEntityId();
		if (DragonAPICore.isReikasComputer())
			EntityList.addMapping(EntityVoidMonster.class, "Void Monster", id, 0x000000, 0x555555);
		EntityRegistry.registerGlobalEntityID(EntityVoidMonster.class, "Void Monster", id);
		EntityRegistry.registerModEntity(EntityVoidMonster.class, "Void Monster", id, instance, 64, 20, true);

		proxy.registerRenderers();

		DonatorController.instance.addDonation(instance, "Seiryn", 10.00F);
	}

	@Override
	@EventHandler
	public void postload(FMLPostInitializationEvent evt) {

	}

	@ForgeSubscribe
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
		return DragonAPICore.getReikaForumPage(instance);
	}

	@Override
	public boolean hasWiki() {
		return false;
	}

	@Override
	public URL getWiki() {
		return null;
	}

	@Override
	public boolean hasVersion() {
		return false;
	}

	@Override
	public String getVersionName() {
		return null;
	}

	@Override
	public ModLogger getModLogger() {
		return logger;
	}
}
