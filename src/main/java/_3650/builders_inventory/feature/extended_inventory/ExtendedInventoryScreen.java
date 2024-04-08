package _3650.builders_inventory.feature.extended_inventory;

import java.util.List;

import _3650.builders_inventory.BuildersInventory;
import _3650.builders_inventory.ModKeybinds;
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
	
	public static final ResourceLocation BACKGROUND = new ResourceLocation(BuildersInventory.MOD_ID, "textures/gui/container/extended_inventory.png");
	public static final ResourceLocation BACKGROUND_LOCKED = new ResourceLocation(BuildersInventory.MOD_ID, "textures/gui/container/extended_inventory_locked.png");
	public static final ResourceLocation BACKGROUND_INVALID = new ResourceLocation(BuildersInventory.MOD_ID, "textures/gui/container/extended_inventory_invalid.png");
	
	private static final WidgetSprites SPRITES_BUTTON_RENAME = new WidgetSprites(
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_rename"),
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_rename_disabled"),
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_rename_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_REPAIR = new WidgetSprites(
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_repair"),
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_repair_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_ORGANIZE = new WidgetSprites(
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_organize"),
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_organize_disabled"),
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_organize_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_DELETE = new WidgetSprites(
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_delete"),
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_delete_disabled"),
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_delete_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_UNLOCKED = new WidgetSprites(
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_lock_unlocked"),
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_lock_unlocked_disabled"),
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_lock_unlocked_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_LOCKED = new WidgetSprites(
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_lock_locked"),
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_lock_locked_disabled"),
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_lock_locked_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_LEFT = new WidgetSprites(
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_switch_left"),
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_switch_left_disabled"),
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_switch_left_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_RIGHT = new WidgetSprites(
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_switch_right"),
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_switch_right_disabled"),
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_switch_right_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_RIGHT_NEW = new WidgetSprites(
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_switch_right_new"),
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/button_switch_right_new_highlighted"));
	
	private final ExtendedImageButtonGui exGui = new ExtendedImageButtonGui();
	
	private CreativeInventoryListener listener;
	
	// Top Buttons
	private ExtendedImageButton buttonRename;
	private ExtendedImageButton buttonRepair;
	// Toolbar Buttons
	private ExtendedImageButton buttonOrganize;
	private ExtendedImageButton buttonDelete;
	private ExtendedImageButton buttonLock;
	private ExtendedImageButton buttonUnlock;
	// Switch Buttons
	private ExtendedImageButton buttonLeft;
	private ExtendedImageButton buttonRight;
	private ExtendedImageButton buttonRightNew;
	
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
			this.addRenderableWidget(this.buttonRename);
			this.addRenderableWidget(this.buttonRepair);
			// Toolbar Buttons
			this.addRenderableWidget(this.buttonOrganize);
			this.addRenderableWidget(this.buttonDelete);
			this.addRenderableWidget(this.buttonLock);
			this.addRenderableWidget(this.buttonUnlock);
			// Switch Buttons
			this.addRenderableWidget(this.buttonLeft);
			this.addRenderableWidget(this.buttonRight);
			this.addRenderableWidget(this.buttonRightNew);
			
			// Page Title
			this.updatePageTitle();
		} else {
			ExtendedInventory.close(this.minecraft);
		}
	}
	
	private void createAllButtons() {
		// Top Buttons
		this.buttonRename = new ExtendedImageButton(this.leftPos + 8, this.topPos + 4, 12, 12, SPRITES_BUTTON_RENAME,
				button -> ExtendedInventory.openRenameScreen(this.minecraft),
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.rename").withStyle(ChatFormatting.WHITE));
		this.buttonRepair = new ExtendedImageButton(this.leftPos + 192, this.topPos + 4, 12, 12, SPRITES_BUTTON_REPAIR,
				button -> {
					int page = ExtendedInventory.getPage();
					ExtendedInventoryPages.reset(page);
					ExtendedInventory.setPage(page);
					ExtendedInventory.refreshScreen(this);
				},
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.repair").withStyle(ChatFormatting.WHITE),
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.repair.warn").withStyle(ChatFormatting.RED));
		
		// Toolbar Buttons
		this.buttonOrganize = new ExtendedImageButton(this.leftPos + 172, this.topPos + 126, 12, 12, SPRITES_BUTTON_ORGANIZE,
				button -> ExtendedInventory.openOrganizeScreen(this.minecraft),
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.organize").withStyle(ChatFormatting.WHITE));
		this.buttonDelete = new ExtendedImageButton(this.leftPos + 154, this.topPos + 126, 12, 12, SPRITES_BUTTON_DELETE,
				button -> ExtendedInventory.openDeleteScreen(this.minecraft),
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.delete").withStyle(ChatFormatting.WHITE),
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.delete.desc").withStyle(ChatFormatting.GRAY));
		this.buttonLock = new ExtendedImageButton(this.leftPos + 100, this.topPos + 126, 12, 12, SPRITES_BUTTON_UNLOCKED,
				button -> ExtendedInventory.lock(this),
				SoundEvents.LODESTONE_COMPASS_LOCK,
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.lock").withStyle(ChatFormatting.WHITE),
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.lock.desc.1").withStyle(ChatFormatting.GRAY),
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.lock.desc.2").withStyle(ChatFormatting.GRAY));
		this.buttonUnlock = new ExtendedImageButton(this.leftPos + 100, this.topPos + 126, 12, 12, SPRITES_BUTTON_LOCKED,
				button -> ExtendedInventory.unlock(this),
				SoundEvents.IRON_TRAPDOOR_OPEN,
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.unlock").withStyle(ChatFormatting.WHITE),
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.unlock.desc").withStyle(ChatFormatting.GRAY));
		
		// Switch Buttons
		this.buttonLeft = new ExtendedImageButton(this.leftPos + 6, this.topPos + 17, 16, 108, SPRITES_BUTTON_LEFT,
				button -> {
					ExtendedInventory.switchLeft(this);
					this.updatePageTitle();
				},
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.left").withStyle(ChatFormatting.WHITE));
		this.buttonRight = new ExtendedImageButton(this.leftPos + 190, this.topPos + 17, 16, 108, SPRITES_BUTTON_RIGHT,
				button -> {
					ExtendedInventory.switchRight(this);
					this.updatePageTitle();
				},
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.right").withStyle(ChatFormatting.WHITE));
		this.buttonRightNew = new ExtendedImageButton(this.leftPos + 190, this.topPos + 17, 16, 108, SPRITES_BUTTON_RIGHT_NEW,
				button -> {
					ExtendedInventory.switchRight(this);
					this.updatePageTitle();
					this.clearFocus();
				},
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.right").withStyle(ChatFormatting.WHITE),
				Component.translatable("container.builders_inventory.extended_inventory.tooltip.button.right_new").withStyle(ChatFormatting.GREEN));
		
		updateButtons();
	}
	
	public void updateButtons() {
		// Reused Data
		boolean validPage = ExtendedInventory.PAGE_CONTAINER.isValid();
		boolean lockedPage = ExtendedInventory.PAGE_CONTAINER.isLocked();
		
		if (ExtendedInventoryPages.isValid()) {
			// Top Buttons
			this.buttonRename.active = validPage;
			this.buttonRepair.visible = !validPage;
			// Toolbar Buttons
			this.buttonDelete.active = true;
			this.buttonLock.active = validPage;
			this.buttonLock.visible = !lockedPage;
			this.buttonUnlock.active = validPage;
			this.buttonUnlock.visible = lockedPage;
			// Switch Buttons
			int page = ExtendedInventory.getPage();
			this.buttonLeft.active = page > 0;
			this.buttonRight.visible = page < ExtendedInventoryPages.size() - 1;
			this.buttonRightNew.visible = page >= ExtendedInventoryPages.size() - 1;
			if (this.buttonRightNew.visible) this.clearFocus();
		} else {
			// Top Buttons
			this.buttonRename.active = false;
			this.buttonRepair.visible = false;
			// Toolbar Buttons
			this.buttonDelete.active = false;
			this.buttonLock.active = false;
			this.buttonLock.visible = !lockedPage;
			this.buttonUnlock.active = false;
			this.buttonUnlock.visible = lockedPage;
			// Switch Buttons
			this.buttonLeft.active = false;
			this.buttonRight.active = false;
			this.buttonRight.visible = true;
			this.buttonRightNew.visible = false;
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
			
			if (!carried.isEmpty() && !stack.isEmpty() && ItemStack.isSameItemSameTags(carried, stack)) {
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
