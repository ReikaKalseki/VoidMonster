/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster.ModInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import com.xcompwiz.mystcraft.api.hook.SymbolValuesAPI;
import com.xcompwiz.mystcraft.api.symbol.IAgeSymbol;
import com.xcompwiz.mystcraft.api.word.WordData;
import com.xcompwiz.mystcraft.api.world.AgeDirector;

import net.minecraft.world.World;

import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.Java.ReikaStringParser;
import Reika.DragonAPI.ModInteract.DeepInteract.ReikaMystcraftHelper;
import Reika.DragonAPI.ModInteract.DeepInteract.ReikaMystcraftHelper.APISegment;
import Reika.DragonAPI.ModInteract.DeepInteract.ReikaMystcraftHelper.MystcraftPageRegistry;


public class VoidMystPages implements MystcraftPageRegistry {

	public static final VoidMystPages instance = new VoidMystPages();

	private final HashMap<Integer, Boolean> dimCache = new HashMap();

	private VoidPage page;

	private VoidMystPages() {

	}

	@Override
	public void register() {
		page = new VoidPage("No Void Monster", 100, 4, 0.0625F);
		ReikaMystcraftHelper.setPageRank(page, page.itemRank);
		ReikaMystcraftHelper.setRandomAgeWeight(page, page.randomWeight);
		ReikaMystcraftHelper.registerAgeSymbol(page);

		SymbolValuesAPI api = ReikaMystcraftHelper.getAPI(APISegment.SYMBOLVALUES);
		if (api != null) {
			api.setSymbolIsPurchasable(page, false);
		}
	}

	public boolean existsInWorld(World age) {
		if (page == null)
			return false;
		Boolean b = dimCache.get(age.provider.dimensionId);
		if (b == null) {
			b = ReikaMystcraftHelper.isMystAge(age) && ReikaMystcraftHelper.isSymbolPresent(age, page.identifier());
			dimCache.put(age.provider.dimensionId, b);
		}
		return b;
	}

	private static class VoidPage implements IAgeSymbol {

		public final String name;
		public final int instability;

		private final int itemRank;
		private final float randomWeight;

		private final ArrayList<String> icons;

		private VoidPage(String s, int ins, int r, float w, String... icons) {
			name = s;
			instability = ins;

			itemRank = r;
			randomWeight = w;

			this.icons = ReikaJavaLibrary.makeListFromArray(icons);
			if (this.icons.size() < 4)
				this.icons.add(0, WordData.Void);
			if (this.icons.size() < 4)
				this.icons.add(0, WordData.Force);
			if (this.icons.size() < 4)
				this.icons.add(0, WordData.Possibility);
			if (this.icons.size() < 4)
				this.icons.add(0, WordData.Resilience);
		}

		@Override
		public void registerLogic(AgeDirector age, long seed) {

		}

		@Override
		public int instabilityModifier(int count) {
			return count == 1 ? instability : 0;
		}

		@Override
		public String identifier() {
			return "void_"+ReikaStringParser.stripSpaces(name).toLowerCase(Locale.ENGLISH);
		}

		@Override
		public String displayName() {
			return name;
		}

		@Override
		public String[] getPoem() {
			ArrayList<String> li = new ArrayList();
			for (String s : icons) {
				li.add(s);
			}
			return li.toArray(new String[li.size()]);
		}

	}
}
