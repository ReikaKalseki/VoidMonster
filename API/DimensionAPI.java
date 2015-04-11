package Reika.VoidMonster.API;

import Reika.VoidMonster.World.AmbientSoundGenerator;
import Reika.VoidMonster.World.MonsterGenerator;

public class DimensionAPI {

	public static void blacklistDimensionForSounds(int id) {
		AmbientSoundGenerator.instance.blacklistDimension(id);
	}

	public static void blacklistDimensionForSpawning(int id) {
		MonsterGenerator.instance.banDimension(id);
	}

}
