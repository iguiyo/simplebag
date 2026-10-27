package com.simplebag.packagebox;

import com.simplebag.Simplebag;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;

public final class PackageData {
	public static final int SLOTS = 120;
	public static final int COLUMNS = 12;
	public static final int ROWS = 10;
	public static final int TOOLTIP_TYPES = 12;
	public static final Set<String> FORBIDDEN_KEYWORDS = Set.of(
		"shulker", "backpack", "package", "parcel", "bundle", "satchel", "pouch", "crate", "bag"
	);

	private PackageData() {
	}

	public static NonNullList<ItemStack> read(ItemStack packageStack) {
		NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
		ItemContainerContents contents = packageStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
		contents.copyInto(items);
		return items;
	}

	public static ItemStack write(List<ItemStack> items, ItemStack packageStack) {
		ItemStack result = packageStack.copy();
		result.setCount(1);
		result.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
		result.set(DataComponents.LORE, new ItemLore(loreOf(items)));
		return result;
	}

	public static List<Component> loreOf(List<ItemStack> contents) {
		List<ItemStack> samples = new ArrayList<>();
		List<Integer> counts = new ArrayList<>();
		int total = 0;

		for (ItemStack stack : contents) {
			if (stack.isEmpty()) {
				continue;
			}
			total += stack.getCount();
			int existing = -1;
			for (int i = 0; i < samples.size(); i++) {
				if (ItemStack.isSameItemSameComponents(samples.get(i), stack)) {
					existing = i;
					break;
				}
			}
			if (existing >= 0) {
				counts.set(existing, counts.get(existing) + stack.getCount());
			} else {
				samples.add(stack.copyWithCount(1));
				counts.add(stack.getCount());
			}
		}

		List<Component> lore = new ArrayList<>();
		if (total == 0) {
			lore.add(Component.translatable("tooltip.simplebag.empty"));
			return lore;
		}

		lore.add(Component.translatable("tooltip.simplebag.summary", total, samples.size()));

		List<Integer> order = new ArrayList<>(samples.size());
		for (int i = 0; i < samples.size(); i++) {
			order.add(i);
		}
		order.sort(Comparator.<Integer>comparingInt(counts::get).reversed()
			.thenComparing(i -> samples.get(i).getHoverName().getString()));

		int shown = Math.min(TOOLTIP_TYPES, order.size());
		for (int i = 0; i < shown; i++) {
			int index = order.get(i);
			lore.add(Component.translatable(
				"tooltip.simplebag.entry",
				samples.get(index).getHoverName(),
				counts.get(index)
			));
		}
		if (shown < order.size()) {
			lore.add(Component.translatable("tooltip.simplebag.more", order.size() - shown));
		}
		lore.add(Component.translatable("tooltip.simplebag.forbidden_hint"));
		return lore;
	}

	public static boolean isForbidden(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		if (stack.is(Simplebag.PACKAGE_BOX)) {
			return true;
		}
		if (stack.has(DataComponents.CONTAINER) || stack.has(DataComponents.BUNDLE_CONTENTS)) {
			return true;
		}

		Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
		String path = itemId.getPath().toLowerCase(Locale.ROOT);
		for (String keyword : FORBIDDEN_KEYWORDS) {
			if (path.contains(keyword)) {
				return true;
			}
		}
		return false;
	}

	public static List<ItemStack> extractForbidden(Container container) {
		List<ItemStack> extracted = new ArrayList<>();
		for (int slot = 0; slot < Math.min(SLOTS, container.getContainerSize()); slot++) {
			ItemStack stack = container.getItem(slot);
			if (isForbidden(stack)) {
				container.setItem(slot, ItemStack.EMPTY);
				extracted.add(stack);
			}
		}
		return extracted;
	}
}
