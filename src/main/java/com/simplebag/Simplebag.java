package com.simplebag;

import com.simplebag.packagebox.PackageData;
import com.simplebag.packagebox.PackageItem;
import com.simplebag.packagebox.PackageMenu;
import java.util.List;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Simplebag implements ModInitializer {
	public static final String MOD_ID = "simplebag";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final MenuType<PackageMenu> PACKAGE_MENU = Registry.register(
		BuiltInRegistries.MENU,
		id("package_box"),
		new MenuType<>(PackageMenu::new, FeatureFlags.VANILLA_SET)
	);

	public static final PackageItem PACKAGE_BOX = Registry.register(
		BuiltInRegistries.ITEM,
		id("package_box"),
		new PackageItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id("package_box")))
			.stacksTo(1)
			.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
			.component(DataComponents.LORE, new ItemLore(PackageData.loreOf(List.of()))))
	);

	@Override
	public void onInitialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output ->
			output.accept(new ItemStack(PACKAGE_BOX), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS)
		);
		LOGGER.info("Simple Package Box loaded");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
