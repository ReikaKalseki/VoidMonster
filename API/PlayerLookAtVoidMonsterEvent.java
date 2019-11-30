package Reika.VoidMonster.API;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;


public class PlayerLookAtVoidMonsterEvent extends PlayerEvent {

	public final EntityLivingBase monster;

	public PlayerLookAtVoidMonsterEvent(EntityPlayer ep, EntityLivingBase m) {
		super(ep);
		monster = m;
	}

}
