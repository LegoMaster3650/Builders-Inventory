package _3650.builders_inventory.feature.extended_inventory;

import java.util.List;

import _3650.builders_inventory.BuildersInventory;
import _3650.builders_inventory.ModKeybinds;
import _3650.builders_inventory.api.widgets.exbutton.ExtendedImageButton;
import _3650.builders_inventory.api.widgets.exbutton.ExtendedImageButtonGui;
import _3650.builders_inventory.api.widgets.exbutton.ExtendedImageDualButton;
import _3650.builders_inventory.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeInventoryListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ExtendedInventoryScreen extends AbstractContainerScreen<ExtendedInventoryMenu> {
	
	public static final ResourceLocation BACKGROUND = BuildersInventory.modLoc("textures/gui/container/extended_inventory/inventory.png");
	public static final ResourceLocation BACKGROUND_LOCKED = BuildersInventory.modLoc("textures/gui/container/extended_inventory/inventory_locked.png");
	public static final ResourceLocation BACKGROUND_INVALID = BuildersInventory.modLoc("textures/gui/container/extended_inventory/inventory_invalid.png");
	
	private static final WidgetSprites SPRITES_BUTTON_REPAIR = new WidgetSprites(
			BuildersInventory.modLoc("extended_inventory/button_repair"),
			BuildersInventory.modLoc("extended_inventory/button_repair_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_ORGANIZE = new WidgetSprites(
			BuildersInventory.modLoc("extended_inventory/button_organize"),
			BuildersInventory.modLoc("extended_inventory/button_organize_disabled"),
			BuildersInventory.modLoc("extended_inventory/button_organize_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_DELETE = new WidgetSprites(
			BuildersInventory.modLoc("extended_inventory/button_delete"),
			BuildersInventory.modLoc("extended_inventory/button_delete_disabled"),
			BuildersInventory.modLoc("extended_inventory/button_delete_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_ICON = new WidgetSprites(
			BuildersInventory.modLoc("extended_inventory/button_icon"),
			BuildersInventory.modLoc("extended_inventory/button_icon_disabled"),
			BuildersInventory.modLoc("extended_inventory/button_icon_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_RENAME = new WidgetSprites(
			BuildersInventory.modLoc("extended_inventory/button_rename"),
			BuildersInventory.modLoc("extended_inventory/button_rename_disabled"),
			BuildersInventory.modLoc("extended_inventory/button_rename_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_UNLOCKED = new WidgetSprites(
			BuildersInventory.modLoc("extended_inventory/button_lock_unlocked"),
			BuildersInventory.modLoc("extended_inventory/button_lock_unlocked_disabled"),
			BuildersInventory.modLoc("extended_inventory/button_lock_unlocked_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_LOCKED = new WidgetSprites(
			BuildersInventory.modLoc("extended_inventory/button_lock_locked"),
			BuildersInventory.modLoc("extended_inventory/button_lock_locked_disabled"),
			BuildersInventory.modLoc("extended_inventory/button_lock_locked_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_LEFT = new WidgetSprites(
			BuildersInventory.modLoc("extended_inventory/button_switch_left"),
			BuildersInventory.modLoc("extended_inventory/button_switch_left_disabled"),
			BuildersInventory.modLoc("extended_inventory/button_switch_left_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_RIGHT = new WidgetSprites(
			BuildersInventory.modLoc("extended_inventory/button_switch_right"),
			BuildersInventory.modLoc("extended_inventory/button_switch_right_disabled"),
			BuildersInventory.modLoc("extended_inventory/button_switch_right_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_RIGHT_NEW = new WidgetSprites(
			BuildersInventory.modLoc("extended_inventory/button_switch_right_new"),
			BuildersInventory.modLoc("extended_inventory/button_switch_right_new_highlighted"));
	
	private final ExtendedImageButtonGui exGui = new ExtendedImageButtonGui();
	
	private CreativeInventoryListener listener;
	
	// Top Buttons
	private ExtendedImageButton buttonRepair;
	// Toolbar Buttons
	private ExtendedImageButton buttonOrganize;
	private ExtendedImageButton buttonDelete;
	private ExtendedImageButton buttonIcon;
	private ExtendedImageButton buttonRename;
	private ExtendedImageDualButton buttonLock;
	// Switch Buttons
	private ExtendedImageButton buttonLeft;
	private ExtendedImageDualButton buttonRight;
	
	private Component pageTitle;
	
	public ExtendedInventoryScreen(Player player) {
		super(new ExtendedInventoryMenu(0, player.inventoryMenu), player.getInventory(), Component.translatable("container.builders_inventory.extended_inventory"));
		player.containerMenu = this.menu;
		this.imageWidth = 212;
		this.imageHeight = 222;
		this.titleLabelX += 18;
		this.inventoryLabelX += 18;
		this.inventoryLabelY = this.imageHeight - 94;
		this.pageTitle = this.title;
		this.createAllButtons();
	}
	
	@Override
	protected void init() {
		if (this.minecraft.gameMode.hasInfiniteItems()) {
			super.init();
			
			//ExGUI
			this.exGui.init();
			
			// Listener
			this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
			this.listener = new CreativeInventoryListener(this.minecraft);
			this.minecraft.player.inventoryMenu.addSlotListener(this.listener);
			
			// Create Buttons
			this.createAllButtons();
			// Top 
			this.addRenderableWidget(this.buttonRepair);
			// Toolbar Buttons
			this.addRenderableWidget(this.buttonOrganize);
			this.addRenderableWidget(this.buttonDelete);
			this.addRenderableWidget(this.buttonIcon);
			this.addRenderableWidget(this.buttonRename);
			this.addRenderableWidget(this.buttonLock);
			// Switch Buttons
			this.addRenderableWidget(this.buttonLeft);
			this.addRenderableWidget(this.buttonRight);
			
			// Page Title
			this.updatePageTitle();
		} else {
			ExtendedInventory.close(this.minecraft);
		}
	}
	
	@Override
	public void removed() {
		super.removed();
		if (this.minecraft.player != null && this.minecraft.player.getInventory() != null) {
			this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
		}
	}
	
	private void createAllButtons() {
		// Top Buttons
		this.buttonRepair = new ExtendedImageButton(this.leftPos + 192, this.topPos + 4, 12, 12,
				SPRITES_BUTTON_REPAIR,
				btn -> {
					int page = ExtendedInventory.getPage();
					ExtendedInventoryPages.reset(page);
					ExtendedInventory.setPage(page);
					ExtendedInventory.refreshScreen(this);
				},
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.repair").withStyle(ChatFormatting.WHITE),
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.repair.warn").withStyle(ChatFormatting.RED));
		
		// Toolbar Buttons
		this.buttonOrganize = new ExtendedImageButton(this.leftPos + 172, this.topPos + 126, 12, 12,
				SPRITES_BUTTON_ORGANIZE,
				btn -> ExtendedInventory.openOrganizeScreen(this.minecraft),
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.organize").withStyle(ChatFormatting.WHITE));
		this.buttonDelete = new ExtendedImageButton(this.leftPos + 154, this.topPos + 126, 12, 12,
				SPRITES_BUTTON_DELETE,
				btn -> ExtendedInventory.openDeleteScreen(this.minecraft),
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.delete").withStyle(ChatFormatting.WHITE),
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.delete.desc").withStyle(ChatFormatting.GRAY));
		this.buttonIcon = new ExtendedImageButton(this.leftPos + 136, this.topPos + 126, 12, 12,
				SPRITES_BUTTON_ICON,
				btn -> ExtendedInventory.openIconSelectScreen(this.minecraft),
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.icon").withStyle(ChatFormatting.WHITE),
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.icon.desc").withStyle(ChatFormatting.GRAY));
		this.buttonRename = new ExtendedImageButton(this.leftPos + 118, this.topPos + 126, 12, 12,
				SPRITES_BUTTON_RENAME,
				btn -> ExtendedInventory.openRenameScreen(this.minecraft),
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.rename").withStyle(ChatFormatting.WHITE));
		this.buttonLock = new ExtendedImageDualButton(this.leftPos + 100, this.topPos + 126, 12, 12,
				SoundEvents.LODESTONE_COMPASS_LOCK,
				SPRITES_BUTTON_UNLOCKED,
				btn -> ExtendedInventory.lock(this),
				List.of(
						Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.lock").withStyle(ChatFormatting.WHITE),
						Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.lock.desc.1").withStyle(ChatFormatting.GRAY),
						Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.lock.desc.2").withStyle(ChatFormatting.GRAY)),
				SoundEvents.IRON_TRAPDOOR_OPEN,
				SPRITES_BUTTON_LOCKED,
				btn -> ExtendedInventory.unlock(this),
				List.of(
						Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.unlock").withStyle(ChatFormatting.WHITE),
						Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.unlock.desc").withStyle(ChatFormatting.GRAY))
				);
		
		// Switch Buttons
		this.buttonLeft = new ExtendedImageButton(this.leftPos + 6, this.topPos + 17, 16, 108,
				SPRITES_BUTTON_LEFT,
				btn -> {
					ExtendedInventory.switchLeft(this);
					this.updatePageTitle();
				},
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.left").withStyle(ChatFormatting.WHITE));
		this.buttonRight = new ExtendedImageDualButton(this.leftPos + 190, this.topPos + 17, 16, 108,
				SPRITES_BUTTON_RIGHT,
				btn -> {
					ExtendedInventory.switchRight(this);
					this.updatePageTitle();
				},
				List.of(
						Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.right").withStyle(ChatFormatting.WHITE)),
				SPRITES_BUTTON_RIGHT_NEW,
				btn -> {
					ExtendedInventory.createPageAndSwitch(this);
					this.updatePageTitle();
					this.clearFocus();
				},
				List.of(
						Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.right").withStyle(ChatFormatting.WHITE),
						Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.right_new").withStyle(ChatFormatting.GREEN))
				);
		
		updateButtons();
	}
	
	public void updateButtons() {
		// Reused Data
		boolean validPage = ExtendedInventory.PAGE_CONTAINER.isValid();
		boolean lockedPage = ExtendedInventory.PAGE_CONTAINER.isLocked();
		
		if (ExtendedInventoryPages.isValid()) {
			// Top Buttons
			this.buttonRepair.visible = !validPage;
			// Toolbar Buttons
			this.buttonDelete.active = true;
			this.buttonIcon.active = validPage;
			this.buttonRename.active = validPage;
			this.buttonLock.secondMode = lockedPage;
			// Switch Buttons
			int page = ExtendedInventory.getPage();
			this.buttonLeft.active = page > 0;
			this.buttonRight.secondMode = page >= ExtendedInventoryPages.size() - 1;
			if (this.buttonRight.secondMode) this.clearFocus();
		} else {
			// Top Buttons
			this.buttonRepair.visible = false;
			// Toolbar Buttons
			this.buttonDelete.active = false;
			this.buttonIcon.active = false;
			this.buttonRename.active = false;
			this.buttonLock.active = false;
			this.buttonLock.secondMode = lockedPage;
			// Switch Buttons
			this.buttonLeft.active = false;
			this.buttonRight.active = false;
			this.buttonRight.secondMode = false;
		}
	}
	
	public void updatePageTitle() {
		this.pageTitle = ExtendedInventory.pageTitle(ExtendedInventory.getPage());
	}
	
	@Override
	protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) {
		this.exGui.addRenderableWidget(widget);
		return super.addRenderableWidget(widget);
	}
	
	@Override
	protected void clearWidgets() {
		this.exGui.clearWidgets();
		super.clearWidgets();
	}
	
	@Override
	public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		super.render(gui, mouseX, mouseY, partialTick);
		this.renderTooltip(gui, mouseX, mouseY);
	}
	
	@Override
	protected void renderTooltip(GuiGraphics gui, int mouseX, int mouseY) {
		if (this.hoveredSlot instanceof ExtendedInventoryMenu.ExtendedInventorySlot slot) {
			if (!ExtendedInventoryPages.isValid()) {
				gui.renderComponentTooltip(this.font, List.of(
						Component.translatable("container.builders_inventory.extended_inventory.tooltip.failed_load.title").withStyle(ChatFormatting.RED),
						Component.translatable("container.builders_inventory.extended_inventory.tooltip.failed_load.desc.1").withStyle(ChatFormatting.WHITE),
						Component.translatable("container.builders_inventory.extended_inventory.tooltip.failed_load.desc.2").withStyle(ChatFormatting.WHITE)
					), mouseX, mouseY);
				return;
			}
			
			if (!slot.exContainer.isValid()) {
				gui.renderComponentTooltip(this.font, List.of(
						Component.translatable("container.builders_inventory.extended_inventory.tooltip.invalid_page.title").withStyle(ChatFormatting.RED),
						Component.translatable("container.builders_inventory.extended_inventory.tooltip.invalid_page.desc.1").withStyle(ChatFormatting.WHITE),
						Component.translatable("container.builders_inventory.extended_inventory.tooltip.invalid_page.desc.2").withStyle(ChatFormatting.WHITE),
						Component.translatable("container.builders_inventory.extended_inventory.tooltip.invalid_page.desc.3").withStyle(ChatFormatting.WHITE),
						Component.translatable("container.builders_inventory.extended_inventory.tooltip.invalid_page.desc.4").withStyle(ChatFormatting.WHITE)
					), mouseX, mouseY);
				return;
			}
		}
		
		if (this.exGui.renderTooltip(this.font, gui, mouseX, mouseY)) return;
		
		super.renderTooltip(gui, mouseX, mouseY);
	}
	
	@Override
	protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
		gui.drawString(this.font, this.pageTitle, this.titleLabelX, this.titleLabelY, 0x404040, false);
		gui.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
	}
	
	@Override
	protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
		int relX = (this.width - this.imageWidth) / 2;
		int relY = (this.height - this.imageHeight) / 2;
		ResourceLocation bg = ExtendedInventory.PAGE_CONTAINER.isValid() ? ExtendedInventory.PAGE_CONTAINER.isLocked() ? BACKGROUND_LOCKED : BACKGROUND : BACKGROUND_INVALID;
		gui.blit(bg, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
	}
	
	@Override
	protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
		type = slotId == -999 && type == ClickType.PICKUP ? ClickType.THROW : type;
		if (slot == this.menu.destroyItemSlot) {
			var clear = Config.instance().extended_inventory_clear_behavior;
			if (type == ClickType.QUICK_MOVE && clear != ExtendedInventoryClearBehavior.NONE) {
				if (clear.player) {
					for (int i = 0; i < this.minecraft.player.inventoryMenu.getItems().size(); i++) {
						this.minecraft.gameMode.handleCreativeModeItemAdd(ItemStack.EMPTY, i);
					}
					this.minecraft.player.inventoryMenu.broadcastChanges();
				}
				if (clear.extended) {
					ExtendedInventory.PAGE_CONTAINER.clearContent();
				}
			} else {
				this.menu.setCarried(ItemStack.EMPTY);
			}
		} else if (slot != null && !slot.mayPickup(this.minecraft.player)) {
			return;
		} else if (ExtendedInventory.PAGE_CONTAINER.isLocked() && slot instanceof ExtendedInventoryMenu.ExtendedInventorySlot && type != ClickType.SWAP) {
			// Slot is guaranteed not null by the instanceof i think
			ItemStack carried = this.menu.getCarried();
			ItemStack stack = slot.getItem();
			
			if (type == ClickType.QUICK_CRAFT) return; // not my problem
			
			if (type == ClickType.SWAP) {
				if (!stack.isEmpty()) {
					this.minecraft.player.getInventory().setItem(mouseButton, stack.copyWithCount(stack.getMaxStackSize()));
					this.minecraft.player.inventoryMenu.broadcastChanges();
				}
				return;
			}
			
			if (type == ClickType.CLONE) {
				if (carried.isEmpty() && !stack.isEmpty()) {
					this.menu.setCarried(stack.copyWithCount(stack.getMaxStackSize()));
				}
				return;
			}
			
			if (type == ClickType.THROW) {
				if (!stack.isEmpty()) {
					ItemStack drop = stack.copyWithCount(mouseButton == 0 ? 1 : stack.getMaxStackSize());
					this.minecraft.player.drop(drop, true);
					this.minecraft.gameMode.handleCreativeModeItemDrop(drop);
				}
				return;
			}
			
			if (!carried.isEmpty() && !stack.isEmpty() && ItemStack.isSameItemSameComponents(carried, stack)) {
				if (mouseButton == 0) {
					if (type == ClickType.QUICK_MOVE) {
						carried.setCount(carried.getMaxStackSize());
					} else if (carried.getCount() < carried.getMaxStackSize()) {
						carried.grow(1);
					}
				} else {
					if (type == ClickType.QUICK_MOVE) {
						this.menu.setCarried(ItemStack.EMPTY);
					} else {
						carried.shrink(1);
					}
				}
			} else if (carried.isEmpty() && !stack.isEmpty()) {
				this.menu.setCarried(stack.copyWithCount(type == ClickType.QUICK_MOVE ? stack.getMaxStackSize() : stack.getCount()));
			} else if (mouseButton == 0) {
				this.menu.setCarried(ItemStack.EMPTY);
			} else if (!carried.isEmpty()) {
				carried.shrink(1);
			}
		} else if (type == ClickType.QUICK_CRAFT && slot == null) {
			this.minecraft.player.inventoryMenu.clicked(slotId, mouseButton, type, this.minecraft.player);
			this.menu.clicked(slotId, mouseButton, type, this.minecraft.player);
			this.minecraft.player.inventoryMenu.broadcastChanges();
		} else if (slot instanceof ExtendedInventoryMenu.WrappedSlot wrappedSlot) {
			this.wrappedSlotClicked(wrappedSlot.target, slotId, mouseButton, type);
		} else if (type == ClickType.THROW && slot != null && slot.hasItem()) {
			ItemStack stack = slot.remove(mouseButton == 0 ? 1 : slot.getItem().getMaxStackSize());
			this.minecraft.player.drop(stack, true);
			this.minecraft.gameMode.handleCreativeModeItemDrop(stack);
		} else if (type == ClickType.THROW && !this.menu.getCarried().isEmpty()) {
			this.minecraft.player.drop(this.menu.getCarried(), true);
			this.minecraft.gameMode.handleCreativeModeItemDrop(this.menu.getCarried());
			this.menu.setCarried(ItemStack.EMPTY);
		} else if (type == ClickType.SWAP) {
			ExtendedInventory.swap(this.minecraft, slotId + 36, mouseButton);
		} else {
			this.menu.clicked(slotId, mouseButton, type, this.minecraft.player);
		}
	}
	
	private void wrappedSlotClicked(Slot target, int slotId, int mouseButton, ClickType type) {
		if (type == ClickType.THROW && target != null && target.hasItem()) {
			ItemStack stack = target.remove(mouseButton == 0 ? 1 : target.getItem().getMaxStackSize());
			ItemStack remaining = target.getItem();
			this.minecraft.player.drop(stack, true);
			this.minecraft.gameMode.handleCreativeModeItemDrop(stack);
			this.minecraft.gameMode.handleCreativeModeItemAdd(remaining, target.index);
		} else if (type == ClickType.PICKUP_ALL || type == ClickType.QUICK_MOVE) {
			this.menu.clicked(slotId, mouseButton, type, this.minecraft.player);
			this.minecraft.player.inventoryMenu.broadcastChanges();
		} else {
			this.minecraft.player.inventoryMenu.clicked(target.index, mouseButton, type, this.minecraft.player);
			this.minecraft.player.inventoryMenu.broadcastChanges();
		}
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (ModKeybinds.OPEN_EXTENDED_INVENTORY.matches(keyCode, scanCode)) {
			ExtendedInventory.close(this.minecraft);
			return true;
		} else return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	protected void containerTick() {
		super.containerTick();
		if (this.minecraft != null && !this.minecraft.gameMode.hasInfiniteItems()) {
			ExtendedInventory.close(this.minecraft);
		}
	}
	
}
