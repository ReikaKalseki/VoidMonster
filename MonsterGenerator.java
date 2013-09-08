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

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import cpw.mods.fml.common.IWorldGenerator;

public class MonsterGenerator implements IWorldGenerator {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if (MonsterManager.canSpawnMonsterInWorld(world)) {
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
			if (ev != null)
				world.spawnEntityInWorld(ev);
		}
	}

}
