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

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import Reika.DragonAPI.Instantiable.ItemDrop;

public class EntityVoidMonster extends EntityLiving {

	private boolean isNether;

	private int soundLength;

	private EntityPlayer target;

	private static final ArrayList<ItemDrop> drops = new ArrayList();

	public EntityVoidMonster(World world) {
		super(world);
		this.setLocationAndAngles(0, -32, 0, 0, 0);
		experienceValue = 20000;
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		target = this.findPlayerEntity();
	}

	@Override
	public void onLivingUpdate()
	{
		super.onLivingUpdate();
	}

	private EntityPlayer findPlayerEntity() {

		return null;
	}

	public EntityVoidMonster setNether() {
		isNether = true;
		return this;
	}

	public boolean isNetherVoid() {
		return isNether;
	}

	@Override
	public boolean canBeCollidedWith() {
		return false;
	}

	/**
	 * Get number of ticks, at least during which the living entity will be silent.
	 */
	@Override
	public int getTalkInterval()
	{
		return 80;
	}

	static {
		addDrop(Item.diamond, 1, 3);
		addDrop(new ItemStack(Item.enchantedBook), Enchantment.fortune, 3);
	}

	private static void addDrop(Item i) {
		addDrop(i, 1, 1);
	}

	private static void addDrop(Item i, int min, int max) {
		addDrop(new ItemStack(i), min, max);
	}

	private static void addDrop(Block b) {
		addDrop(b, 1, 1);
	}

	private static void addDrop(Block b, int min, int max) {
		addDrop(new ItemStack(b), min, max);
	}

	private static void addDrop(ItemStack is) {
		addDrop(is, 1, 1);
	}

	private static void addDrop(ItemStack is, int min, int max) {
		ItemDrop it = new ItemDrop(is, min, max);
		if (!drops.contains(it))
			drops.add(it);
		VoidMonster.instance.getModLogger().log("Adding monster drop "+is.getDisplayName());
	}

	private static void addDrop(ItemStack is, Enchantment e, int level) {
		ItemDrop it = new ItemDrop(is, 1, 1);
		it.enchant(e, level);
		if (!drops.contains(it))
			drops.add(it);
		VoidMonster.instance.getModLogger().log("Adding monster drop "+is.getDisplayName()+", enchanted with "+e.getName()+" "+level);
	}

	private static void addDrop(ItemStack is, int min, int max, Enchantment e, int level) {
		ItemDrop it = new ItemDrop(is, min, max);
		it.enchant(e, level);
		if (!drops.contains(it))
			drops.add(it);
		VoidMonster.instance.getModLogger().log("Adding monster drop "+is.getDisplayName()+", enchanted with "+e.getName()+" "+level);
	}

	@Override
	protected void dropFewItems(boolean par1, int par2)
	{
		for (int i = 0; i < drops.size(); i++) {
			ItemDrop it = drops.get(i);
			it.drop(this);
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeEntityToNBT(par1NBTTagCompound);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readEntityFromNBT(par1NBTTagCompound);
	}

}
