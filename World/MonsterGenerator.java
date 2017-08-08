/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.MinecraftForge;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Auxiliary.Trackers.TickRegistry.TickHandler;
import Reika.DragonAPI.Auxiliary.Trackers.TickRegistry.TickType;
import Reika.DragonAPI.Instantiable.IO.SimpleConfig;
import Reika.DragonAPI.Libraries.ReikaPlayerAPI;
import Reika.DragonAPI.ModInteract.ItemHandlers.ExtraUtilsHandler;
import Reika.VoidMonster.Entity.EntityVoidMonster;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class MonsterGenerator implements TickHandler {

	public static final MonsterGenerator instance = new MonsterGenerator();

	private final Random rand = new Random();

	private final HashMap<Integer, Boolean> APIRules = new HashMap();
	private final HashMap<Integer, Boolean> spawnRules = new HashMap();

	private MonsterGenerator() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void spawn(World world, EntityPlayer ep) {
		EntityVoidMonster ev = new EntityVoidMonster(world);
		if (world.provider.isHellWorld)
			ev.setNether();
		ev.forceSpawn = true;
		ev.setLocationAndAngles(ep.posX, ev.isNetherVoid() ? 260 : -10, ep.posZ, 0, 0);
		world.spawnEntityInWorld(ev);
	}

	@Override
	public void tick(TickType type, Object... tickData) {
		World world = (World)tickData[0];
		if (world != null) {
			if (this.canSpawnIn(world)) {
				EntityPlayer ep = this.getRandomPlayer(world);
				if (ep != null) {
					this.spawn(world, ep);
				}
			}
		}
	}

	private EntityPlayer getRandomPlayer(World world) {
		ArrayList<EntityPlayer> li = new ArrayList(world.playerEntities);
		int idx = rand.nextInt(li.size());
		EntityPlayer ep = li.get(idx);
		while (ReikaPlayerAPI.isFake(ep)) {
			li.remove(idx);
			if (li.isEmpty())
				return null;
			idx = rand.nextInt(li.size());
			ep = li.get(idx);
		}
		return ep;
	}

	private boolean canSpawnIn(World world) {
		if (world.playerEntities.isEmpty())
			return false;
		if (world.getWorldInfo().getTerrainType() == WorldType.FLAT)
			return false;
		for (Entity e : ((List<Entity>)world.loadedEntityList)) {
			if (e instanceof EntityVoidMonster) {
				return false;
			}
		}

		return this.isHardcodedAllowed(world.provider.dimensionId) || this.isDimensionAllowed(world);
	}

	public boolean isDimensionAllowed(World world) {
		Boolean get = spawnRules.get(world.provider.dimensionId);
		return get == null || get.booleanValue();
	}

	@Override
	public EnumSet<TickType> getType() {
		return EnumSet.of(TickType.WORLD);
	}

	@Override
	public boolean canFire(Phase p) {
		return p == Phase.START;
	}

	@Override
	public String getLabel() {
		return "Void Monster";
	}

	public void setDimensions(Collection<Integer> dimensions, boolean allow) {
		for (int id : dimensions) {
			this.setDimension(id, allow);
		}
	}

	public void setDimensions(HashMap<Integer, Boolean> dimensions) {
		for (int id : dimensions.keySet()) {
			this.setDimension(id, dimensions.get(id));
		}
	}

	public void setDimensionRuleAPI(int id, boolean allow) {
		this.setDimension(id, allow);
		APIRules.put(id, allow);
	}

	public void setDimension(int id, boolean allow) {
		spawnRules.put(id, allow);
	}

	private boolean isHardcodedAllowed(int id) {
		if (id == 0)
			return true;
		if (id == -1)
			return true;
		if (ModList.EXTRAUTILS.isLoaded() && id == ExtraUtilsHandler.getInstance().darkID)
			return true;
		return false;
	}

	public void loadConfig(SimpleConfig config) {
		spawnRules.clear();
		ArrayList<Integer> dimensions = config.getIntList("Control Setup", "Banned Dimensions", 1, -112);
		boolean allow = config.getBoolean("Control Setup", "Dimension list is actually whitelist", false);
		this.setDimensions(dimensions, allow);
		this.setDimensions(APIRules);
	}

}
