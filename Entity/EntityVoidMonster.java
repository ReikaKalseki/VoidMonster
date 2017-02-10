/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2016
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster.Entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.DragonAPI.ModInteract.ItemHandlers.ThaumItemHelper;
import Reika.RotaryCraft.API.Interfaces.RadarJammer;
import Reika.VoidMonster.VoidMonster;
import Reika.VoidMonster.VoidMonsterDrops;

public final class EntityVoidMonster extends EntityMob implements RadarJammer {

	private boolean isNether;

	public int innerRotation;

	private int hitCooldown;
	private int attackCooldown;
	private int healTime;

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
		float f = this.getDifficulty();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(300.0D*f*f);
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(16.0D*f);
	}

	public float getDifficulty() {
		return VoidMonster.instance.getMonsterDifficulty();
	}

	@Override
	public void onUpdate()
	{
		if (!VoidMonster.allowedIn(worldObj)) {
			this.setDead();
			return;
		}

		boolean flag = false;
		if (worldObj.difficultySetting == EnumDifficulty.PEACEFUL)
			flag = true;
		if (flag)
			worldObj.difficultySetting = EnumDifficulty.EASY;
		super.onUpdate();
		if (flag)
			worldObj.difficultySetting = EnumDifficulty.PEACEFUL;

		innerRotation++;
		if (innerRotation >= 3600) {
			innerRotation = 0;
		}
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();

		if (posY < -40)
			posY = -10;
		motionY = 0;

		float f = this.getDifficulty();

		entityToAttack = worldObj.getClosestPlayerToEntity(this, -1);
		if (entityToAttack != null && hitCooldown == 0) {
			double dx = posX-entityToAttack.posX;
			double dy = posY-entityToAttack.posY-entityToAttack.getEyeHeight();
			double dz = posZ-entityToAttack.posZ;
			double dist = ReikaMathLibrary.py3d(dx, dy, dz);
			dist = Math.max(1, dist);
			motionX = -dx/dist/16D*f;
			motionY = -dy/dist/16D*f;
			motionZ = -dz/dist/16D*f;
			velocityChanged = true;
		}

		if (hitCooldown > 0)
			hitCooldown--;
		if (attackCooldown > 0)
			attackCooldown--;

		if (!worldObj.isRemote) {
			if (this.isHealing()) {
				healTime--;
				this.heal(0.25F*f);
			}
			else if (this.isAtLessHealth()) {
				if (rand.nextInt((int)(80/f)) == 0)
					healTime = 40;
			}
			dataWatcher.updateObject(31, healTime);
		}

		this.func_145771_j(posX, posY-4, posZ);

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
					Block b = worldObj.getBlock(x, y, z);
					if (b != Blocks.air) {
						if (b != null && !(b instanceof BlockLiquid || b instanceof BlockFluidBase)) {
							int meta = worldObj.getBlockMetadata(x, y, z);
							if (!b.hasTileEntity(meta) && b.getBlockHardness(worldObj, x, y, z) >= 0) {
								if (b.getLightValue(worldObj, x, y, z) > 0) {
									ReikaWorldHelper.dropBlockAt(worldObj, x, y, z, null);
									ReikaSoundHelper.playBreakSound(worldObj, x, y, z, b);
									worldObj.setBlockToAir(x, y, z);
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
	public boolean isPotionApplicable(PotionEffect pot)
	{
		return false;
	}

	@Override
	public int getTalkInterval()
	{
		int delay = VoidMonster.instance.getMonsterSoundDelay();
		return delay/2+rand.nextInt(1+delay/2);
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
	protected float getSoundVolume() {
		EntityPlayer ep = worldObj.getClosestPlayerToEntity(this, 32);
		if (ep == null)
			return 0.5F;
		float d = this.getDistanceToEntity(ep);
		return d < 6 ? 2 : d < 12 ? 1 : 0.5F;
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

	@Override
	protected void dropFewItems(boolean par1, int par2) {
		VoidMonsterDrops.doDrops(this);
		ReikaWorldHelper.splitAndSpawnXP(worldObj, posX, posY, posZ, experienceValue);
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
	public void setHealth(float health) {
		super.setHealth(health);
	}

	@Override
	public boolean attackEntityFrom(DamageSource src, float dmg) {
		if (hitCooldown > 0)
			return false;
		if (src.isFireDamage() || src == DamageSource.fall || src == DamageSource.outOfWorld || src == DamageSource.inWall || src == DamageSource.drown)
			return false;
		Entity e = src.getEntity();
		if (!(e instanceof EntityPlayer))
			return false;
		float cap = 20;
		if (src.isMagicDamage() && dmg > 5000)
			cap = 100;
		cap /= this.getDifficulty();
		EntityPlayer ep = (EntityPlayer)e;
		ItemStack weapon = ep.getCurrentEquippedItem();
		if (ModList.THAUMCRAFT.isLoaded()) {
			if (ThaumItemHelper.isVoidMetalTool(weapon)) {
				cap *= 2F;
			}
			else if (ThaumItemHelper.isWarpingTool(weapon)) {
				cap *= 1.5F;
			}
		}
		if (this.isHealing()) {
			this.playSound("random.bowhit", 1, 1);
			return false;
		}
		if (posY < 0)
			return false;
		dmg = Math.min(dmg, cap);
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
			this.heal(2*this.getDifficulty());
		}
		return flag;
	}

	@Override
	public void setDead()
	{
		super.setDead();
	}

	@Override
	public String getCommandSenderName() {
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
		return null;//AxisAlignedBB.getBoundingBox(posX, posY, posZ, posX, posY, posZ).expand(3, 3, 3);
	}

	@Override
	public boolean jamRadar(World world, int radarX, int radarY, int radarZ) {
		return true;
	}

}
