package org.zeith.comm3.alcheng.inventory;

import com.zeitheron.hammercore.client.gui.GuiFluidTank;
import com.zeitheron.hammercore.client.gui.GuiWTFMojang;
import com.zeitheron.hammercore.client.gui.GuiWidgets;
import com.zeitheron.hammercore.client.gui.impl.container.SlotScaled;
import com.zeitheron.hammercore.client.utils.RenderUtil;
import com.zeitheron.hammercore.client.utils.UtilsFX;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.zeith.comm3.alcheng.init.InfoAE;
import org.zeith.comm3.alcheng.tiles.TileAlchemicalCondenser;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AlchemicalCondenser
{
	@SideOnly(Side.CLIENT)
	public static GuiContainer createGUI(TileAlchemicalCondenser tile, EntityPlayer player)
	{
		return new Gui(tile, player);
	}

	public static Container createContainer(TileAlchemicalCondenser tile, EntityPlayer player)
	{
		return new Inventory(tile, player);
	}

	@SideOnly(Side.CLIENT)
	static class Gui
			extends GuiWTFMojang<Container>
	{
		static final ResourceLocation WIDGETS = new ResourceLocation(InfoAE.MOD_ID, "textures/gui/widgets.png");
		static final ResourceLocation TEX = new ResourceLocation(InfoAE.MOD_ID, "textures/gui/alchemical_condenser.png");

		private final TileAlchemicalCondenser tile;
		private final EntityPlayer player;

		public GuiFluidTank tank;
		public final Rectangle autoExtract = new Rectangle();

		public Gui(TileAlchemicalCondenser tile, EntityPlayer player)
		{
			super(createContainer(tile, player));
			this.tile = tile;
			this.player = player;
			this.xSize = 176;
			this.ySize = 166;
		}

		@Override
		public void initGui()
		{
			super.initGui();
			this.tank = new GuiFluidTank(guiLeft + 120, guiTop + 11, 16, 64, tile.fluid);
			this.autoExtract.setBounds(guiLeft + 12, guiTop + 10, 20, 20);
		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
		{
			UtilsFX.bindTexture(TEX);
			RenderUtil.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

			float h = (float) (tile.energy.getFilledProgress() * 64);

			GuiWidgets.drawFurnaceArrow(guiLeft + 90, guiTop + 35, tile.getProgress(partialTicks));
			GuiWidgets.drawEnergy(guiLeft + 41, guiTop + 11 + 64 - h, 8, h, GuiWidgets.EnumPowerAnimation.UP);

			UtilsFX.bindTexture(WIDGETS);
			RenderUtil.drawTexturedModalRect(autoExtract.x, autoExtract.y, 0, autoExtract.contains(mouseX, mouseY) ? 20 : 0, autoExtract.width, autoExtract.height);
			RenderUtil.drawTexturedModalRect(autoExtract.x + 2, autoExtract.y + 2, 22, 2 + (tile.autoExtract.get() ? 0 : 20), autoExtract.width - 4, autoExtract.height - 4);

			tank.render(mouseX, mouseY);
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
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(-guiLeft, -guiTop, 0);

			if(tank.postRender(mouseX, mouseY))
				drawHoveringText(tank.getTooltip(mouseX, mouseY), mouseX, mouseY);

			if(mouseX >= guiLeft + 41 && mouseY >= guiTop + 11 && mouseX < guiLeft + 41 + 8 && mouseY < guiTop + 11 + 64)
			{
				int j1 = guiLeft + 41, k1 = guiTop + 11;
				this.drawGradientRect(j1, k1, j1 + 8, k1 + 64, -2130706433, -2130706433);

				List<String> text = new ArrayList<>(2);
				text.add(String.format("FE: %,d/%,d", tile.energy.getEnergy(), tile.energy.getCapacity()));
				if(tile.recipe != null)
				{
					text.add(String.format("FE/tick: %,d", tile.computeEnergyRate(tile.recipe.energyRate)));
				}
				drawHoveringText(text, mouseX, mouseY);
			}

			if(autoExtract.contains(mouseX, mouseY))
			{
				drawHoveringText("Auto-extract: " + (tile.autoExtract.get() ? "ON" : "OFF"), mouseX, mouseY);
			}

			GlStateManager.popMatrix();

			InventoryPlayer inventoryplayer = this.mc.player.inventory;
			ItemStack mouse = this.draggedStack.isEmpty() ? inventoryplayer.getItemStack() : this.draggedStack;

			if(!mouse.isEmpty())
			{
				int color = tile.upgrades.isItemValidForSlot(0, mouse) ? 0x8044ff44 : 0x80ff4444;

				List<Slot> sls = inventorySlots.inventorySlots;
				for(int i = 1; i < 6; ++i)
				{
					Slot slot = sls.get(i);

					GlStateManager.disableLighting();
					GlStateManager.disableDepth();
					int j1 = slot.xPos;
					int k1 = slot.yPos;
					GlStateManager.colorMask(true, true, true, false);
					if(slot instanceof SlotScaled)
						this.drawGradientRect(j1, k1, j1 + ((SlotScaled) slot).getWidth(), k1 + ((SlotScaled) slot).getHeight(), color, color);
					else
						this.drawGradientRect(j1, k1, j1 + 16, k1 + 16, color, color);
					GlStateManager.colorMask(true, true, true, true);
					GlStateManager.enableLighting();
					GlStateManager.enableDepth();
				}
			}
		}
	}

	static class Inventory
			extends Container
	{
		private final TileAlchemicalCondenser tile;

		public Inventory(TileAlchemicalCondenser tile, EntityPlayer player)
		{
			this.tile = tile;

			addSlotToContainer(new SlotScaled(tile.items, 0, 58, 31, 24, 24));
			for(int i = 0; i < 5; ++i)
				addSlotToContainer(new SlotScaled(tile.upgrades, i, 156, 11 + 14 * i, 12, 12));
			addInventorySlots(player, 8, 84);
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
			return tile.items.isUsableByPlayer(playerIn, tile.getPos());
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