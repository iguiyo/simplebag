package com.simplebag.client;

import com.simplebag.packagebox.PackageMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class PackageScreen extends AbstractContainerScreen<PackageMenu> {
	private static final int BORDER_DARK = 0xFF4A3524;
	private static final int PANEL = 0xFFE9D6B4;
	private static final int INVENTORY_PANEL = 0xFFD6BD93;
	private static final int SLOT_DARK = 0xFF8E714D;
	private static final int SLOT_FILL = 0xFFC4A779;
	private static final int SLOT_LIGHT = 0xFFF2E4C8;

	public PackageScreen(PackageMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, PackageMenu.GUI_WIDTH, PackageMenu.GUI_HEIGHT);
		this.titleLabelX = 8;
		this.titleLabelY = 6;
		this.inventoryLabelX = 35;
		this.inventoryLabelY = 200;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractBackground(graphics, mouseX, mouseY, delta);
		int x = leftPos;
		int y = topPos;

		graphics.fill(x - 1, y - 1, x + imageWidth + 1, y + imageHeight + 1, BORDER_DARK);
		graphics.fill(x, y, x + imageWidth, y + imageHeight, PANEL);
		graphics.fill(x, y + 196, x + imageWidth, y + imageHeight, INVENTORY_PANEL);
		graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + 2, 0xFFFFF0D8);
		graphics.fill(x + 1, y + imageHeight - 2, x + imageWidth - 1, y + imageHeight - 1, 0xFFB79A70);

		for (Slot slot : menu.slots) {
			int slotX = x + slot.x;
			int slotY = y + slot.y;
			graphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, SLOT_DARK);
			graphics.fill(slotX, slotY, slotX + 16, slotY + 16, SLOT_FILL);
			graphics.fill(slotX, slotY, slotX + 16, slotY + 1, SLOT_LIGHT);
			graphics.fill(slotX, slotY, slotX + 1, slotY + 16, SLOT_LIGHT);
		}
	}
}
