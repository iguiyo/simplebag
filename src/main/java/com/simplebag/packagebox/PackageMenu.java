package com.simplebag.packagebox;

import com.simplebag.Simplebag;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Prediction;

public class PackageMenu extends AbstractContainerMenu {
	public static final int CONTAINER_SLOTS = PackageData.SLOTS;
	public static final int INV_START = CONTAINER_SLOTS;
	public static final int HOTBAR_START = INV_START + 27;
	public static final int TOTAL_SLOTS = HOTBAR_START + 9;
	public static final int GUI_WIDTH = 232;
	public static final int GUI_HEIGHT = 296;

	private final Container container;
	private final ItemStack opened;
	private final int lockedSlotId;

	public PackageMenu(int syncId, Inventory playerInventory) {
		this(syncId, playerInventory, new SimpleContainer(CONTAINER_SLOTS), -1, ItemStack.EMPTY);
	}

	public static PackageMenu createForHand(
		int syncId,
		Inventory playerInventory,
		PackageInventory container,
		int sourceSlot,
		ItemStack opened
	) {
		return new PackageMenu(syncId, playerInventory, container, sourceSlot, opened);
	}

	private PackageMenu(int syncId, Inventory playerInventory, Container container, int sourceSlot, ItemStack opened) {
		super(Simplebag.PACKAGE_MENU, syncId);
		this.container = container;
		this.opened = opened.copy();

		for (int slot = 0; slot < CONTAINER_SLOTS; slot++) {
			addSlot(new PackageSlot(container, slot, 8 + slot % 12 * 18, 18 + slot / 12 * 18));
		}
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				addSlot(new Slot(playerInventory, column + row * 9 + 9, 35 + column * 18, 212 + row * 18));
			}
		}
		for (int column = 0; column < 9; column++) {
			addSlot(new Slot(playerInventory, column, 35 + column * 18, 270));
		}

		this.lockedSlotId = menuSlotFor(sourceSlot);
		if (lockedSlotId >= 0 && lockedSlotId < slots.size()) {
			Slot original = slots.get(lockedSlotId);
			LockedSlot locked = new LockedSlot(original.container, original.getContainerSlot(), original.x, original.y);
			locked.index = original.index;
			slots.set(lockedSlotId, locked);
		}
	}

	public static int menuSlotFor(int inventorySlot) {
		if (inventorySlot >= 9 && inventorySlot <= 35) {
			return INV_START + inventorySlot - 9;
		}
		if (inventorySlot >= 0 && inventorySlot <= 8) {
			return HOTBAR_START + inventorySlot;
		}
		return -1;
	}

	@Override
	public boolean stillValid(Player player) {
		return container.stillValid(player);
	}

	@Override
	public void clicked(int slotId, int button, ContainerInput input, Player player) {
		if (touchesOpenedPackage(slotId)) {
			sendAllDataToRemote();
			return;
		}

		boolean touchedForbidden = touchesForbidden(slotId, button, input, player);
		super.clicked(slotId, button, input, player);
		if (player instanceof ServerPlayer) {
			ejectForbidden(player);
		}
		if (touchedForbidden) {
			sendAllDataToRemote();
		}
	}

	@Override
	public void removed(Player player) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			super.removed(player);
			return;
		}

		ejectForbidden(player);
		saveContents(serverPlayer);
		serverPlayer.level().playSound(
			null,
			serverPlayer.blockPosition(),
			SoundEvents.SHULKER_BOX_CLOSE,
			SoundSource.PLAYERS,
			1.0F,
			1.0F
		);
		super.removed(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);
		if (!slot.hasItem()) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = slot.getItem();
		ItemStack original = stack.copy();
		if (index < CONTAINER_SLOTS) {
			if (!moveItemStackTo(stack, INV_START, TOTAL_SLOTS, true)) {
				return ItemStack.EMPTY;
			}
		} else if (!moveItemStackTo(stack, 0, CONTAINER_SLOTS, false)) {
			return ItemStack.EMPTY;
		}

		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		slot.onTake(player, stack);
		return original;
	}

	private boolean touchesOpenedPackage(int slotId) {
		if (slotId < 0 || slotId >= slots.size()) {
			return false;
		}
		return slotId == lockedSlotId || isOpenedPackage(slots.get(slotId).getItem());
	}

	private boolean isOpenedPackage(ItemStack stack) {
		if (!(stack.getItem() instanceof PackageItem)) {
			return false;
		}
		return opened.isEmpty() || ItemStack.isSameItemSameComponents(stack, opened);
	}

	private boolean touchesForbidden(int slotId, int button, ContainerInput input, Player player) {
		if (PackageData.isForbidden(getCarried())) {
			return true;
		}
		if (slotId >= 0 && slotId < CONTAINER_SLOTS && PackageData.isForbidden(container.getItem(slotId))) {
			return true;
		}
		if (input == ContainerInput.SWAP && button >= 0 && button < 9
			&& PackageData.isForbidden(player.getInventory().getItem(button))) {
			return true;
		}
		if (slotId >= 0 && slotId < slots.size() && PackageData.isForbidden(slots.get(slotId).getItem())) {
			return true;
		}
		return false;
	}

	private void ejectForbidden(Player player) {
		for (ItemStack stack : PackageData.extractForbidden(container)) {
			if (!player.getInventory().add(stack)) {
				player.drop(stack, false, Prediction.SERVER_ONLY);
			}
		}
	}

	private void saveContents(ServerPlayer player) {
		Inventory playerInventory = player.getInventory();
		int packageSlot = findPackageSlot(playerInventory);
		if (packageSlot < 0) {
			for (ItemStack stack : contents()) {
				if (!stack.isEmpty() && !playerInventory.add(stack)) {
					player.drop(stack, false, Prediction.SERVER_ONLY);
				}
			}
			return;
		}

		ItemStack current = playerInventory.getItem(packageSlot);
		playerInventory.setItem(packageSlot, PackageData.write(contents(), current));
		playerInventory.setChanged();
	}

	private int findPackageSlot(Inventory playerInventory) {
		if (lockedSlotId >= 0) {
			int inventorySlot = lockedSlotId < HOTBAR_START
				? lockedSlotId - INV_START + 9
				: lockedSlotId - HOTBAR_START;
			if (inventorySlot >= 0 && inventorySlot < playerInventory.getContainerSize()
				&& matchesOpenedPackage(playerInventory.getItem(inventorySlot))) {
				return inventorySlot;
			}
		}
		for (int slot = 0; slot < playerInventory.getContainerSize(); slot++) {
			if (matchesOpenedPackage(playerInventory.getItem(slot))) {
				return slot;
			}
		}
		return -1;
	}

	private boolean matchesOpenedPackage(ItemStack stack) {
		return stack.getItem() instanceof PackageItem
			&& (opened.isEmpty() || ItemStack.isSameItemSameComponents(stack, opened));
	}

	private List<ItemStack> contents() {
		if (container instanceof PackageInventory packageInventory) {
			return packageInventory.contents();
		}
		return java.util.Collections.nCopies(0, ItemStack.EMPTY);
	}

	public static class PackageSlot extends Slot {
		public PackageSlot(Container container, int slot, int x, int y) {
			super(container, slot, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return !PackageData.isForbidden(stack);
		}
	}

	public static final class LockedSlot extends PackageSlot {
		public LockedSlot(Container container, int slot, int x, int y) {
			super(container, slot, x, y);
		}

		@Override
		public boolean mayPickup(Player player) {
			return false;
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return false;
		}
	}
}
