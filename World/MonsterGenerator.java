/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster.World;

import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Auxiliary.TickRegistry.TickHandler;
import Reika.DragonAPI.Auxiliary.TickRegistry.TickType;
import Reika.DragonAPI.ModInteract.ExtraUtilsHandler;
import Reika.VoidMonster.Entity.EntityVoidMonster;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class MonsterGenerator implements TickHandler {

	private final Random rand = new Random();
	private final ArrayList<Integer> bannedDimensions = new ArrayList();

	public MonsterGenerator() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void spawn(World world, EntityPlayer ep) {
		EntityVoidMonster ev = new EntityVoidMonster(world);
		if (world.provider.isHellWorld)
			ev.setNether();
		ev.forceSpawn = true;
		ev.setLocationAndAngles(ep.posX, -10, ep.posZ, 0, 0);
		world.spawnEntityInWorld(ev);
	}

	@Override
	public void tick(Object... tickData) {
		World world = (World)tickData[0];
		if (world != null) {
			if (this.canSpawnIn(world)) {
				this.spawn(world, (EntityPlayer)world.playerEntities.get(0));
			}
		}
	}

	private boolean canSpawnIn(World world) {
		if (world.playerEntities.isEmpty())
			return false;
		if (world.getWorldInfo().getTerrainType() == WorldType.FLAT)
			return false;
		for (int i = 0; i < world.loadedEntityList.size(); i++) {
			Entity e = (Entity)world.loadedEntityList.get(i);
			if (e instanceof EntityVoidMonster) {
				return false;
			}
		}

		return this.isHardcodedAllowed(world.provider.dimensionId) || !bannedDimensions.contains(world.provider.dimensionId);
	}

	@Override
	public TickType getType() {
		return TickType.WORLD;
	}

	@Override
	public Phase getPhase() {
		return Phase.START;
	}

	@Override
	public String getLabel() {
		return "Void Monster";
	}

	public void banDimensions(ArrayList<Integer> dimensions) {
		for (int i = 0; i < dimensions.size(); i++) {
			int id = dimensions.get(i);
			bannedDimensions.add(id);/*
			if (this.isHardcodedAllowed(id))
				VoidMonster.logger.log("Blacklist reqest for dimension ID "+id+", but this dimension may not be blacklisted.");
			else
				VoidMonster.logger.log("Dimension ID "+id+" blacklisted for monster spawn.");*/
		}
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

}