package _3650.builders_inventory.feature.extended_inventory;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import _3650.builders_inventory.BuildersInventory;
import _3650.builders_inventory.ModKeybinds;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ExtendedInventoryDeleteScreen extends Screen {
	
	public static final DecimalFormat TIMER_FORMAT = Util.make(
		new DecimalFormat("0.0"), decimalFormat -> decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT))
	);
	
	private static final ResourceLocation BACKGROUND = new ResourceLocation(BuildersInventory.MOD_ID, "textures/gui/container/extended_inventory_delete.png");
	
	private static final WidgetSprites SPRITES_BUTTON_DELETE = new WidgetSprites(
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/delete/button_delete"),
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/delete/button_delete_disabled"),
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/delete/button_delete_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_CANCEL = new WidgetSprites(
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/delete/button_cancel"),
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/delete/button_cancel_highlighted"));
	
	private final ExtendedImageButtonGui exGui = new ExtendedImageButtonGui();
	private final int imageWidth;
	private final int imageHeight;
	
	private int leftPos;
	private int topPos;
	
	private final ExtendedInventoryPage page;
	private final int pageIndex;
	
	private final long enableTimer;
	private ExtendedImageButton deleteButton;
	private ExtendedImageButton cancelButton;
	
	public ExtendedInventoryDeleteScreen(int page) {
		super(Component.translatable("container.builders_inventory.extended_inventory.delete", page + 1));
		this.page = ExtendedInventoryPages.get(page);
		this.pageIndex = page;
		this.imageWidth = 179;
		this.imageHeight = 161;
		this.enableTimer = Util.getMillis() + 3500L;
		this.createButtons();
	}
	
	@Override
	protected void init() {
		super.init();
		this.exGui.init();
		this.leftPos = (this.width - this.imageWidth) / 2;
		this.topPos = (this.height - this.imageHeight) / 2;
		
		this.createButtons();
		this.addRenderableWidget(this.deleteButton);
		this.addRenderableWidget(this.cancelButton);
	}
	
	private void createButtons() {
		this.deleteButton = new ExtendedImageButton(this.leftPos + 77, this.topPos + 129, 26, 26, SPRITES_BUTTON_DELETE,
				button -> {
					ExtendedInventoryPages.delete(this.pageIndex);
					ExtendedInventory.setPage(Math.min(ExtendedInventoryPages.size() - 1, this.pageIndex));
					ExtendedInventory.open(this.minecraft);
				},
				List.of(Component.translatable("container.builders_inventory.extended_inventory.delete.tooltip.button.delete").withStyle(ChatFormatting.WHITE),
						Component.translatable("container.builders_inventory.extended_inventory.delete.tooltip.button.delete.desc").withStyle(ChatFormatting.RED)),
				() -> List.of(Component.translatable("container.builders_inventory.extended_inventory.delete.tooltip.button.delete").withStyle(ChatFormatting.WHITE),
						Component.translatable("container.builders_inventory.extended_inventory.delete.tooltip.button.delete.desc").withStyle(ChatFormatting.RED),
						Component.translatable("container.builders_inventory.extended_inventory.delete.tooltip.button.delete.desc.disabled",
								TIMER_FORMAT.format(Math.max(0L, this.enableTimer - Util.getMillis()) / 1000.0)).withStyle(ChatFormatting.GRAY))
				) {
			@Nullable
			@Override
			public ComponentPath nextFocusPath(FocusNavigationEvent event) {
				return null;
			}
		};
		this.deleteButton.active = false;
		this.cancelButton = new ExtendedImageButton(this.leftPos + 110, this.topPos + 135, 14, 14, SPRITES_BUTTON_CANCEL,
				button -> {
					ExtendedInventory.open(this.minecraft);
				},
				Component.translatable("container.builders_inventory.extended_inventory.delete.tooltip.button.cancel").withStyle(ChatFormatting.WHITE),
				Component.translatable("container.builders_inventory.extended_inventory.delete.tooltip.button.cancel.desc").withStyle(ChatFormatting.GRAY));
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
		
		int x = this.leftPos + 10;
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
		
		this.deleteButton.active = Util.getMillis() > this.enableTimer;
		
		this.exGui.renderTooltip(this.font, gui, mouseX, mouseY);
	}
	
	@Override
	public void renderBackground(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		super.renderBackground(gui, mouseX, mouseY, partialTick);
		gui.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
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
	
}
