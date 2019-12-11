/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster.API;

import java.lang.reflect.Method;
import java.util.Collection;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class MonsterAPI {

	private static Class modClass;
	private static Class monster;
	private static Class drops;

	private static Method add;
	private static Method getList;

	static {
		try {
			modClass = Class.forName("Reika.VoidMonster.VoidMonster");
			monster = Class.forName("Reika.VoidMonster.Entity.EntityVoidMonster");
			drops = Class.forName("Reika.VoidMonster.Auxiliary.VoidMonsterDrops");

			getList = modClass.getMethod("getCurrentMonsterList", World.class);
			add = drops.getMethod("addDrop", ItemStack.class, int.class, int.class, double.class);
		}
		catch (Exception e) {
			throw new RuntimeException("Could not access own internal classes! Is there an API version mismatch?", e);
		}
	}

	public static void addDrop(ItemStack is, int minDrops, int maxDrops) {
		addDrop(is, minDrops, maxDrops, 0);
	}

	public static void addDrop(ItemStack is, int minDrops, int maxDrops, double requiredDifficulty) {
		try {
			add.invoke(null, is, minDrops, maxDrops, requiredDifficulty);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addDrop(Item item, int minDrops, int maxDrops) {
		addDrop(new ItemStack(item), minDrops, maxDrops);
	}

	public static void addDrop(Block b, int minDrops, int maxDrops) {
		addDrop(new ItemStack(b), minDrops, maxDrops);
	}

	public static void addDrop(ItemStack is) {
		addDrop(is, 1, 1);
	}

	public static void addDrop(Item item) {
		addDrop(new ItemStack(item), 1, 1);
	}

	public static void addDrop(Block b) {
		addDrop(new ItemStack(b), 1, 1);
	}

	public static Entity getNearestMonster(World world, double x, double y, double z) {
		try {
			Collection<Entity> c = (Collection<Entity>)getList.invoke(null, world);
			double dist = Double.POSITIVE_INFINITY;
			Entity ret = null;
			for (Entity e : c) {
				double d = e.getDistanceSq(x, y, z);
				if (ret == null || d < dist) {
					dist = d;
					ret = e;
				}
			}
			return ret;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
