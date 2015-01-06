/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster.API;

import java.lang.reflect.Method;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class MonsterAPI {

	private static final Class monster;
	private static final Method add;

	static {
		Class m = null;
		Method a = null;
		try {
			m = Class.forName("Reika.VoidMonster.EntityVoidMonster");
			a = m.getMethod("addDrop", ItemStack.class, int.class, int.class);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		monster = m;
		add = a;
	}

	public static void addDrop(ItemStack is, int minDrops, int maxDrops) {
		try {
			add.invoke(null, is, minDrops, maxDrops);
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

}
