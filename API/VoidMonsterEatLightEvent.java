package Reika.VoidMonster.API;

import net.minecraft.block.Block;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Event.PositionEvent;

import cpw.mods.fml.common.eventhandler.Cancelable;

@Cancelable
/** Note that cancellation will have no effect on torches or glowstone; those cannot be disabled. */
public class VoidMonsterEatLightEvent extends PositionEvent {

	public final Block block;
	public final int metadata;

	public VoidMonsterEatLightEvent(World world, int x, int y, int z, Block b, int meta) {
		super(world, x, y, z);
		block = b;
		metadata = meta;
	}

}
