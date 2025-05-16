package _3650.builders_inventory.feature.hotbar_swapper;

import _3650.builders_inventory.BuildersInventory;
import _3650.builders_inventory.ModKeybinds;
import _3650.builders_inventory.config.Config;
import _3650.builders_inventory.feature.extended_inventory.ExtendedInventory;
import _3650.builders_inventory.mixin.feature.hotbar_swapper.GuiGraphicsAccessor;
import _3650.builders_inventory.mixin.feature.hotbar_swapper.GuiInvoker;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class HotbarSwapper {
	
	private static final ResourceLocation SPRITE_HOTBAR = ResourceLocation.withDefaultNamespace("hud/hotbar");
	private static final ResourceLocation SPRITE_HOTBAR_SELECTION_WIDE = BuildersInventory.modLoc("hud/hotbar_selection_wide");
	
	public static boolean selecting = false;
	public static boolean singleColumn = false;
	public static int row = 0;
	public static int queueSwap = 0;
	
	public static int max = 4;
	private static int extendedOffset = 2;
	private static int hudShift = (max - 1) * -22 - extendedOffset;
	
	/**
	 * Refreshes the max rows
	 */
	public static void refresh() {
		ExtendedInventory.refresh();
		max = ExtendedInventory.enabled
				&& ExtendedInventory.PAGE_CONTAINER.canModify()
				&& Config.instance().hotbar_swapper_includeExtendedInventory ? 10 : 4;
		extendedOffset = Config.instance().hotbar_swapper_extendedInventoryOffset;
		hudShift = (max - 1) * -22 - extendedOffset;
	}
	
	private static void reset() {
		selecting = false;
		singleColumn = false;
		row = 0;
		queueSwap = 0;
		refresh();
	}
	
	/*
	 * RENDERING
	 */
	
	public static void renderHotbars(GuiGraphics gui, Minecraft mc) {
		if (selecting) {
			int width = gui.guiWidth() / 2;
			
			if (singleColumn) {
				int offset = mc.player.getInventory().selected * 20;
				for (int i = 1; i < max; i++) {
					int y = gui.guiHeight() - 22 - (i * 22);
					if (i > 3) y -= extendedOffset;
					gui.blitSprite(RenderType::guiTextured, SPRITE_HOTBAR, 182, 22, 0, 0, width - 91 + offset, y, 1, 22);
					gui.blitSprite(RenderType::guiTextured, SPRITE_HOTBAR, 182, 22, 1 + offset, 0, width - 91 + offset + 1, y, 20, 22);
					gui.blitSprite(RenderType::guiTextured, SPRITE_HOTBAR, 181, 22, 180, 0, width - 91 + offset + 21, y, 1, 22);
				}
			} else {
				for (int i = 1; i < max; i++) {
					int y = gui.guiHeight() - 22 - (i * 22);
					if (i > 3) y -= extendedOffset;
					gui.blitSprite(RenderType::guiTextured, SPRITE_HOTBAR, width - 91, y, 182, 22);
				}
			}
		}
	}
	
	public static boolean renderHotbarSelector(GuiGraphics gui, Minecraft mc, ResourceLocation selectorSprite) {
		if (selecting) {
			int width = gui.guiWidth() / 2;
			if (singleColumn) {
				var sprite = ((GuiGraphicsAccessor) gui).getSprites().getSprite(selectorSprite).contents();
				var spriteWidth = sprite.width();
				var spriteHeight = sprite.height();
				
				int offset = mc.player.getInventory().selected * 20;
				int y = gui.guiHeight() - 22 - 1 - (row * 22);
				if (row > 3) y -= extendedOffset;
				gui.blitSprite(RenderType::guiTextured, selectorSprite, width - 91 - 1 + offset, y, spriteWidth, spriteHeight);
			} else {
				int y = gui.guiHeight() - 22 - 1 - (row * 22);
				if (row > 3) y -= extendedOffset;
				gui.blitSprite(RenderType::guiTextured, SPRITE_HOTBAR_SELECTION_WIDE, width - 91 - 1, y, 184, 24);
			}
			
			return true;
		}
		return false;
	}
	
	public static void renderItems(GuiGraphics gui, DeltaTracker deltaTick, Minecraft mc, GuiInvoker hud) {
		if (selecting) {
			int width =  gui.guiWidth() / 2;
			LocalPlayer player = mc.player;
			
			if (singleColumn) {
				int x = width - 91 + 3 + (player.getInventory().selected * 20);
				for (int i = 1; i < max; i++) {
					int y = gui.guiHeight() - 22 + 3 - (i * 22);
					if (i > 3) y -= extendedOffset;
					hud.callRenderSlot(gui, x, y, deltaTick, player, ExtendedInventory.getItem(mc, getSlot(i, player.getInventory().selected)), x + (y * 22));
				}
			} else {
				for (int i = 1; i < max; i++) {
					int y = gui.guiHeight() - 22 + 3 - (i * 22);
					if (i > 3) y -= extendedOffset;
					for (int j = 0; j < 9; j++) {
						int x = width - 91 + 3 + (j * 20);
						hud.callRenderSlot(gui, x, y, deltaTick, player, ExtendedInventory.getItem(mc, getSlot(i, j)), x * y);
					}
				}
			}
		}
	}
	
	public static void renderLabels(GuiGraphics gui, Minecraft mc) {
		if (selecting) {
			int width = gui.guiWidth() / 2;
			
			int x = singleColumn ? (width - 91 - 7 + (mc.player.getInventory().selected * 20)) : (width - 91 - 7);
			for (int i = 1; i < max; i++) {
				int y = gui.guiHeight() - 22 + 7 - (i * 22);
				if (i > 3) y -= extendedOffset;
				gui.drawString(mc.font, String.valueOf(i), x, y, 0xFFFFFF, true);
			}
		}
	}
	
	public static ItemStack toolHighlightOverride(ItemStack stack, Minecraft mc, GuiInvoker hud) {
		if (selecting) {
			ItemStack main = getMainItem(mc);
			if (!main.isEmpty()) hud.setToolHighlightTimer((int)(40.0 * mc.options.notificationDisplayTime().get()));
			return main;
		}
		return stack;
	}
	
	public static void shiftHud(GuiGraphics gui) {
		if (selecting) {
			gui.pose().pushPose();
			gui.pose().translate(0, hudShift, 0);
		}
	}
	
	public static void shiftHudReset(GuiGraphics gui) {
		if (selecting) {
			gui.pose().popPose();
		}
	}
	
	/*
	 * LOGIC
	 */
	
	public static void preKeybinds(Minecraft mc) {
		if (selecting && Config.instance().hotbar_swapper_useHotbarHotkeys) {
			// Hotbar keys
			// Consumes all hotbar keys regardless of capacity because I need to cancel them anyways
			for (int i = 0; i < 9; ++i) {
				if (mc.options.keyHotbarSlots[i].consumeClick() && i < max) {
					row = i + 1;
					queueSwap = Config.instance().hotbar_swapper_hotkeyDelay + 1;
				}
			}
		}
	}
	
	public static void clientTick(Minecraft mc) {
		if (mc.player == null) return;
		if (!mc.player.isCreative()) {
			if (selecting) reset();
			return;
		}
		
		if (queueSwap > 0 && --queueSwap == 0) swapRow(mc);
		
		boolean fullKey = ModKeybinds.HOTBAR_SCROLL.isDown();
		boolean fullKeyPressed = ModKeybinds.HOTBAR_SCROLL.consumeClick();
		boolean columnKey = ModKeybinds.COLUMN_SCROLL.isDown();
		boolean columnKeyPressed = ModKeybinds.COLUMN_SCROLL.consumeClick();
		var behavior = Config.instance().hotbar_swapper_behavior;
		
		switch (behavior) {
		case PREFER_FULL:
			if (!Config.instance().hotbar_swapper_stickyModifiers) singleColumn = columnKey;
			if (fullKeyPressed) {
				refresh();
				selecting = true;
				singleColumn = columnKey;
				return;
			}
			if (selecting && !fullKey) swapRow(mc);
			break;
		case PREFER_COLUMN:
			if (!Config.instance().hotbar_swapper_stickyModifiers) singleColumn = !fullKey;
			if (columnKeyPressed) {
				refresh();
				selecting = true;
				singleColumn = !fullKey;
				return;
			}
			if (selecting && !columnKey) swapRow(mc);
			break;
		case SEPARATE:
			if (selecting) {
				if ((!columnKey && singleColumn) || !fullKey && !singleColumn) swapRow(mc);
				return;
			}
			
			if (fullKeyPressed) {
				refresh();
				selecting = true;
				singleColumn = false;
				return;
			}
			if (columnKeyPressed) {
				refresh();
				selecting = true;
				singleColumn = true;
				return;
			}
			break;
		}
	}
	
	public static boolean mouseScroll(double amount) {
		if (selecting) {
			row += amount;
			row %= max;
			if (row < 0) row = max + row; //row is negative, + is -
			return true;
		}
		return false;
	}
	
	/*
	 * Utils
	 */
	
	private static void swapRow(Minecraft mc) {
		if (mc.player == null) {
			reset();
			return;
		}
		if (!mc.player.isCreative()) {
			reset();
			return;
		}
		if (row == 0) {
			reset();
			return;
		}
		
		LocalPlayer player = mc.player;
		Inventory inv = player.getInventory();
		
		if (singleColumn) {
			swap(mc, inv.selected);
		} else {
			for (int i = 0; i < 9; i++) swap(mc, i);
		}
		
		reset();
	}
	
	private static void swap(Minecraft mc, int slot) {
		ExtendedInventory.swap(mc, getSlot(row, slot), slot);
	}
	
	/*
	 * Get Utils
	 */
	
	private static int getSlot(int row, int offset) {
		// this math is scary
		return row == 0 ? offset : row < 4 ? ((9 * (4 - row)) + offset) : ((9 * (max + 3 - row)) + offset);
	}
	
	private static ItemStack getMainItem(Minecraft mc) {
		if (singleColumn) {
			return ExtendedInventory.getItem(mc, getSlot(row, mc.player.getInventory().selected));
		} else {
			for (int i = 0; i < 9; i++) {
				ItemStack stack = ExtendedInventory.getItem(mc, getSlot(row, i));
				if (!stack.isEmpty()) return stack;
			}
			return ItemStack.EMPTY;
		}
	}
	
}
