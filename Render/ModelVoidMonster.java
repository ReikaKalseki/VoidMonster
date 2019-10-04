/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster.Render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelDragon;
import net.minecraft.client.model.ModelEnderCrystal;
import net.minecraft.client.model.ModelGhast;
import net.minecraft.entity.Entity;

public class ModelVoidMonster extends ModelBase {

	private ModelEnderCrystal c = new ModelEnderCrystal(0, false);
	private ModelGhast g = new ModelGhast();
	private ModelDragon d = new ModelDragon(0);

	public ModelVoidMonster() {

	}

	@Override
	public void render(Entity e, float par2, float par3, float par4, float par5, float par6, float par7) {
		c.render(e, par2, par3, par4, par5, par6, par7);
	}

}
