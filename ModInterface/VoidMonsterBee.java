/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.VoidMonster.ModInterface;

import java.util.Collection;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import Reika.ChromatiCraft.ModInterface.Bees.ChromaBeeHelpers;
import Reika.ChromatiCraft.ModInterface.Bees.TileEntityLumenAlveary;
import Reika.ChromatiCraft.ModInterface.Bees.TileEntityLumenAlveary.LumenAlvearyEffect;
import Reika.ChromatiCraft.Registry.ChromaIcons;
import Reika.ChromatiCraft.Registry.CrystalElement;
import Reika.ChromatiCraft.Render.Particle.EntityCCBlurFX;
import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Auxiliary.ModularLogger;
import Reika.DragonAPI.Instantiable.Rendering.ColorBlendList;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaPhysicsHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.Libraries.Rendering.ReikaColorAPI;
import Reika.DragonAPI.ModInteract.Bees.BasicFlowerProvider;
import Reika.DragonAPI.ModInteract.Bees.BasicGene;
import Reika.DragonAPI.ModInteract.Bees.BeeAlleleRegistry.Fertility;
import Reika.DragonAPI.ModInteract.Bees.BeeAlleleRegistry.Flowering;
import Reika.DragonAPI.ModInteract.Bees.BeeAlleleRegistry.Life;
import Reika.DragonAPI.ModInteract.Bees.BeeAlleleRegistry.Speeds;
import Reika.DragonAPI.ModInteract.Bees.BeeAlleleRegistry.Territory;
import Reika.DragonAPI.ModInteract.Bees.BeeAlleleRegistry.Tolerance;
import Reika.DragonAPI.ModInteract.Bees.BeeSpecies;
import Reika.DragonAPI.ModInteract.ItemHandlers.ForestryHandler;
import Reika.VoidMonster.VoidMonster;
import Reika.VoidMonster.Auxiliary.VoidMonsterDamage;
import Reika.VoidMonster.Entity.EntityVoidMonster;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.FlowerManager;
import forestry.api.apiculture.IAlleleBeeEffect;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.core.EnumHumidity;
import forestry.api.core.EnumTemperature;
import forestry.api.genetics.IAlleleFlowers;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IFlowerAcceptableRule;
import forestry.api.genetics.IFlowerProvider;

public class VoidMonsterBee extends BeeSpecies {

	private static final String LOGGER_ID = "VoidMonsterBee";

	private final AlleleVoid voidflower = new AlleleVoid();
	private final AlleleVoidEffect voideffect = new AlleleVoidEffect();

	private static ColorBlendList beeColor;
	private static Object voidMonsterAlvearyEffect;
	private static VoidMonsterBee beeType;

	private long nextDescTime;
	private String currentDesc;

	static {
		ModularLogger.instance.addLogger(VoidMonster.instance, LOGGER_ID);

		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			loadColorData();
		}

		if (ModList.CHROMATICRAFT.isLoaded()) {
			addAlvearyEffect();
		}
	}

	public VoidMonsterBee() {
		super(EnumChatFormatting.OBFUSCATED+"VoidMonster", "bee.voidmonster", "Mali Sub Orbis Terrarum", "Reika", new BeeBranch("branch.voidmonster", "Void", "Void", "Is breeding these even slightly advisable?"));

		this.register();
		beeType = this;

		if (ModList.MAGICBEES.isLoaded()) {
			this.addBreeding("Draconic", ModList.MAGICBEES, "Withering", ModList.MAGICBEES, 4);
			this.addSpecialty(ReikaItemHelper.lookupItem("MagicBees:miscResources:5"), 8);
			this.addSpecialty(ReikaItemHelper.lookupItem("MagicBees:miscResources:3"), 8);
			this.addProduct(ReikaItemHelper.lookupItem("MagicBees:comb:6"), 10);
		}
		else {
			this.addBreeding("Ended", "Demonic", 1);
			this.addProduct(new ItemStack(Blocks.soul_sand), 1);
		}

		this.addProduct(ReikaItemHelper.lookupItem("Forestry:ash"), 1);
		this.addProduct(ForestryHandler.Combs.SIMMERING.getItem(), 20);
	}

	@ModDependent(ModList.CHROMATICRAFT)
	private static void addAlvearyEffect() {
		voidMonsterAlvearyEffect = new LumenAlvearyEffect("voidmonster", CrystalElement.PINK, 200){

			@Override
			protected boolean ticksOnClient() {
				return true;
			}

			@Override
			protected boolean isActive(TileEntityLumenAlveary te) {
				return super.isActive(te) && te.getSpecies() == beeType;
			}

			@Override
			@SideOnly(Side.CLIENT)
			protected void clientTick(TileEntityLumenAlveary te) {
				if (te.getSpecies() == beeType && te.canQueenWork()) {
					ChunkCoordinates c = te.getMultiblockLogic().getController().getCoordinates();
					double d = Minecraft.getMinecraft().thePlayer.getDistanceSq(c.posX+0.5, c.posY+0.5, c.posZ+0.5);
					int n = d < 256 ? 4 : d < 1024 ? 3 : 2;
					for (int i = 0; i < n; i++) {
						float s = (float)ReikaRandomHelper.getRandomBetween(2.5, 5);
						int l = ReikaRandomHelper.getRandomBetween(20, 100);
						double px = ReikaRandomHelper.getRandomPlusMinus(c.posX+0.5, 1);
						double py = ReikaRandomHelper.getRandomPlusMinus(c.posY+0.5, 1);
						double pz = ReikaRandomHelper.getRandomPlusMinus(c.posZ+0.5, 1);
						EntityCCBlurFX fx = new EntityCCBlurFX(te.worldObj, px, py, pz);
						fx.setColor(0x000000).setBasicBlend().setScale(s).setLife(l);
						double v = ReikaRandomHelper.getRandomBetween(0.03125, 0.0625);
						double[] xyz = ReikaPhysicsHelper.polarToCartesian(v, 0, te.worldObj.rand.nextDouble()*360);
						fx.motionX = xyz[0];
						fx.motionZ = xyz[2];
						fx.motionY = ReikaRandomHelper.getRandomPlusMinus(0, 0.0625);
						fx.setIcon(ChromaIcons.FADE_BASICBLEND).setAlphaFading();
						if (d < 64)
							fx.setNoDepthTest();
						Minecraft.getMinecraft().effectRenderer.addEffect(fx);
					}
				}
			}

			@Override
			public String getDescription() {
				return "Void Imitation";
			}

		};
	}

	@SideOnly(Side.CLIENT)
	private static void loadColorData() {
		beeColor = new ColorBlendList(18F, 0x000000, 0x6000e0, 0x000000, 0xb00000, 0x000000, 0xff00ff, 0x000000, 0xa0a000);
	}

	private final class AlleleVoid extends BasicGene implements IAlleleFlowers {

		private final FlowerProviderVoid flowers = new FlowerProviderVoid();

		public AlleleVoid() {
			super("flower.void", "Void", EnumBeeChromosome.FLOWER_PROVIDER);
		}

		@Override
		public IFlowerProvider getProvider() {
			return flowers;
		}
	}

	private final class FlowerProviderVoid extends BasicFlowerProvider implements IFlowerAcceptableRule {

		private FlowerProviderVoid() {
			super(Blocks.command_block, "void");
			FlowerManager.flowerRegistry.registerAcceptableFlowerRule(this, this.getFlowerType());
		}

		@Override
		public String getDescription() {
			return "The Void";
		}

		@Override
		public boolean isAcceptableFlower(String type, World world, int x, int y, int z) {
			return y < 0 || (world.provider.isHellWorld && y >= 128);
		}
	}

	static final class AlleleVoidEffect extends BasicGene implements IAlleleBeeEffect {

		public AlleleVoidEffect() {
			super("effect.void", "Aura of the Void", EnumBeeChromosome.EFFECT);
		}

		@Override
		public boolean isCombinable() {
			return false;
		}

		@Override
		public IEffectData validateStorage(IEffectData ied) {
			return ied;
		}

		@Override
		public IEffectData doEffect(IBeeGenome ibg, IEffectData ied, IBeeHousing ibh) {
			World world = ibh.getWorld();
			ChunkCoordinates cc = ibh.getCoordinates();
			Collection<EntityVoidMonster> c = VoidMonster.getCurrentMonsterList(world);
			if (c != null && !c.isEmpty()) {
				for (EntityVoidMonster e : c) {
					if (world.provider.isHellWorld) {
						e.moveTowards(cc.posX, cc.posY, cc.posZ, 2);
					}
					else {
						if (e.getDistanceSq(cc.posX+0.5, cc.posY+0.5, cc.posZ+0.5) < 256) {
							e.attackEntityFrom(new VoidMonsterDamage(e), 50);
						}
					}
				}
			}
			return ied;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public IEffectData doFX(IBeeGenome ibg, IEffectData ied, IBeeHousing ibh) {
			World world = ibh.getWorld();
			ChunkCoordinates c = ibh.getCoordinates();


			return ied;
		}
	}

	@Override
	public String getDescription() {
		long time = System.currentTimeMillis();
		if (time >= nextDescTime) {
			nextDescTime = time+ReikaRandomHelper.getRandomBetween(50, 10000);
			currentDesc = this.getRandomString(36);
		}
		return currentDesc;
	}

	private static String getRandomString(int maxlen) {
		StringBuilder sb = new StringBuilder();
		while (sb.length() < maxlen) {
			int len = Math.min(maxlen-sb.length(), ReikaRandomHelper.getRandomBetween(4, 8));
			while (len > 0 && sb.length() < maxlen) {
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
	public EnumTemperature getTemperature() {
		return EnumTemperature.HELLISH;
	}

	@Override
	public EnumHumidity getHumidity() {
		return EnumHumidity.ARID;
	}

	@Override
	public boolean hasEffect() {
		return true;
	}

	@Override
	public boolean isSecret() {
		return false;
	}

	@Override
	public boolean isCounted() {
		return true;
	}

	@Override
	public int getOutlineColor() {
		return beeColor != null ? beeColor.getColor(DragonAPICore.getSystemTimeAsInt()/25D) : 0x000000;
	}

	@Override
	public boolean isDominant() {
		return true;
	}

	@Override
	public boolean isNocturnal() {
		return false;
	}

	@Override
	public boolean isJubilant(IBeeGenome ibg, IBeeHousing ibh) {
		World world = ibh.getWorld();
		ChunkCoordinates c = ibh.getCoordinates();
		return (c.posY <= 1 || (c.posY >= 128 && world.provider.isHellWorld)) || (ModList.CHROMATICRAFT.isLoaded() && this.isLumenAlveary(ibh, world, c));
	}

	@ModDependent(ModList.CHROMATICRAFT)
	private boolean isLumenAlveary(IBeeHousing ibh, World world, ChunkCoordinates cc) {
		TileEntityLumenAlveary te = ChromaBeeHelpers.getLumenAlvearyController(ibh, world, cc);
		return te != null && te.getActiveEffects().contains(voidMonsterAlvearyEffect);
	}

	@Override
	public IAlleleFlowers getFlowerAllele() {
		return voidflower;
	}

	@Override
	public Speeds getProductionSpeed() {
		return Speeds.SLOWEST;
	}

	@Override
	public Fertility getFertility() {
		return Fertility.LOW;
	}

	@Override
	public Flowering getFloweringRate() {
		return Flowering.FASTER;
	}

	@Override
	public Life getLifespan() {
		return Life.LONGEST;
	}

	@Override
	public Territory getTerritorySize() {
		return Territory.DEFAULT;
	}

	@Override
	public boolean isCaveDwelling() {
		return true;
	}

	@Override
	public int getTemperatureTolerance() {
		return 0;
	}

	@Override
	public int getHumidityTolerance() {
		return 0;
	}

	@Override
	public Tolerance getHumidityToleranceDir() {
		return Tolerance.NONE;
	}

	@Override
	public Tolerance getTemperatureToleranceDir() {
		return Tolerance.NONE;
	}

	@Override
	public IAlleleBeeEffect getEffectAllele() {
		return voideffect;
	}

	@Override
	public boolean isTolerantFlyer() {
		return false;
	}

	@Override
	public int getBeeStripeColor() {
		return ReikaColorAPI.mixColors(this.getOutlineColor(), 0x000000, 0.2F);
	}

}
