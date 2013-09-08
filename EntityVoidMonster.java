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

	private boolean isNether;

	public EntityVoidMonster(World world) {
		super(world);
		this.setLocationAndAngles(0, -32, 0, 0, 0);
	}

	public EntityVoidMonster setNether() {
		isNether = true;
		return this;
	}

	public boolean isNetherVoid() {
		return isNether;
	}

	@Override
	public int getMaxHealth() {
		return 100;
	}

}
