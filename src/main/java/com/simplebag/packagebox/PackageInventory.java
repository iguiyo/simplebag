package com.simplebag.packagebox;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class PackageInventory extends SimpleContainer {
	public PackageInventory(List<ItemStack> contents) {
		super(PackageData.SLOTS);
		for (int slot = 0; slot < PackageData.SLOTS && slot < contents.size(); slot++) {
			ItemStack stack = contents.get(slot);
			if (!stack.isEmpty()) {
				setItem(slot, stack);
			}
		}
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return !PackageData.isForbidden(stack);
	}

	public List<ItemStack> contents() {
		List<ItemStack> result = new ArrayList<>(PackageData.SLOTS);
		for (int slot = 0; slot < getContainerSize(); slot++) {
			result.add(getItem(slot));
		}
		return result;
	}
}
