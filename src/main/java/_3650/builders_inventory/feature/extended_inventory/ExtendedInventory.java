package _3650.builders_inventory.feature.extended_inventory;

import _3650.builders_inventory.BuildersInventory;
import _3650.builders_inventory.ModKeybinds;
import _3650.builders_inventory.config.Config;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ExtendedInventory {
	
	public static final ExtendedInventoryContainer PAGE_CONTAINER = new ExtendedInventoryContainer();
	
	/** Not actually meant to be used to void items it's just meant to do nothing if something does */
	public static final Container VOID_CONTAINER = new Container() {
		@Override
		public void clearContent() {}
		@Override
		public boolean stillValid(Player player) {
			return true;
		}
		@Override
		public void setItem(int slot, ItemStack stack) {
			BuildersInventory.LOGGER.error("Something was put inside the void container ({})", stack);
		}
		@Override
		public void setChanged() {}
		@Override
		public ItemStack removeItemNoUpdate(int slot) {
			return ItemStack.EMPTY;
		}
		@Override
		public ItemStack removeItem(int slot, int amount) {
			return ItemStack.EMPTY;
		}
		@Override
		public boolean isEmpty() {
			return true;
		}
		@Override
		public ItemStack getItem(int slot) {
			return ItemStack.EMPTY;
		}
		@Override
		public int getContainerSize() {
			return 1;
		}
	};
	
	public static boolean enabled = false;
	
	public static void refresh() {
		enabled = Config.instance().extended_inventory_enabled;
	}
	
	/*
	 * Button Sprites
	 */
	
	private static final WidgetSprites OPEN_BUTTON_SPRITES = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/button_open"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/button_open_highlighted"));
	
	/*
	 * Creative Screen Switch Button
	 */
	
	public static ExtendedImageButton createOpenButton(int midX, int midY) {
		return new ExtendedImageButton(midX + 57, midY - 28, 12, 12, OPEN_BUTTON_SPRITES, ExtendedInventory::onPressOpenButton);
	}
	
	public static boolean isOpenButtonVisible(CreativeModeTab tab) {
		return enabled && tab.getType() == CreativeModeTab.Type.INVENTORY;
	}
	
	private static void onPressOpenButton(Button button) {
		Minecraft mc = Minecraft.getInstance();
		open(mc);
	}
	
	/*
	 * Page Switching
	 */
	
	public static void switchLeft(ExtendedInventoryScreen screen) {
		int page = getPage();
		if (page > 0) setPage(page - 1);
		refreshScreen(screen);
	}
	
	public static void switchRight(ExtendedInventoryScreen screen) {
		int page = getPage();
		for (int i = 0; page >= ExtendedInventoryPages.size() - 1 && i < 3; i++) {
			ExtendedInventoryPages.create();
		}
		setPage(page + 1);
		refreshScreen(screen);
	}
	
	public static void lock(ExtendedInventoryScreen screen) {
		PAGE_CONTAINER.setLocked(true);
		refreshScreen(screen);
	}
	
	public static void unlock(ExtendedInventoryScreen screen) {
		PAGE_CONTAINER.setLocked(false);
		refreshScreen(screen);
	}
	
	public static void refreshScreen(ExtendedInventoryScreen screen) {
		PAGE_CONTAINER.refresh();
		screen.updateButtons();
		screen.updatePageTitle();
	}
	
	public static void setPage(int page) {
		PAGE_CONTAINER.setPage(page);
	}
	
	public static int getPage() {
		return PAGE_CONTAINER.getPage();
	}
	
	public static String getPageName() {
		return PAGE_CONTAINER.getName();
	}
	
	public static String getPageName(int page) {
		return ExtendedInventoryPages.get(page).getName();
	}
	
	public static void setPageName(String name) {
		PAGE_CONTAINER.setName(name);
	}
	
	public static Component pageTitle(int page) {
		if (page < 0) {
			return Component.translatable("container.builders_inventory.extended_inventory.invalid");
		} else if (!ExtendedInventory.PAGE_CONTAINER.isValid()) {
			return Component.translatable("container.builders_inventory.extended_inventory.invalid.page", page + 1);
		} else {
			String pageName = ExtendedInventory.getPageName(page);
			if (pageName.isBlank()) {
				return Component.translatable("container.builders_inventory.extended_inventory.page", page + 1);
			} else {
				return Component.translatable("container.builders_inventory.extended_inventory.page.custom", pageName, page + 1);
			}
		}
	}
	
	/*
	 * Logic
	 */
	
	public static void clientTick(Minecraft mc) {
		if (!enabled) return;
		
		ExtendedInventoryPages.tick(mc);
		
		if (ModKeybinds.OPEN_EXTENDED_INVENTORY.consumeClick() && mc.gameMode.hasInfiniteItems()) {
			open(mc);
		}
	}
	
	public static void onJoinWorld(ClientPacketListener handler, PacketSender sender, Minecraft mc) {
		if (!enabled) return;
		
		ExtendedInventoryPages.load();
	}
	
	public static void onQuitWorld(ClientPacketListener handler, Minecraft mc) {
		if (!enabled) return;
		
		ExtendedInventoryPages.save();
	}
	
	/*
	 * Utils
	 */
	
	public static void open(Minecraft mc) {
		refresh();
		mc.setScreen(new ExtendedInventoryScreen(mc.player));
	}
	
	public static void close(Minecraft mc) {
		mc.setScreen(new InventoryScreen(mc.player));
	}
	
	public static void openDeleteScreen(Minecraft mc) {
		mc.setScreen(new ExtendedInventoryDeleteScreen(getPage()));
	}
	
	public static void openRenameScreen(Minecraft mc) {
		if (PAGE_CONTAINER.isValid()) mc.setScreen(new ExtendedInventoryRenameScreen(getPage()));
	}
	
	public static void openIconSelectScreen(Minecraft mc) {
		if (PAGE_CONTAINER.isValid()) mc.setScreen(new ExtendedInventoryIconScreen(getPage()));
	}
	
	public static void openOrganizeScreen(Minecraft mc) {
		mc.setScreen(new ExtendedInventoryOrganizeScreen());
	}
	
	public static void swap(Minecraft mc, int slot, int hotbar) {
		if (slot < 36) mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, slot, hotbar, ClickType.SWAP, mc.player);
		else {
			// bad code do not use it (depends on creative inventory listener and technically less efficient)
//			mc.player.getInventory().setItem(
//					hotbar,
//					PAGE_CONTAINER.swapItem(
//							slot - 36,
//							mc.player.getInventory().removeItemNoUpdate(hotbar)));
//			mc.player.inventoryMenu.broadcastChanges();
			
			mc.gameMode.handleCreativeModeItemAdd(
					PAGE_CONTAINER.swapItem(slot - 36, mc.player.getInventory().items.get(hotbar)),
					hotbar == 45 ? 45 : hotbar + InventoryMenu.USE_ROW_SLOT_START);
		}
	}
	
	public static ItemStack getItem(Minecraft mc, int slot) {
		return slot < 36 ? mc.player.getInventory().items.get(slot) : PAGE_CONTAINER.getItem(slot - 36);
	}
	
}
