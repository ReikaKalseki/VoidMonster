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

import net.minecraft.entity.Entity;
import Reika.DragonAPI.Instantiable.CustomStringDamageSource;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.VoidMonster.Entity.EntityVoidMonster;


public class VoidMonsterDamage extends CustomStringDamageSource {

	private final EntityVoidMonster monster;

	public VoidMonsterDamage(EntityVoidMonster e) {
		super(getRandomString());
		this.setDamageIsAbsolute().setDamageBypassesArmor();
		monster = e;
	}

	private static String getRandomString() {
		StringBuilder sb = new StringBuilder();
		while (sb.length() < 24) {
			int len = Math.min(24-sb.length(), ReikaRandomHelper.getRandomBetween(4, 8));
			while (len > 0 && sb.length() < 24) {
				int idx = ReikaRandomHelper.getRandomBetween(33, 254);
				while (idx == 127 || idx == '%' || idx == '\\') //127 = DEL
					idx = ReikaRandomHelper.getRandomBetween(33, 254);
				char c = (char)idx;
				sb.append(String.valueOf(c));
			}
			sb.append(" ");
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}

	@Override
	public boolean isUnblockable()
	{
		return true;
	}

	@Override
	public boolean isDamageAbsolute()
	{
		return true;
	}

	@Override
	public boolean isExplosion()
	{
		return false;
	}

	@Override
	public boolean isProjectile()
	{
		return false;
	}

	@Override
	public boolean canHarmInCreative()
	{
		return false;
	}

	@Override
	public Entity getSourceOfDamage()
	{
		return this.getEntity();
	}

	@Override
	public Entity getEntity()
	{
		return monster;
	}

	@Override
	public boolean isFireDamage()
	{
		return false;
	}

	@Override
	public boolean isDifficultyScaled()
	{
		return false;
	}

	@Override
	public boolean isMagicDamage()
	{
		return false;
	}

}
