/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster.Auxiliary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.lang3.tuple.ImmutablePair;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Instantiable.ItemDrop;
import Reika.DragonAPI.Instantiable.IO.CustomRecipeList;
import Reika.DragonAPI.Instantiable.IO.LuaBlock;
import Reika.DragonAPI.Libraries.ReikaEnchantmentHelper;
import Reika.DragonAPI.Libraries.ReikaEntityHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.VoidMonster.VoidMonster;
import Reika.VoidMonster.Entity.EntityVoidMonster;


public class VoidMonsterDrops {

	private static final Random rand = new Random();

	private static final ArrayList<VoidMonsterDrop> drops = new ArrayList();

	static {
		addDrop(Items.diamond, 2, 8, 0.8);
		addDrop(Items.ghast_tear, 0.6);
		addDrop(Items.speckled_melon, 2, 5, 0);
		addDrop(Items.emerald, 2, 6, 0.4);
		addDrop(Items.ender_pearl, 1, 3, 0.2);
		addDrop(Items.ender_eye, 1, 3, 0.25);
		addDrop(Items.fire_charge, 2, 8, 0);
		addDrop(Items.nether_wart, 8, 22, 0);
		addDrop(Items.nether_star, 1, 2, 1);
		addDrop(Blocks.obsidian, 6, 16, 0.6);
		addDrop(Items.gunpowder, 8, 12, 0.3);
	}

	public static void loadCustomDrops() {
		CustomRecipeList crl = new DropList(VoidMonster.instance, "");
		if (crl.load()) {
			for (LuaBlock lb : crl.getEntries()) {
				Exception e = null;
				boolean flag = false;
				try {
					flag = addCustomDrop(lb, crl);
				}
				catch (Exception ex) {
					e = ex;
					flag = false;
				}
				if (flag) {
					VoidMonster.logger.log("Loaded custom monster drop '"+lb.getString("type")+"'");
				}
				else {
					VoidMonster.logger.logError("Could not load custom monster drop '"+lb.getString("type")+"'");
					if (e != null)
						e.printStackTrace();
				}
			}
		}
		else {
			crl.createFolders();
			LuaBlock ex = crl.createExample("customDrop1");
			crl.writeItem(ex, ReikaItemHelper.bonemeal);
			ex.putData("min", 3);
			ex.putData("max", 14);
			ex = crl.createExample("customDrop2");
			crl.writeItem(ex, new ItemStack(Items.redstone));
			ex.putData("min", 1);
			ex.putData("max", 6);
			ex.putData("required_difficulty", 0.4);
			ex = crl.createExample("customDrop3");
			ItemStack is = new ItemStack(Items.diamond_axe);
			ReikaEnchantmentHelper.applyEnchantment(is, Enchantment.fortune, 2);
			crl.writeItem(ex, is);
			ex.putData("required_difficulty", 1.5);
			crl.createExampleFile();
		}
	}

	private static boolean addCustomDrop(LuaBlock lb, CustomRecipeList crl) {
		String s = lb.getString("item");
		ItemStack is = crl.parseItemString(s, lb.getChild("item_nbt"), false);
		if (is == null || is.getItem() == null)
			throw new IllegalArgumentException("No such item '"+s+"'");
		double d = lb.getDouble("required_difficulty"); //returns 0 if not present
		int min = Math.max(1, lb.getInt("min"));
		int max = Math.max(1, lb.getInt("max"));
		addDrop(is, min, max, (float)d);
		return true;
	}

	public static void addDrop(Item i, double d) {
		addDrop(i, 1, 1, d);
	}

	public static void addDrop(Item i, int min, int max, double d) {
		addDrop(new ItemStack(i), min, max, d);
	}

	public static void addDrop(Block b, double d) {
		addDrop(b, 1, 1, d);
	}

	public static void addDrop(Block b, int min, int max, double d) {
		addDrop(new ItemStack(b), min, max, d);
	}

	public static void addDrop(ItemStack is, double d) {
		addDrop(is, 1, 1, d);
	}

	public static void addDrop(ItemStack is, int min, int max, double d) {
		VoidMonsterDrop it = new VoidMonsterDrop(is, min, max, d);
		if (!drops.contains(it))
			drops.add(it);
		logItem(is);
	}

	private static void logItem(ItemStack is) {
		String n = is.getDisplayName();
		if (is.isItemEnchanted()) {
			n = n+", Enchanted with:";
			HashMap<Enchantment, Integer> map = ReikaEnchantmentHelper.getEnchantments(is);
			for (Entry<Enchantment, Integer> e : map.entrySet()) {
				n = n+" "+e.getKey().getTranslatedName(e.getValue())+";";
			}
		}
		VoidMonster.instance.getModLogger().log("Adding monster drop "+n);
	}

	public static void doDrops(EntityVoidMonster e) {
		for (VoidMonsterDrop d : drops) {
			if (d.shouldDrop(e)) {
				EntityItem ei = d.drop(e, e.getDifficulty());
				ReikaEntityHelper.setInvulnerable(ei, true);
			}
		}
		dropEnchantedBooks(e);
	}

	private static void dropEnchantedBooks(EntityVoidMonster e) {
		ArrayList<ImmutablePair<Enchantment, Integer>> li = new ArrayList();
		int n = 1+2*MathHelper.ceiling_float_int(Math.max(1, 1+e.getDifficulty()));
		for (int i = 0; i < n; i++) {
			Enchantment en = ReikaEnchantmentHelper.getRandomEnchantment(null, false);
			int max = Math.max(1, (int)(en.getMaxLevel()*Math.min(1, e.getDifficulty())));
			int l = 1+rand.nextInt(max);
			li.add(new ImmutablePair(en, l));
		}
		for (ImmutablePair<Enchantment, Integer> p : li) {
			ReikaItemHelper.dropItem(e.worldObj, e.posX, e.posY+0.25, e.posZ, ReikaEnchantmentHelper.getEnchantedBook(p.left, p.right));
		}
	}

	private static class DropList extends CustomRecipeList {

		public DropList(DragonAPIMod mod, String type) {
			super(mod, type);
		}

		@Override
		protected String getFolderName() {
			return "CustomDrops";
		}

		@Override
		protected String getExtension() {
			return ".drops";
		}

	}

	private static class VoidMonsterDrop extends ItemDrop {

		private final double requiredDifficulty;

		private VoidMonsterDrop(ItemStack is, int min, int max, double minDifficulty) {
			super(is, min, max);
			requiredDifficulty = minDifficulty;
		}

		public boolean shouldDrop(EntityVoidMonster e) {
			return e.getDifficulty() >= requiredDifficulty;
		}

	}

}
