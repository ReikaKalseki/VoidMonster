/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2016
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster.World;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.MinecraftForge;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Auxiliary.Trackers.TickRegistry.TickHandler;
import Reika.DragonAPI.Auxiliary.Trackers.TickRegistry.TickType;
import Reika.DragonAPI.ModInteract.ItemHandlers.ExtraUtilsHandler;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class AmbientSoundGenerator implements TickHandler {

	public static final AmbientSoundGenerator instance = new AmbientSoundGenerator();

	private final Random rand = new Random();

	private final HashSet<Integer> bannedDimensions = new HashSet();

	private AmbientSoundGenerator() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void tick(TickType type, Object... tickData) {
		EntityPlayer ep = (EntityPlayer)tickData[0];
		World world = ep.worldObj;
		if (this.canSpawnSounds(world)) {
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

	private boolean canSpawnSounds(World world) {
		if (world.getWorldInfo().getTerrainType() == WorldType.FLAT)
			return false;
		return this.isHardcodedAllowed(world.provider.dimensionId) || !bannedDimensions.contains(world.provider.dimensionId);
	}

	public void blacklistDimension(int id) {
		bannedDimensions.add(id);
	}

	private boolean isHardcodedAllowed(int id) {
		if (id == 0)
			return true;
		if (id == -1)
			return true;
		if (ModList.EXTRAUTILS.isLoaded() && id == ExtraUtilsHandler.getInstance().darkID)
			return true;
		return false;
	}

	@Override
	public EnumSet<TickType> getType() {
		return EnumSet.of(TickType.PLAYER);
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
