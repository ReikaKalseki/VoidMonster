/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2018
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster.API;

import Reika.VoidMonster.Entity.EntityVoidMonster;


public interface VoidMonsterHook {

	public void tick(EntityVoidMonster e);

}
