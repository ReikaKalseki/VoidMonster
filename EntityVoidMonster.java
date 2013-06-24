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

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.world.World;

public class EntityVoidMonster extends EntityMob {

	public EntityVoidMonster(World par1World) {
		super(par1World);
	}

	@Override
	public int getMaxHealth() {
		return 100;
	}

}
