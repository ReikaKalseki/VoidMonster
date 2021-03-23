/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster;

import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.IO.SingleSound;
import Reika.DragonAPI.Instantiable.IO.SoundLoader;

public class VoidCommon {

	public static int tileRender;
	public static int wireRender;

	public static final SingleSound monsterAura = new SingleSound("aura", "Reika/VoidMonster/aura3.ogg");

	protected SoundLoader sounds = new SoundLoader(monsterAura);

	/**
	 * Client side only register stuff...
	 */
	public void registerRenderers()
	{
		//unused server side. -- see ClientProxy for implementation
	}

	public void addArmorRenders() {}

	public World getClientWorld() {
		return null;
	}

	public void registerRenderInformation() {
		// TODO Auto-generated method stub

	}

	public void registerSounds() {
		// TODO Auto-generated method stub
	}

}
