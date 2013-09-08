/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster;

import java.util.ArrayList;

import net.minecraft.world.World;

public class MonsterManager {

	private static final ArrayList<Integer> worlds = new ArrayList<Integer>();

	public static void registerSpawn(World world) {
		worlds.add(world.provider.dimensionId);
	}

	public static boolean canSpawnMonsterInWorld(World world) {
		return false;//return !worlds.contains(world.provider.dimensionId);
	}

	private static void readWorldList() {
		//TODO
	}

	static {
		readWorldList();
	}

}
