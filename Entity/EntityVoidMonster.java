/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster.Entity;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import Reika.ChromatiCraft.Registry.ChromaIcons;
import Reika.ChromatiCraft.Render.Particle.EntityBlurFX;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Instantiable.RayTracer;
import Reika.DragonAPI.Interfaces.Entity.DestroyOnUnload;
import Reika.DragonAPI.Libraries.ReikaEntityHelper;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.MathSci.ReikaPhysicsHelper;
import Reika.DragonAPI.Libraries.World.ReikaBlockHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.DragonAPI.ModInteract.ItemHandlers.ExtraUtilsHandler;
import Reika.DragonAPI.ModInteract.ItemHandlers.ThaumItemHelper;
import Reika.RotaryCraft.API.Interfaces.RadarJammer;
import Reika.VoidMonster.GhostMonsterDamage;
import Reika.VoidMonster.MonsterFX;
import Reika.VoidMonster.VoidMonster;
import Reika.VoidMonster.VoidMonsterDamage;
import Reika.VoidMonster.VoidMonsterDrops;
import Reika.VoidMonster.API.NonTeleportingDamage;
import Reika.VoidMonster.API.VoidMonsterHook;
import Reika.VoidMonster.World.MonsterGenerator;
import WayofTime.alchemicalWizardry.api.soulNetwork.SoulNetworkHandler;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public final class EntityVoidMonster extends EntityMob implements RadarJammer, DestroyOnUnload, IEntityAdditionalSpawnData {

	private boolean isNether;
	private boolean isGhost;

	public int innerRotation;

	private int hitCooldown;
	private int attackCooldown;
	private int ghostTick;
	private int healTime;

	private final Collection<VoidMonsterHook> hooks = new ArrayList();

	private static final RayTracer LOS = RayTracer.getVisualLOS();

	public EntityVoidMonster(World world) {
		super(world);
		experienceValue = 20000;

		innerRotation = rand.nextInt(100000);
		this.setSize(3, 3);

		isImmuneToFire = true;
		//ignoreFrustumCheck = true;
	}

	public EntityVoidMonster setNether() {
		isNether = true;
		return this;
	}

	public EntityVoidMonster setGhost() {
		isGhost = true;
		return this;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(31, healTime);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		float f = this.getDifficulty();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(300.0D*f*f);
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(16.0D*f);
	}

	public float getDifficulty() {
		return VoidMonster.instance.getMonsterDifficulty();
	}

	public void addHook(VoidMonsterHook h) {
		hooks.add(h);
	}

	@Override
	public void onUpdate() {
		if (!VoidMonster.allowedIn(worldObj)) {
			this.setDead();
			return;
		}

		VoidMonster.registerExistingMonster(this);

		if (!worldObj.isRemote) {
			for (VoidMonsterHook h : hooks) {
				h.tick(this);
			}
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

		if (worldObj.isRemote && ticksExisted%128 == 0) {
			ReikaEntityHelper.verifyClientEntity(this);
		}
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();

		if (posY < -40)
			posY = -10;
		motionY = 0;

		rotationPitch = rotationYaw = rotationYawHead = renderYawOffset = 0;
		prevRotationPitch = prevRotationYaw = prevRotationYawHead = prevRenderYawOffset = 0;

		float f = this.getDifficulty();

		entityToAttack = worldObj.getClosestPlayerToEntity(this, -1);
		if (entityToAttack != null && hitCooldown == 0) {
			this.moveToAttackEntity((EntityLivingBase)entityToAttack, f);
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
			else if (!isGhost && this.isAtLessHealth()) {
				if (rand.nextInt((int)(80/f)) == 0)
					healTime = 40;
			}
			dataWatcher.updateObject(31, healTime);
		}

		this.func_145771_j(posX, posY-0*4, posZ);

		if (!worldObj.isRemote) {
			if (!isGhost)
				this.eatTorches();
		}
		else
			this.playSounds();
	}

	@SideOnly(Side.CLIENT)
	private void playSounds() {
		if (!isGhost && MonsterFX.clearLOS(this)) {
			/*
			if (ticksExisted%8 == 0) {
				ReikaSoundHelper.playClientSound(VoidClient.monsterAura, posX, posY, posZ, 1, f, true);
			}
			 */
			int t3 = ticksExisted%64;
			int t2 = t3%32;
			int t = t2%16;
			if (t == 0 || t == 5 || (t == 8 && t2 > 16 && t3 > 32)) {
				float f = (float)(0.75F+0.13*Math.sin(ticksExisted/40D));
				//ReikaJavaLibrary.pConsole(f);
				ReikaSoundHelper.playClientSound("mob.wither.spawn", posX, posY, posZ, 0.8F, f, true);
				ReikaSoundHelper.playClientSound("note.bd", posX, posY, posZ, 1, 0.5F, true);
			}
			if (t == 3 || t == 8 || t == 13) {
				ReikaSoundHelper.playClientSound("note.bassattack", posX, posY, posZ, 0.5F, 0.5F+rand.nextFloat()*0.25F, true);
			}
		}
	}

	public void moveToAttackEntity(EntityLivingBase e, float f) {
		double dist = this.moveTowards(e.posX, e.posY, e.posZ, f);

		if (isGhost) {
			if (!worldObj.isRemote) {
				if (this.canSee(e) && ReikaEntityHelper.isLookingAt(e, this)) {
					ghostTick++;
					if (ghostTick >= 1200)
						this.setDead();
				}
				if (dist < 4) {
					motionX = motionY = motionZ = 0;
					velocityChanged = true;
				}
			}
			return;
		}

		if (dist <= 20) { //play sound
			double dmg = 6;
			if (!worldObj.isRemote && dist <= dmg && this.canHurt(e) && this.canSee(e)) { //hurt
				this.drainHealth(e, f, dist, dmg);
			}
		}
	}

	public double moveTowards(double x, double y, double z, double vel) {
		double dx = posX-x;
		double dy = posY-(y-1.62*0);//entityToAttack.getEyeHeight();
		double dz = posZ-z;
		double dist = ReikaMathLibrary.py3d(dx, dy, dz);


		if (ModList.EXTRAUTILS.isLoaded() && ExtraUtilsHandler.getInstance().initializedProperly() && worldObj.provider.dimensionId == ExtraUtilsHandler.getInstance().darkID && (dist >= 200 || (posY >= 2 && posY < 80))) {
			noClip = true;
		}
		else if (isGhost) {
			noClip = (ticksExisted%200) < 100 && this.getDistanceSq(x, y, z) >= 64;
		}

		double d = Math.max(1, dist);
		double f2 = 1.5;
		double f3 = 0.9375;
		if (dist >= 256) {
			f2 = 12;
		}
		else if (dist >= 96) {
			f2 = Math.min(12, 4+(dist-96)/16D); //4 at 96, 6 at 128, 12 at 256 minus a bit
		}
		else if (dist >= 16) {
			f2 = 1.5+(dist-16)/32D; //1.5 at 16, 4 at 96 (was 2 in all cases)
		}
		motionX = -dx/d/16D*vel*f2*f3;
		motionY = -dy/d/16D*vel*6*f3;
		motionZ = -dz/d/16D*vel*f2*f3;
		velocityChanged = true;

		return dist;
	}

	private boolean canSee(EntityLivingBase e) {
		for (double dx = -2; dx <= 2; dx += 0.5) {
			for (double dy = -2; dy <= 1; dy += 0.5) { //+1, not +2, to avoid 'leaking' through bedrock
				for (double dz = -2; dz <= 2; dz += 0.5) {
					LOS.setOrigins(posX+dx, posY+dy, posZ+dz, e.posX, e.posY, e.posZ);
					if (LOS.isClearLineOfSight(worldObj))
						return true;
				}
			}
		}
		return false;
	}

	private boolean canHurt(EntityLivingBase e) {
		if (e.isDead || e.getHealth() <= 0)
			return false;
		if (e instanceof EntityPlayer)
			return !((EntityPlayer)e).capabilities.isCreativeMode;
		return true;
	}

	private void drainHealth(EntityLivingBase e, float f, double dist, double damageDist) {
		double fullDist = 2;
		float fac = dist <= fullDist ? 1 : (float)(1-(fullDist-2)/(damageDist-dist));
		float base = (float)this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
		float attack = base*fac/20F;
		this.drainLPArmorOrHealth(e, attack);
		this.onAttackEntity(e, f, fac);
	}

	private void drainLPArmorOrHealth(EntityLivingBase e, float attack) {
		if (e instanceof EntityPlayer && ModList.BLOODMAGIC.isLoaded()) {
			attack -= this.drainLP((EntityPlayer)e, attack);
			if (attack <= 0)
				return;
		}
		attack -= this.drainArmor(e, attack);
		if (attack <= 0)
			return;
		ReikaEntityHelper.doSetHealthDamage(e, new VoidMonsterDamage(this), attack);
	}

	private int drainArmor(EntityLivingBase e, float attack) {
		HashSet<Integer> slots = new HashSet();
		int ret = 0;
		for (int i = 1; i < 5; i++) {
			ItemStack is = e.getEquipmentInSlot(i);
			if (is != null && is.getItem() instanceof ItemArmor) {
				slots.add(i);
			}
		}
		if (!slots.isEmpty()) {
			float perSlot = attack/slots.size();
			//for (int slot : slots) {
			//this.handleArmorSlot(e, slot, e.getEquipmentInSlot(slot), MathHelper.ceiling_float_int(perSlot));
			//}
			ret += ReikaEntityHelper.damageArmor(e, MathHelper.ceiling_float_int(perSlot));
		}
		return ret;
	}

	@ModDependent(ModList.BLOODMAGIC)
	private int drainLP(EntityPlayer e, float attack) {
		int cur = SoulNetworkHandler.getCurrentEssence(e.getCommandSenderName());
		if (cur > 0) {
			int rem = Math.min(MathHelper.ceiling_float_int(attack), cur);
			SoulNetworkHandler.syphonFromNetwork(e.getCommandSenderName(), rem);
			return rem;
		}
		return 0;
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
						if (b != null && !ReikaBlockHelper.isLiquid(b)) {
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

	public boolean isNetherVoid() {
		return isNether;
	}

	@Override
	public boolean canBeCollidedWith() {
		return !isGhost;
	}

	@Override
	public void addPotionEffect(PotionEffect p) {
		if (this.isGhost() && p.getPotionID() == Potion.regeneration.id) {
			this.doGhostDamage(null, p.getAmplifier(), 1);
		}
	}

	public void doGhostDamage(EntityLivingBase src, int lvl, double f) {
		float amt = (float)(50*Math.max(0.25, f)*Math.pow(3, lvl));
		this.attackEntityFrom(new GhostMonsterDamage(src), amt);
	}

	@Override
	public boolean isPotionApplicable(PotionEffect pot) {
		return false;
	}

	@Override
	public int getTalkInterval()
	{
		int delay = VoidMonster.instance.getMonsterSoundDelay();
		return delay/2+rand.nextInt(1+delay/2);
	}

	@Override
	public void playLivingSound() {
		for (int i = 0; i < 2; i++)
			super.playLivingSound();
	}

	@Override
	protected String getLivingSound() {
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
	protected float getSoundPitch() {
		return 0;
	}

	@Override
	protected String getHurtSound() {
		return "mob.zombie.hurt";
	}

	@Override
	protected String getDeathSound() {
		return "mob.wither.death";
	}

	@Override
	public void onDeath(DamageSource src) {
		super.onDeath(src);

		if (worldObj.isRemote) {
			if (ModList.CHROMATICRAFT.isLoaded()) {
				this.spawnDeathParticles();
			}
		}
		else {
			worldObj.addWeatherEffect(new EntityLightningBolt(worldObj, posX, posY, posZ));
			MonsterGenerator.instance.addCooldown(this, 200);
		}
	}

	@SideOnly(Side.CLIENT)
	@ModDependent(ModList.CHROMATICRAFT)
	private void spawnDeathParticles() {
		for (int i = 0; i < 24; i++) {
			float s = (float)ReikaRandomHelper.getRandomBetween(1.5, 5);
			int l = ReikaRandomHelper.getRandomBetween(40, 200);
			EntityBlurFX fx = new EntityBlurFX(worldObj, posX, posY, posZ).setColor(0x000000).setBasicBlend().setScale(s).setLife(l);
			double v = ReikaRandomHelper.getRandomBetween(0.0625, 0.25);
			double[] xyz = ReikaPhysicsHelper.polarToCartesian(v, 0, rand.nextDouble()*360);
			fx.motionX = xyz[0];
			fx.motionZ = xyz[2];
			fx.setColliding().setIcon(ChromaIcons.FADE_BASICBLEND);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}
	}

	@Override
	protected void dropFewItems(boolean par1, int par2) {
		if (!isGhost) {
			VoidMonsterDrops.doDrops(this);
			ReikaWorldHelper.splitAndSpawnXP(worldObj, posX, posY, posZ, experienceValue);
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);

		nbt.setBoolean("nether", isNether);
		nbt.setBoolean("ghost", isGhost);

		nbt.setBoolean("isdead", isDead);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);

		isNether = nbt.getBoolean("nether");
		isGhost = nbt.getBoolean("ghost");

		if (nbt.getBoolean("isdead"))
			this.setDead();
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
		boolean ghostDamage = src instanceof GhostMonsterDamage;
		boolean monsterDamage = src instanceof VoidMonsterDamage;
		float cap = 20;
		if (monsterDamage) {
			cap = 50;
		}
		else {
			if (ghostDamage) {
				cap = 50;
			}
			else {
				if (isGhost)
					return false;
				if (hitCooldown > 0)
					return false;
				if (src.isFireDamage() || src == DamageSource.fall || src == DamageSource.outOfWorld || src == DamageSource.inWall || src == DamageSource.drown)
					return false;
				Entity e = src.getEntity();
				if (!(e instanceof EntityPlayer))
					return false;
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
			}
			if (src.isMagicDamage() && dmg > 5000)
				cap = 100;
			cap /= this.getDifficulty();
		}
		if (this.isHealing()) {
			this.playSound("random.bowhit", 1, 1);
			return false;
		}
		if (posY < 0)
			return false;
		float net = Math.min(dmg, cap);
		if (!ghostDamage && !monsterDamage) {
			float reflect = Math.min(15, (dmg-net)/8F*Math.min(8, (float)Math.pow(1.05, dmg-net)));
			src.getEntity().attackEntityFrom(DamageSource.outOfWorld, reflect);
		}
		boolean flag = super.attackEntityFrom(src, net);
		if (flag && this.getHealth() > 0 && !ghostDamage && !monsterDamage && !(src instanceof NonTeleportingDamage)) {
			hitCooldown = 50;
			this.teleport(src.getEntity());
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

	/*
	@Override
	public boolean attackEntityAsMob(Entity e)
	{
		boolean flag = super.attackEntityAsMob(e);
		if (flag && e instanceof EntityLivingBase) {
			onAttackEntity(e, getDifficulty(), 1);
		}
		return flag;
	}
	 */

	@Override
	public boolean attackEntityAsMob(Entity e)
	{
		return false;
	}

	private void onAttackEntity(EntityLivingBase e, float f, float fac) {
		if (fac > 0.5)
			e.addPotionEffect(new PotionEffect(Potion.blindness.id, (int)(100*fac), 0));
		if (isNether) {
			if (fac > 0.25)
				e.setFire((int)(10*fac));
		}
		this.heal(2*f);
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
	public void setFire(int par1) {

	}

	@Override
	public boolean canRenderOnFire()
	{
		return false;
	}

	@Override
	public boolean isPushedByWater() {
		return false;
	}

	@Override
	public float getShadowSize() {
		return 0;
	}

	@Override
	public AxisAlignedBB getCollisionBox(Entity entity) {
		/*
		if (entity instanceof EntityLivingBase && !(entity instanceof EntityVoidMonster)) {
			if (attackCooldown == 0) {
				this.attackEntityAsMob(entity);
				attackCooldown = 20;
			}
		}
		 */
		return null;//AxisAlignedBB.getBoundingBox(posX, posY, posZ, posX, posY, posZ).expand(3, 3, 3);
	}

	@Override
	public boolean jamRadar(World world, int radarX, int radarY, int radarZ) {
		return true;
	}

	@Override
	public void destroy() {
		this.setDead();
	}

	@Override
	public void writeSpawnData(ByteBuf buf) {
		buf.writeBoolean(isNether);
		buf.writeBoolean(isGhost);
	}

	@Override
	public void readSpawnData(ByteBuf buf) {
		isNether = buf.readBoolean();
		isGhost = buf.readBoolean();
	}

	public boolean isGhost() {
		return isGhost;
	}

}
