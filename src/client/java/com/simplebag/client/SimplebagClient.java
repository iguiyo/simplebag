package com.simplebag.client;

import com.simplebag.Simplebag;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class SimplebagClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(Simplebag.PACKAGE_MENU, PackageScreen::new);
	}
}
