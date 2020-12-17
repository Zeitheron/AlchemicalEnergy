package org.zeith.comm3.alcheng;

import com.zeitheron.hammercore.HammerCore;
import com.zeitheron.hammercore.mod.ModuleLister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeith.comm3.alcheng.blocks.BlockAlchemicalEnergy;
import org.zeith.comm3.alcheng.compat.BaseCompatAE;
import org.zeith.comm3.alcheng.init.*;
import org.zeith.comm3.alcheng.proxy.CommonProxyAE;

import java.util.ArrayList;
import java.util.List;

@Mod(modid = InfoAE.MOD_ID, version = "@VERSION@", name = "Alchemical Energy", certificateFingerprint = HammerCore.CERTIFICATE_FINGERPRINT, updateJSON = "http://dccg.herokuapp.com/api/fmluc/427755", dependencies = "required-after:hammercore")
public class AlchemicalEnergyMod
{
	@SidedProxy(clientSide = "org.zeith.comm3.alcheng.proxy.ClientProxyAE", serverSide = "org.zeith.comm3.alcheng.proxy.CommonProxyAE")
	public static CommonProxyAE proxy;

	public static final Logger LOG = LogManager.getLogger("AlchemicalEnergy");

	public static List<BaseCompatAE> compats;

	public static final CreativeTabs TAB = new CreativeTabs(InfoAE.MOD_ID)
	{
		@Override
		public void displayAllRelevantItems(NonNullList<ItemStack> items)
		{
			UniversalBucket ub = ForgeModContainer.getInstance().universalBucket;
			List<Fluid> fluids = new ArrayList<>();

			fluids.add(FluidsAE.ALCHEMICAL_ENERGY);

			for(Item item : Item.REGISTRY)
			{
				item.getSubItems(this, items);
				if(item == ub)
					for(Fluid fl : fluids)
						items.add(FluidUtil.getFilledBucket(new FluidStack(fl, Fluid.BUCKET_VOLUME)));
			}
		}

		@Override
		public ItemStack createIcon()
		{
			return new ItemStack(BlocksAE.ALCHEMICAL_CONDENSER);
		}
	};

	@Mod.EventHandler
	public void certificateViolation(FMLFingerprintViolationEvent e)
	{
		LOG.warn("*****************************");
		LOG.warn("WARNING: Somebody has been tampering with HammerCore jar!");
		LOG.warn("It is highly recommended that you redownload mod from https://www.curseforge.com/projects/427755 !");
		LOG.warn("*****************************");
		HammerCore.invalidCertificates.put("alcheng", "https://www.curseforge.com/projects/427755");
	}

	@Mod.EventHandler
	public void construct(FMLConstructionEvent e)
	{
		RegistriesAE.init();
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent e)
	{
		BlockAlchemicalEnergy.transmuteBlock(BlockAlchemicalEnergy.blocksSource(Blocks.DIRT, Blocks.GRASS), Blocks.SAND.getDefaultState(), false);
		BlockAlchemicalEnergy.transmuteBlock(BlockAlchemicalEnergy.blockSource(Blocks.SAND), Blocks.GRAVEL.getDefaultState(), false);
		BlockAlchemicalEnergy.transmuteBlock(BlockAlchemicalEnergy.blockSource(Blocks.GRAVEL), Blocks.COBBLESTONE.getDefaultState(), false);

		compats = ModuleLister.createModules(BaseCompatAE.class, "", e.getAsmData());

		FluidsAE.register();
		BlocksAE.register();
		ItemsAE.register();
		SoundsAE.register();

		proxy.preInit();
		compats.forEach(BaseCompatAE::preInit);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent e)
	{
		proxy.init();
		compats.forEach(BaseCompatAE::init);
	}

	@Mod.EventHandler
	public void loadComplete(FMLLoadCompleteEvent e)
	{
		compats.forEach(BaseCompatAE::onLoadComplete);
	}
}