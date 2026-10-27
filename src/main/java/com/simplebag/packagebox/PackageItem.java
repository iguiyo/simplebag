package com.simplebag.packagebox;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PackageItem extends Item {
	public PackageItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack packageStack = player.getItemInHand(hand);
		if (!(packageStack.getItem() instanceof PackageItem)) {
			return InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.containerMenu != serverPlayer.inventoryMenu) {
			return InteractionResult.FAIL;
		}

		Inventory inventory = serverPlayer.getInventory();
		int sourceSlot = hand == InteractionHand.MAIN_HAND ? inventory.getSelectedSlot() : 40;
		if (sourceSlot < 0 || sourceSlot >= inventory.getContainerSize()) {
			return InteractionResult.FAIL;
		}

		PackageInventory packageInventory = new PackageInventory(PackageData.read(packageStack));
		serverPlayer.openMenu(new SimpleMenuProvider(
			(syncId, playerInventory, ignored) -> PackageMenu.createForHand(
				syncId,
				playerInventory,
				packageInventory,
				sourceSlot,
				packageStack
			),
			packageStack.getHoverName()
		));
		level.playSound(null, player.blockPosition(), SoundEvents.SHULKER_BOX_OPEN, SoundSource.PLAYERS, 1.0F, 1.0F);
		return InteractionResult.SUCCESS;
	}
}
