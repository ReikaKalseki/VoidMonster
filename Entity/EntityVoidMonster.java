/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster.Entity;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.FakePlayer;
import net.minecraftforge.fluids.BlockFluidBase;
import Reika.DragonAPI.Instantiable.ItemDrop;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.RotaryCraft.API.RadarJammer;
import Reika.VoidMonster.VoidMonster;

public final class EntityVoidMonster extends EntityMob implements RadarJammer {

	private boolean isNether;

	public int innerRotation;

	private int hitCooldown;
	private int attackCooldown;
	private int healTime;

	private static final ArrayList<ItemDrop> drops = new ArrayList();

	public EntityVoidMonster(World world) {
		super(world);
		experienceValue = 20000;

		innerRotation = rand.nextInt(100000);
		this.setSize(3, 3);

		isImmuneToFire = true;
		ignoreFrustumCheck = true;
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		dataWatcher.addObject(31, healTime);
	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(200.0D);
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(16.0D);
	}

	@Override
	public void onUpdate()
	{
		boolean flag = false;
		if (worldObj.difficultySetting == 0)
			flag = true;
		if (flag)
			worldObj.difficultySetting = 1;
		super.onUpdate();
		if (flag)
			worldObj.difficultySetting = 0;

		innerRotation++;
		if (innerRotation >= 3600) {
			innerRotation = 0;
		}
	}

	@Override
	public void onLivingUpdate()
	{
		super.onLivingUpdate();

		if (posY < -40)
			posY = -10;
		motionY = 0;

		if (entityToAttack != null && hitCooldown == 0) {
			double dx = posX-entityToAttack.posX;
			double dy = posY-entityToAttack.posY-entityToAttack.getEyeHeight();
			double dz = posZ-entityToAttack.posZ;
			double dist = ReikaMathLibrary.py3d(dx, dy, dz);
			dist = Math.max(1, dist);
			motionX = -dx/dist/16D;
			motionY = -dy/dist/16D;
			motionZ = -dz/dist/16D;
			velocityChanged = true;
		}
		if (hitCooldown > 0)
			hitCooldown--;
		if (attackCooldown > 0)
			attackCooldown--;

		if (!worldObj.isRemote) {
			if (this.isHealing()) {
				healTime--;
				this.heal(0.25F);
			}
			else if (this.isAtLessHealth()) {
				if (rand.nextInt(80) == 0)
					healTime = 40;
			}
			dataWatcher.updateObject(31, healTime);
		}

		this.pushOutOfBlocks(posX, posY-4, posZ);

		if (!worldObj.isRemote)
			this.eatTorches();
	}

	private void eatTorches() {
		int r = 3;
		for (int i = -r; i <= r; i++) {
			for (int j = -r; j <= r; j++) {
				for (int k = -r; k <= r; k++) {
					int x = MathHelper.floor_double(posX)+i;
					int y = MathHelper.floor_double(posY)+j;
					int z = MathHelper.floor_double(posZ)+k;
					int id = worldObj.getBlockId(x, y, z);
					if (id > 0) {
						Block b = Block.blocksList[id];
						if (b != null && !(b instanceof BlockFluid || b instanceof BlockFluidBase)) {
							int meta = worldObj.getBlockMetadata(x, y, z);
							if (!b.hasTileEntity(meta) && b.blockHardness >= 0) {
								if (b.getLightValue(worldObj, x, y, z) > 0) {
									ReikaWorldHelper.dropBlockAt(worldObj, x, y, z);
									ReikaSoundHelper.playBreakSound(worldObj, x, y, z, b);
									worldObj.setBlock(x, y, z, 0);
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected EntityPlayer findPlayerToAttack() {
		return worldObj.getClosestPlayerToEntity(this, 128);
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
		return true;
	}

	@Override
	public int getTalkInterval()
	{
		return 80;
	}

	@Override
	public void playLivingSound()
	{
		for (int i = 0; i < 2; i++)
			super.playLivingSound();
	}

	@Override
	protected String getLivingSound()
	{
		switch(rand.nextInt(9)) {
		case 0:
			return "mob.enderdragon.growl";
		case 1:
			return "mob.wither.death";
		case 2:
			return "mob.zombiepig.zpigdeath";
		case 3:
			return "mob.irongolem.death";
		case 4:
			return "mob.ghast.death";
		case 5:
			return "mob.zombiepig.zpig";
		case 6:
			return "mob.zombie.say";
		case 7:
			return "mob.zombie.death";
		case 8:
			return "mob.wither.idle";
		}
		return null;
	}

	@Override
	protected float getSoundVolume()
	{
		return 2F;
	}

	@Override
	protected float getSoundPitch()
	{
		return 0;
	}

	@Override
	protected String getHurtSound()
	{
		return "mob.zombie.hurt";
	}

	@Override
	protected String getDeathSound()
	{
		return "mob.wither.death";
	}

	static {
		addDrop(Item.diamond, 2, 8);
		addDrop(new ItemStack(Item.enchantedBook), Enchantment.fortune, 3);
		addDrop(new ItemStack(Item.enchantedBook), Enchantment.infinity, 1);
		addDrop(new ItemStack(Item.enchantedBook), Enchantment.protection, 4);
		addDrop(Item.ghastTear);
		addDrop(Item.speckledMelon, 2, 5);
		addDrop(Item.emerald, 2, 6);
		addDrop(Item.enderPearl, 1, 3);
		addDrop(Item.eyeOfEnder, 1, 3);
		addDrop(Item.fireballCharge, 2, 8);
		addDrop(Item.netherStalkSeeds, 8, 22);
		addDrop(Item.netherStar, 1, 2);
		addDrop(Block.obsidian, 6, 16);
		addDrop(Item.gunpowder, 8, 12);
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

	public static void addDrop(ItemStack is, int min, int max) {
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
	public void writeEntityToNBT(NBTTagCompound nbt)
	{
		super.writeEntityToNBT(nbt);

		nbt.setBoolean("nether", isNether);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);

		isNether = nbt.getBoolean("nether");
	}

	@Override
	protected void kill() {
		//to prevent void damage
	}

	@Override
	public boolean attackEntityFrom(DamageSource src, float dmg) {
		if (hitCooldown > 0)
			return false;
		if (src.isFireDamage() || src == DamageSource.fall || src == DamageSource.outOfWorld || src == DamageSource.inWall || src == DamageSource.drown)
			return false;
		Entity e = src.getEntity();
		if (!(e instanceof EntityPlayer) || e instanceof FakePlayer)
			return false;
		if (this.isHealing()) {
			this.playSound("random.bowhit", 1, 1);
			return false;
		}
		if (posY < 0)
			return false;
		dmg = Math.min(dmg, 20);
		boolean flag = super.attackEntityFrom(src, dmg);
		if (flag && this.getHealth() > 0) {
			hitCooldown = 50;
			this.teleport(e);
		}
		return flag;
	}

	private void teleport(Entity e) {
		double dx = posX-e.posX;
		double dy = posY-e.posY-e.getEyeHeight();
		double dz = posZ-e.posZ;
		double dist = ReikaMathLibrary.py3d(dx, dy, dz);
		dist = Math.max(1, dist);
		//this.setPosition(e.posX-dx, e.posY-dy, e.posZ-dz);
		motionX = -dx/dist*2;
		motionY = -dy/dist*2;
		motionZ = -dz/dist*2;
		velocityChanged = true;
		this.playSound("mob.endermen.portal", 1.0F, 1.0F);
	}

	public boolean isAtLessHealth() {
		return this.getHealth() < this.getMaxHealth();
	}

	public boolean isHealing() {
		healTime = dataWatcher.getWatchableObjectInt(31);
		return this.isAtLessHealth() && healTime > 0;
	}

	@Override
	public boolean attackEntityAsMob(Entity e)
	{
		boolean flag = super.attackEntityAsMob(e);
		if (flag && e instanceof EntityLivingBase) {
			((EntityLivingBase)e).addPotionEffect(new PotionEffect(Potion.blindness.id, 200, 0));
			if (isNether) {
				e.setFire(10);
			}
			this.heal(2);
		}
		return flag;
	}

	@Override
	public void setDead()
	{
		super.setDead();
	}

	@Override
	public String getEntityName() {
		return "Void Monster";
	}

	@Override
	public void setFire(int par1)
	{

	}

	@Override
	public boolean canRenderOnFire()
	{
		return false;
	}

	@Override
	public boolean isPushedByWater()
	{
		return false;
	}

	@Override
	public float getShadowSize()
	{
		return 0;
	}

	@Override
	public AxisAlignedBB getCollisionBox(Entity entity)
	{
		if (entity instanceof EntityLivingBase && !(entity instanceof EntityVoidMonster)) {
			if (attackCooldown == 0) {
				this.attackEntityAsMob(entity);
				attackCooldown = 20;
			}
		}
		return null;//AxisAlignedBB.getAABBPool().getAABB(posX, posY, posZ, posX, posY, posZ).expand(3, 3, 3);
	}

	@Override
	public boolean jamRadar(World world, int radarX, int radarY, int radarZ) {
		return true;
	}

}
