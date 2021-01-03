package org.zeith.comm3.alcheng.inventory;

import com.zeitheron.hammercore.client.gui.GuiFluidTank;
import com.zeitheron.hammercore.client.gui.GuiWTFMojang;
import com.zeitheron.hammercore.client.utils.RenderUtil;
import com.zeitheron.hammercore.client.utils.UtilsFX;
import com.zeitheron.hammercore.compat.jei.IJeiHelper;
import com.zeitheron.hammercore.net.HCNet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.zeith.comm3.alcheng.init.InfoAE;
import org.zeith.comm3.alcheng.tiles.TileAlchemicalCreator;

import java.awt.*;
import java.io.IOException;

public class AlchemicalCreator
{
	@SideOnly(Side.CLIENT)
	public static GuiContainer createGUI(TileAlchemicalCreator tile, EntityPlayer player)
	{
		return new Gui(tile, player);
	}

	public static Container createContainer(TileAlchemicalCreator tile, EntityPlayer player)
	{
		return new Inventory(tile, player);
	}

	@SideOnly(Side.CLIENT)
	public static class Gui
			extends GuiWTFMojang<Container>
	{
		static final ResourceLocation WIDGETS = new ResourceLocation(InfoAE.MOD_ID, "textures/gui/widgets.png");
		static final ResourceLocation TEX = new ResourceLocation(InfoAE.MOD_ID, "textures/gui/alchemical_creator.png");

		private final TileAlchemicalCreator tile;
		private final EntityPlayer player;

		public GuiFluidTank tank;
		public final Rectangle autoExtract = new Rectangle();
		int mouseX, mouseY;

		public Gui(TileAlchemicalCreator tile, EntityPlayer player)
		{
			super(createContainer(tile, player));
			this.tile = tile;
			this.player = player;
			this.xSize = 176;
			this.ySize = 128;
		}

		@Override
		public void initGui()
		{
			super.initGui();
			this.tank = new GuiFluidTank(guiLeft + 8, guiTop + 16, 139, 16, tile.fluid);
			this.autoExtract.setBounds(guiLeft + 149, guiTop + 14, 20, 20);
		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
		{
			this.mouseX = mouseX;
			this.mouseY = mouseY;

			UtilsFX.bindTexture(TEX);
			RenderUtil.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

			UtilsFX.bindTexture(WIDGETS);
			RenderUtil.drawTexturedModalRect(autoExtract.x, autoExtract.y, 0, autoExtract.contains(mouseX, mouseY) ? 20 : 0, autoExtract.width, autoExtract.height);
			RenderUtil.drawTexturedModalRect(autoExtract.x + 2, autoExtract.y + 2, 22, 2 + (tile.autoExtract.get() ? 0 : 20), autoExtract.width - 4, autoExtract.height - 4);

			GlStateManager.pushMatrix();
			GlStateManager.translate(tank.x + tank.width, tank.y, 0);
			GlStateManager.rotate(90, 0, 0, 1);
			tank.x = tank.y = 0;
			tank.width = 16;
			tank.height = 139;
			tank.render(mouseX, mouseY);
			tank.x = guiLeft + 8;
			tank.y = guiTop + 16;
			tank.width = 139;
			tank.height = 16;
			GlStateManager.popMatrix();
		}

		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
		{
			this.fontRenderer.drawString(tile.getBlockType().getLocalizedName(), 8, 6, 4210752);
			this.fontRenderer.drawString(player.inventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 4, 4210752);

			GlStateManager.pushMatrix();
			GlStateManager.translate(-guiLeft, -guiTop, 200);

			if(tank.postRender(mouseX, mouseY))
				drawHoveringText(tank.getTooltip(mouseX, mouseY), mouseX, mouseY);

			if(autoExtract.contains(mouseX, mouseY) && HCNet.getMouseStack(Minecraft.getMinecraft().player).isEmpty())
				drawHoveringText("Auto-extract: " + (tile.autoExtract.get() ? "ON" : "OFF"), mouseX, mouseY);

			GlStateManager.popMatrix();
		}

		@Override
		protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
		{
			super.mouseClicked(mouseX, mouseY, mouseButton);

			if(autoExtract.contains(mouseX, mouseY))
			{
				mc.playerController.sendEnchantPacket(inventorySlots.windowId, 1);
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1F));
			}
		}

		@Override
		protected void keyTyped(char typedChar, int keyCode) throws IOException
		{
			IJeiHelper helper = IJeiHelper.instance();
			if(helper.getKeybind_showRecipes() != null)
			{
				KeyBinding showRecipe = (KeyBinding) helper.getKeybind_showRecipes();
				KeyBinding showUses = (KeyBinding) helper.getKeybind_showUses();

				// Show uses/recipes
				if(tank.isHovered(mouseX, mouseY))
				{
					FluidStack fs = tile.fluid.getFluid();

					if(fs != null)
					{
						FluidStack nfs = new FluidStack(fs.getFluid(), Fluid.BUCKET_VOLUME);
						if(showRecipe.getKeyCode() == keyCode)
							helper.showRecipes(nfs);
						else if(showUses.getKeyCode() == keyCode)
							helper.showUses(nfs);
					}
				}
			}

			super.keyTyped(typedChar, keyCode);
		}
	}

	public static class Inventory
			extends Container
	{
		private final TileAlchemicalCreator tile;

		public Inventory(TileAlchemicalCreator tile, EntityPlayer player)
		{
			this.tile = tile;
			addInventorySlots(player, 8, 46);
		}

		@Override
		public boolean enchantItem(EntityPlayer playerIn, int id)
		{
			if(id == 1)
			{
				tile.autoExtract.set(!tile.autoExtract.get());
				return true;
			}

			return false;
		}

		@Override
		public boolean canInteractWith(EntityPlayer playerIn)
		{
			return playerIn.getDistanceSq(tile.getPos()) < 64.0D;
		}

		protected void addInventorySlots(EntityPlayer player, int x, int y)
		{
			for(int i = 0; i < 9; ++i)
				addSlotToContainer(new Slot(player.inventory, i, x + i * 18, 58 + y));

			for(int i = 0; i < 3; ++i)
				for(int j = 0; j < 9; ++j)
					addSlotToContainer(new Slot(player.inventory, 9 + j + i * 9, x + 18 * j, y + i * 18));
		}

		@Override
		public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
		{
			return ItemStack.EMPTY;
		}
	}
}