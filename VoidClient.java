/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster;

import Reika.VoidMonster.Entity.EntityVoidMonster;
import Reika.VoidMonster.Entity.RenderVoidMonster;

import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class VoidClient extends VoidCommon {

	@Override
	public void registerSounds() {

	}

	@Override
	public void registerRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(EntityVoidMonster.class, new RenderVoidMonster());
	}

	// Override any other methods that need to be handled differently client side.

	@Override
	public World getClientWorld()
	{
		return FMLClientHandler.instance().getClient().theWorld;
	}

}
