package _3650.builders_inventory.feature.extended_inventory;

import java.util.List;

import _3650.builders_inventory.BuildersInventory;
import _3650.builders_inventory.ModKeybinds;
import _3650.builders_inventory.util.StepSliderWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class ExtendedInventoryIconScreen extends Screen {
	
	private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "textures/gui/container/extended_inventory_icon.png");
	
	private static final WidgetSprites SPRITES_TILE = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/organize/tile"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/organize/tile_highlighted"));
	private static final WidgetSprites SPRITES_TILE_ACTIVE = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/organize/tile_active"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/organize/tile_active_highlighted"));
	
	private static final WidgetSprites SPRITES_BUTTON_COUNT = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_count"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_count_disabled"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_count_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_COUNT_OPEN = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_count_open"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_count_disabled"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_count_open_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_DATA = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_data"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_data_disabled"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_data_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_DATA_OFF = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_data_off"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_data_disabled"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_data_off_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_SIZE = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_size"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_size_disabled"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_size_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_SIZE_OPEN = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_size_open"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_size_disabled"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_size_open_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_CLEAR = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_clear"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_clear_disabled"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_clear_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_RESET = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_reset"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_reset_disabled"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_reset_highlighted"));
	
	private static final WidgetSprites SPRITES_BUTTON_CANCEL = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_cancel"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_cancel_disabled"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_cancel_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_CONFIRM = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_confirm"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_confirm_disabled"),
			ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "extended_inventory/icon/button_confirm_highlighted"));
	
	private final ExtendedImageButtonGui exGui = new ExtendedImageButtonGui();
	private final int imageWidth;
	private final int imageHeight;
	
	private int leftPos;
	private int topPos;
	
	// Preview Tile
	private ExtendedImageButton tilePreview;
	private ExtendedImageButton tilePreviewActive;
	// Toolbar Top
	private ExtendedImageButton buttonCount;
	private ExtendedImageButton buttonCountOpen;
	private ExtendedImageButton buttonData;
	private ExtendedImageButton buttonDataOff;
	private ExtendedImageButton buttonSize;
	private ExtendedImageButton buttonSizeOpen;
	private ExtendedImageButton buttonClear;
	private ExtendedImageButton buttonReset;
	// Toolbar Bottom
	private ExtendedImageButton buttonCancel;
	private ExtendedImageButton buttonConfirm;
	
	// Overlay Widgets
	private StepSliderWidget countSlider;
	private StepSliderWidget sizeSlider;
	
	private final ExtendedInventoryPage page;
	private final int pageIndex;
	
	private ItemStack iconPreview = ItemStack.EMPTY;
	private ItemStack iconPreviewOriginal = ItemStack.EMPTY;
	private boolean tileActive = false;
	private boolean dataActive = true;
	private int iconScaleDown = 0;
	private boolean hasChanged = false;
	
	public ExtendedInventoryIconScreen(int page) {
		super(Component.translatable("container.builders_inventory.extended_inventory.icon", page + 1));
		this.page = ExtendedInventoryPages.get(page);
		this.pageIndex = page;
		this.iconPreviewOriginal = this.page.originalIcon;
		this.iconPreview = this.page.icon.copy();
		this.dataActive = this.page.iconDataActive;
		this.iconScaleDown = this.page.iconScaleDown;
		this.imageWidth = 194;
		this.imageHeight = 204;
	}
	
	@Override
	protected void init() {
		super.init();
		this.exGui.init();
		this.leftPos = (this.width - this.imageWidth) / 2;
		this.topPos = (this.height - this.imageHeight) / 2;
		
		// Create Buttons
		this.createAllButtons();
		// Preview Tile
		this.addRenderableWidget(this.tilePreview);
		this.addRenderableWidget(this.tilePreviewActive);
		// Toolbar Top
		this.addRenderableWidget(this.buttonCount);
		this.addRenderableWidget(this.buttonCountOpen);
		this.addRenderableWidget(this.buttonData);
		this.addRenderableWidget(this.buttonDataOff);
		this.addRenderableWidget(this.buttonSize);
		this.addRenderableWidget(this.buttonSizeOpen);
		this.addRenderableWidget(this.buttonClear);
		this.addRenderableWidget(this.buttonReset);
		// Toolbar Bottom
		this.addRenderableWidget(this.buttonCancel);
		this.addRenderableWidget(this.buttonConfirm);
		
	}
	
	private void createAllButtons() {
		// Preview Tile
		this.tilePreview = new ExtendedImageButton(this.leftPos + 6, this.topPos + 17, 16, 16, SPRITES_TILE,
				button -> {
					this.tileActive = true;
					updateButtons();
				});
		this.tilePreviewActive = new ExtendedImageButton(this.leftPos + 6, this.topPos + 17, 16, 16, SPRITES_TILE_ACTIVE,
				button -> {
					this.tileActive = false;
					updateButtons();
				});
		// Toolbar Top
		this.buttonCount = new ExtendedImageButton(this.leftPos + 7, this.topPos + 37, 14, 14, SPRITES_BUTTON_COUNT,
				button -> {
					openCountSlider();
				},
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.count").withStyle(ChatFormatting.WHITE),
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.count.desc").withStyle(ChatFormatting.GRAY),
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.count.desc.closed").withStyle(ChatFormatting.GRAY));
		this.buttonCountOpen = new ExtendedImageButton(this.leftPos + 7, this.topPos + 37, 14, 14, SPRITES_BUTTON_COUNT_OPEN,
				button -> {
					closeCountSlider();
				},
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.count").withStyle(ChatFormatting.WHITE),
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.count.desc").withStyle(ChatFormatting.GRAY),
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.count.desc.open").withStyle(ChatFormatting.GRAY));
		this.buttonData = new ExtendedImageButton(this.leftPos + 7, this.topPos + 55, 14, 14, SPRITES_BUTTON_DATA,
				button -> {
					dataOff();
				},
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.data").withStyle(ChatFormatting.WHITE),
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.data.desc").withStyle(ChatFormatting.GRAY));
		this.buttonDataOff = new ExtendedImageButton(this.leftPos + 7, this.topPos + 55, 14, 14, SPRITES_BUTTON_DATA_OFF,
				button -> {
					dataOn();
				},
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.data").withStyle(ChatFormatting.WHITE),
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.data.desc").withStyle(ChatFormatting.GRAY));
		this.buttonSize = new ExtendedImageButton(this.leftPos + 7, this.topPos + 73, 14, 14, SPRITES_BUTTON_SIZE,
				button -> {
					openSizeSlider();
				},
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.size").withStyle(ChatFormatting.WHITE),
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.size.desc").withStyle(ChatFormatting.GRAY),
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.size.desc.closed").withStyle(ChatFormatting.GRAY));
		this.buttonSizeOpen = new ExtendedImageButton(this.leftPos + 7, this.topPos + 73, 14, 14, SPRITES_BUTTON_SIZE_OPEN,
				button -> {
					closeSizeSlider();
				},
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.size").withStyle(ChatFormatting.WHITE),
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.size.desc").withStyle(ChatFormatting.GRAY),
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.size.desc.open").withStyle(ChatFormatting.GRAY));
		this.buttonClear = new ExtendedImageButton(this.leftPos + 7, this.topPos + 91, 14, 14, SPRITES_BUTTON_CLEAR,
				button -> {
					clearPreview();
				},
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.clear").withStyle(ChatFormatting.WHITE),
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.clear.desc").withStyle(ChatFormatting.GRAY));
		this.buttonReset = new ExtendedImageButton(this.leftPos + 7, this.topPos + 109, 14, 14, SPRITES_BUTTON_RESET,
				button -> {
					resetPreview();
				},
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.reset").withStyle(ChatFormatting.WHITE),
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.reset.desc").withStyle(ChatFormatting.GRAY));
		// Toolbar Bottom
		this.buttonCancel = new ExtendedImageButton(this.leftPos + 7, this.topPos + 163, 14, 14, SPRITES_BUTTON_CANCEL,
				button -> {
					ExtendedInventory.open(this.minecraft);
				},
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.cancel").withStyle(ChatFormatting.WHITE),
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.cancel.desc").withStyle(ChatFormatting.GRAY));
		this.buttonConfirm = new ExtendedImageButton(this.leftPos + 7, this.topPos + 181, 14, 14, SPRITES_BUTTON_CONFIRM,
				button -> {
					savePreview();
					ExtendedInventory.open(this.minecraft);
				},
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.confirm").withStyle(ChatFormatting.WHITE),
				Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.button.confirm.desc").withStyle(ChatFormatting.GRAY));
		
		updateButtons();
	}
	
	private void updateButtons() {
		// Preview Tile
		this.tilePreview.visible = !this.tileActive;
		this.tilePreviewActive.visible = this.tileActive;
		// Toolbar
		if (this.iconPreview.isEmpty()) {
			// Top
			this.buttonCount.active = false;
			this.buttonCountOpen.active = false;
			this.buttonData.active = false;
			this.buttonDataOff.active = false;
			this.buttonSize.active = false;
			this.buttonSizeOpen.active = false;
			this.buttonClear.active = false;
		} else {
			// Top
			this.buttonCount.active = true;
			this.buttonCountOpen.active = true;
			this.buttonData.active = !this.iconPreviewOriginal.getComponentsPatch().isEmpty();
			this.buttonDataOff.active = this.buttonData.active;
			this.buttonSize.active = this.minecraft.options.guiScale().get() >= 2;
			this.buttonSizeOpen.active = true;
			this.buttonClear.active = true;
		}
		// Top
		this.buttonCount.visible = this.countSlider == null;
		this.buttonCountOpen.visible = this.countSlider != null;
		this.buttonData.visible = this.dataActive;
		this.buttonDataOff.visible = !this.dataActive;
		this.buttonSize.visible = this.sizeSlider == null;
		this.buttonSizeOpen.visible = this.sizeSlider != null;
		this.buttonReset.active = hasChanged;
		// Bottom
		this.buttonConfirm.active = hasChanged;
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
		gui.drawString(this.font, this.title, this.leftPos + 8, this.topPos + 6, 0x404040, false);
		
		int x = this.leftPos + 26;
		int y = this.topPos + 18;
		int slot = 0;
		for (int row = 0; row < 6; row++) {
			int slotY = y + (row * 18);
			for (int col = 0; col < 9; col++) {
				int slotX = x + (col * 18);
				ItemStack stack = this.page.get(slot++);
				gui.renderItem(stack, slotX, slotY);
				gui.renderItemDecorations(this.font, stack, slotX, slotY);
			}
		}
		
		Inventory playerInv = this.minecraft.player.getInventory();
		
		y += 108;
		slot = 9;
		for (int row = 0; row < 3; row++) {
			int slotY = y + (row * 18);
			for (int col = 0; col < 9; col++) {
				int slotX = x + (col * 18);
				ItemStack stack = playerInv.getItem(slot++);
				gui.renderItem(stack, slotX, slotY);
				gui.renderItemDecorations(this.font, stack, slotX, slotY);
			}
		}
		
		y += 54;
		slot = 0;
		for (int col = 0; col < 9; col++) {
			int slotX = x + (col * 18);
			ItemStack stack = playerInv.getItem(slot++);
			gui.renderItem(stack, slotX, y);
			gui.renderItemDecorations(this.font, stack, slotX, y);
		}
		
		var hover = (this.countSlider == null && this.sizeSlider == null) ? findSlot(mouseX - this.leftPos, mouseY - this.topPos) : null;
		if (hover != null) {
			int gx = this.leftPos + 26 + (hover.col * 18);
			int gy = this.topPos + 18 + (hover.row * 18);
			gui.fillGradient(RenderType.guiOverlay(), gx, gy, gx + 16, gy + 16, 0x80FFFFFF, 0x80FFFFFF, 2);
		}
		
		if (this.iconPreview.isEmpty()) {
			this.renderTileText(this.pageIndex + 1, gui, this.leftPos + 6 + 8, this.topPos + 17 + 4, 100);
		} else {
			this.renderTileIcon(this.iconPreview, this.iconScaleDown, gui, this.leftPos + 6, this.topPos + 17, 300);
		}
		
		this.renderTooltip(gui, mouseX, mouseY, hover);
	}
	
	private void renderTooltip(GuiGraphics gui, int x, int y, ItemFindResult hover) {
		if (hover != null && !hover.item.isEmpty()) {
			gui.renderTooltip(this.font, getTooltipFromItem(this.minecraft, hover.item), hover.item.getTooltipImage(), x, y);
		} else {
			gui.pose().pushPose();
			gui.pose().translate(0, 0, 800);
			this.exGui.renderTooltip(this.font, gui, x, y);
			gui.pose().popPose();
		}
	}
	
	void renderTileText(int number, GuiGraphics gui, int x, int y, int z) {
		String text = String.valueOf(number);
		gui.pose().pushPose();
		gui.pose().translate(x, y, z);
		if (text.length() > 2) {
			float scale = 2f / text.length();
			gui.pose().translate(0, 4 * (1f - scale), 0); // (8*scale - 8) / 2
			gui.pose().scale(scale, scale, 1f);
		}
		gui.drawCenteredString(this.font, Component.literal(text), 0, 0, 0xFFFFFF);
		gui.pose().popPose();
	}
	
	void renderTileIcon(ItemStack icon, int iconScaleDown, GuiGraphics gui, int x, int y, int z) {
		gui.pose().pushPose();
		gui.pose().translate(x, y, z);
		if (iconScaleDown > 0) {
			final int guiScale = (int) (this.minecraft.getWindow().getGuiScale() + 0.5);
			final float iconScale = iconScaleDown >= guiScale ? (1f / guiScale) : (1f - (iconScaleDown / (float)guiScale));
			gui.pose().translate((8 * (1f - iconScale)), (8 * (1f - iconScale)), 0);
			gui.pose().scale(iconScale, iconScale, 1);
		}
		gui.renderItem(icon, 0, 0);
		gui.renderItemDecorations(this.font, icon, 0, 0);
		gui.pose().popPose();
	}
	
	@Override
	public void renderBackground(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		this.renderTransparentBackground(gui);
		gui.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
	}
	
	public void openCountSlider() {
		this.countSlider = new StepSliderWidget(this.leftPos + 24, this.topPos + 33, 600, 1, 64, this.iconPreview.getCount(), this.font,
				val -> {
					return List.of(Component.literal(String.valueOf(val)));
				},
				val -> {
					this.iconPreview.setCount(val);
					this.hasChanged = true;
					updateButtons();
				},
				initialVal -> {
					this.iconPreview.setCount(initialVal);
					closeCountSlider();
				});
		this.addRenderableWidget(this.countSlider);
		this.updateButtons();
	}
	
	public void closeCountSlider() {
		if (this.countSlider == null) return;
		this.removeWidget(this.countSlider);
		this.countSlider = null;
		this.updateButtons();
	}
	
	public void openSizeSlider() {
		final int guiScale = (int) (this.minecraft.getWindow().getGuiScale() + 0.5);
		this.sizeSlider = new StepSliderWidget(this.leftPos + 24, this.topPos + 69, 600, 1 - guiScale, 0, -this.iconScaleDown, this.font,
				val -> {
					final double sizePercent = (guiScale + val) * 100.0 / (guiScale);
					return List.of(Component.translatable("container.builders_inventory.extended_inventory.icon.tooltip.slider.size",
							ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(sizePercent), guiScale + val, guiScale));
				},
				val -> {
					this.iconScaleDown = -val;
					this.hasChanged = true;
					updateButtons();
				},
				initialVal -> {
					this.iconScaleDown = -initialVal;
					closeSizeSlider();
				});
		this.addRenderableWidget(this.sizeSlider);
		this.updateButtons();
	}
	
	public void closeSizeSlider() {
		if (this.sizeSlider == null) return;
		this.removeWidget(this.sizeSlider);
		this.sizeSlider = null;
		this.updateButtons();
	}
	
	public void dataOff() {
		if (!this.dataActive) return;
		this.dataActive = false;
		this.iconPreview = new ItemStack(this.iconPreviewOriginal.getItem(), this.iconPreview.getCount());
		updateButtons();
	}
	
	public void dataOn() {
		if (this.dataActive) return;
		this.dataActive = true;
		this.iconPreview = this.iconPreviewOriginal.copyWithCount(this.iconPreview.getCount());
		updateButtons();
	}
	
	public void setPreview(ItemStack stack) {
		this.iconPreviewOriginal = stack;
		this.iconPreview = stack.copyWithCount(1);
		this.dataActive = true;
		this.hasChanged = true;
		updateButtons();
	}
	
	public void resetPreview() {
		this.iconPreviewOriginal = page.originalIcon;
		this.iconPreview = page.icon.copy();
		this.dataActive = page.iconDataActive;
		this.iconScaleDown = page.iconScaleDown;
		this.hasChanged = false;
		updateButtons();
	}
	
	public void clearPreview() {
		this.iconPreviewOriginal = ItemStack.EMPTY;
		this.iconPreview = ItemStack.EMPTY;
		this.dataActive = false;
		this.iconScaleDown = 0;
		this.hasChanged = true;
		updateButtons();
	}
	
	public void savePreview() {
		page.originalIcon = this.iconPreviewOriginal.copy();
		page.icon = this.iconPreview.copy();
		page.iconDataActive = this.dataActive;
		page.iconScaleDown = this.iconScaleDown;
		page.setChanged();
		this.hasChanged = false;
		updateButtons();
	}
	
	public ItemFindResult findSlot(int x, int y) {
		x -= 25;
		y -= 17;
		if (x >= 0 && x < 162 && y >= 0 && y < 180) {
			int col = x / 18;
			int row = y / 18;
			if (row < 6) return new ItemFindResult(page.get(col + (row * 9)), row, col);
			else if (row < 9) return new ItemFindResult(this.minecraft.player.getInventory().getItem(col + ((row - 6) * 9) + 9), row, col);
			else if (row < 10) return new ItemFindResult(this.minecraft.player.getInventory().getItem(col + ((row - 9) * 9)), row, col);
		}
		return null;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button)) {
			return true;
		} else {
			var target = findSlot((int)mouseX - this.leftPos, (int)mouseY - this.topPos);
			if (target != null && target.item != ItemStack.EMPTY) {
				this.setPreview(target.item);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
			this.onClose();
			return true;
		} else if (ModKeybinds.OPEN_EXTENDED_INVENTORY.matches(keyCode, scanCode)) {
			ExtendedInventory.open(this.minecraft);
			return true;
		} else return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}
	
	private static class ItemFindResult {
		public final ItemStack item;
		public final int row;
		public final int col;
		
		public ItemFindResult(ItemStack item, int row, int col) {
			this.item = item;
			this.row = row;
			this.col = col;
		}
	}
	
}
