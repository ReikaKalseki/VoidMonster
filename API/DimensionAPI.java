/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2016
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster.API;

import Reika.VoidMonster.World.AmbientSoundGenerator;
import Reika.VoidMonster.World.MonsterGenerator;

public class DimensionAPI {

	public static void blacklistDimensionForSounds(int id) {
		AmbientSoundGenerator.instance.blacklistDimension(id);
	}

	public static void setDimensionRuleForSpawning(int id, boolean allow) {
		MonsterGenerator.instance.setDimensionRuleAPI(id, allow);
	}

}
