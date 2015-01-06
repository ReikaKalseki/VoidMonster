/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster.World;

import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.MinecraftForge;
import Reika.DragonAPI.Auxiliary.Trackers.TickRegistry.TickHandler;
import Reika.DragonAPI.Auxiliary.Trackers.TickRegistry.TickType;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class AmbientSoundGenerator implements TickHandler {

	private final Random rand = new Random();

	public AmbientSoundGenerator() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void tick(TickType type, Object... tickData) {
		EntityPlayer ep = (EntityPlayer)tickData[0];
		World world = ep.worldObj;
		if (world.getWorldInfo().getTerrainType() != WorldType.FLAT) {
			if (ep != null) {
				if (rand.nextInt(200) == 0) {
					if (ep.posY < 45) {
						int x = MathHelper.floor_double(ep.posX);
						int y = MathHelper.floor_double(ep.posY);
						int z = MathHelper.floor_double(ep.posZ);
						if (world.canBlockSeeTheSky(x, y+1, z))
							return;
						if (world.getBlockLightValue(x, y+1, z) > 7)
							return;
						float volume = 2*(45-(float)ep.posY)/45F;
						if (rand.nextInt(4) == 0) { //stack sounds
							int n = 1+rand.nextInt(4);
							for (int i = 0; i < n; i++)
								ep.playSound("ambient.cave.cave", volume/n, 0);
						}
						else {
							ep.playSound("ambient.cave.cave", volume, 0);
						}
					}
				}
			}
		}
	}

	@Override
	public TickType getType() {
		return TickType.PLAYER;
	}

	@Override
	public boolean canFire(Phase p) {
		return p == Phase.START;
	}

	@Override
	public String getLabel() {
		return "VoidMonster Ambient Sound";
	}

}
