/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster.World;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.MinecraftForge;
import Reika.VoidMonster.Entity.EntityVoidMonster;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class MonsterGenerator implements ITickHandler {

	private final ArrayList<Integer> dimIDs = new ArrayList();
	private final Random rand = new Random();

	public MonsterGenerator() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void spawn(World world, EntityPlayer ep) {
		EntityVoidMonster ev = null;
		switch(world.provider.dimensionId) {
		case 0:
			ev = new EntityVoidMonster(world);
			break;
		case 1:
			//no spawn since the end is open and the monster could escape
			break;
		case -1:
			ev = new EntityVoidMonster(world).setNether();
			break;
		default:
			ev = new EntityVoidMonster(world);
			break;
		}
		if (ev != null) {
			ev.forceSpawn = true;
			ev.setLocationAndAngles(ep.posX, -10, ep.posZ, 0, 0);
			world.spawnEntityInWorld(ev);
			dimIDs.add(world.provider.dimensionId);
		}
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		World world = (World)tickData[0];
		if (world != null) {
			if (this.canSpawnIn(world)) {
				this.spawn(world, (EntityPlayer)world.playerEntities.get(0));
			}
		}
	}

	private boolean canSpawnIn(World world) {
		if (dimIDs.contains(world.provider.dimensionId) || world.playerEntities.isEmpty())
			return false;
		if (world.getWorldInfo().getTerrainType() == WorldType.FLAT)
			return false;
		for (int i = 0; i < world.loadedEntityList.size(); i++) {
			Entity e = (Entity)world.loadedEntityList.get(i);
			if (e instanceof EntityVoidMonster) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {

	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.WORLD);
	}

	@Override
	public String getLabel() {
		return "Void Monster";
	}

}
