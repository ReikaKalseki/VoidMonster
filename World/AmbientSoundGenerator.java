/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster.World;

import java.util.EnumSet;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class AmbientSoundGenerator implements ITickHandler {

	private final Random rand = new Random();

	public AmbientSoundGenerator() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		EntityPlayer ep = (EntityPlayer)tickData[0];
		World world = ep.worldObj;
		if (world.getWorldInfo().getTerrainType() != WorldType.FLAT) {
			if (ep != null) {
				if (rand.nextInt(200) == 0) {
					if (ep.posY < 45) {
						float volume = 2*(45-(float)ep.posY)/45F;
						ep.playSound("ambient.cave.cave", volume, 0);
					}
				}
			}
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {

	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.PLAYER);
	}

	@Override
	public String getLabel() {
		return "VoidMonster Ambient Sound";
	}

}
