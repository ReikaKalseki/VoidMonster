package Reika.VoidMonster;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

import org.apache.commons.lang3.tuple.ImmutablePair;

import Reika.DragonAPI.Instantiable.ItemDrop;
import Reika.DragonAPI.Instantiable.IO.CustomRecipeList;
import Reika.DragonAPI.Instantiable.IO.LuaBlock;
import Reika.DragonAPI.Libraries.ReikaEnchantmentHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.VoidMonster.Entity.EntityVoidMonster;


public class VoidMonsterDrops {

	private static final Random rand = new Random();

	private static final ArrayList<ItemDrop> drops = new ArrayList();

	static {
		addDrop(Items.diamond, 2, 8);
		addDrop(Items.ghast_tear);
		addDrop(Items.speckled_melon, 2, 5);
		addDrop(Items.emerald, 2, 6);
		addDrop(Items.ender_pearl, 1, 3);
		addDrop(Items.ender_eye, 1, 3);
		addDrop(Items.fire_charge, 2, 8);
		addDrop(Items.nether_wart, 8, 22);
		addDrop(Items.nether_star, 1, 2);
		addDrop(Blocks.obsidian, 6, 16);
		addDrop(Items.gunpowder, 8, 12);
	}

	public static void loadCustomDrops() {
		CustomRecipeList crl = new CustomRecipeList(VoidMonster.instance, "drops");
		crl.load();
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

	private static boolean addCustomDrop(LuaBlock lb, CustomRecipeList crl) {
		String s = lb.getString("item");
		ItemStack is = crl.parseItemString(s, lb.getChild("item_nbt"), false);
		if (is == null || is.getItem() == null)
			throw new IllegalArgumentException("No such item '"+s+"'");
		addDrop(is);
		return true;
	}

	public static void addDrop(Item i) {
		addDrop(i, 1, 1);
	}

	public static void addDrop(Item i, int min, int max) {
		addDrop(new ItemStack(i), min, max);
	}

	public static void addDrop(Block b) {
		addDrop(b, 1, 1);
	}

	public static void addDrop(Block b, int min, int max) {
		addDrop(new ItemStack(b), min, max);
	}

	public static void addDrop(ItemStack is) {
		addDrop(is, 1, 1);
	}

	public static void addDrop(ItemStack is, int min, int max) {
		ItemDrop it = new ItemDrop(is, min, max);
		if (!drops.contains(it))
			drops.add(it);
		VoidMonster.instance.getModLogger().log("Adding monster drop "+is.getDisplayName());
	}

	public static void addDrop(ItemStack is, Enchantment e, int level) {
		ItemDrop it = new ItemDrop(is, 1, 1);
		it.enchant(e, level);
		if (!drops.contains(it))
			drops.add(it);
		VoidMonster.instance.getModLogger().log("Adding monster drop "+is.getDisplayName()+", enchanted with "+e.getTranslatedName(level));
	}

	public static void addDrop(ItemStack is, int min, int max, Enchantment e, int level) {
		ItemDrop it = new ItemDrop(is, min, max);
		it.enchant(e, level);
		if (!drops.contains(it))
			drops.add(it);
		VoidMonster.instance.getModLogger().log("Adding monster drop "+is.getDisplayName()+", enchanted with "+e.getName()+" "+level);
	}

	public static void doDrops(EntityVoidMonster e) {
		for (int i = 0; i < drops.size(); i++) {
			ItemDrop it = drops.get(i);
			it.drop(e);
		}
		dropEnchantedBooks(e);
	}

	private static void dropEnchantedBooks(EntityVoidMonster e) {
		ArrayList<ImmutablePair<Enchantment, Integer>> li = new ArrayList();
		int n = 1+2*MathHelper.ceiling_float_int(Math.max(1, 1+e.getDifficulty()));
		for (int i = 0; i < n; i++) {
			Enchantment en = ReikaEnchantmentHelper.getRandomEnchantment(null, false);
			int l = 1+rand.nextInt(en.getMaxLevel());
			li.add(new ImmutablePair(en, l));
		}
		for (ImmutablePair<Enchantment, Integer> p : li) {
			ReikaItemHelper.dropItem(e.worldObj, e.posX, e.posY+0.25, e.posZ, ReikaEnchantmentHelper.getEnchantedBook(p.left, p.right));
		}
	}

}
