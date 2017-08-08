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

import net.minecraft.client.audio.SoundCategory;
import net.minecraft.world.World;
import Reika.DragonAPI.Instantiable.IO.SingleSound;
import Reika.DragonAPI.Instantiable.IO.SoundLoader;
import Reika.DragonAPI.Interfaces.Registry.SoundEnum;
import Reika.VoidMonster.Entity.EntityVoidMonster;
import Reika.VoidMonster.Entity.RenderVoidMonster;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class VoidClient extends VoidCommon {

	public static final SoundEnum monsterAura = new SingleSound("aura", "Reika/VoidMonster/aura3.ogg", SoundCategory.MASTER);

	@Override
	public void registerSounds() {
		new SoundLoader(monsterAura).register();
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
