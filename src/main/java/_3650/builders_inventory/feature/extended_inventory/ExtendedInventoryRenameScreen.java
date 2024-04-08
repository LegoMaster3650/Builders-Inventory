package _3650.builders_inventory.feature.extended_inventory;

import org.lwjgl.glfw.GLFW;

import _3650.builders_inventory.BuildersInventory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ExtendedInventoryRenameScreen extends Screen {
	
	private static final ResourceLocation BACKGROUND = new ResourceLocation(BuildersInventory.MOD_ID, "textures/gui/container/extended_inventory_rename.png");
	
	private static final WidgetSprites SPRITES_BUTTON_CONFIRM = new WidgetSprites(
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/rename/button_confirm"),
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/rename/button_confirm_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_CANCEL = new WidgetSprites(
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/rename/button_cancel"),
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/rename/button_cancel_highlighted"));
	private static final WidgetSprites SPRITES_BUTTON_CLEAR = new WidgetSprites(
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/rename/button_clear"),
			new ResourceLocation(BuildersInventory.MOD_ID, "extended_inventory/rename/button_clear_highlighted"));
	
	private final ExtendedImageButtonGui exGui = new ExtendedImageButtonGui();
	private final int imageWidth;
	private final int imageHeight;
	
	private int leftPos;
	private int topPos;
	private EditBox name;
	
	protected ExtendedInventoryRenameScreen(int page) {
		super(Component.translatable("container.builders_inventory.extended_inventory.rename", page + 1));
		this.imageWidth = 198;
		this.imageHeight = 54;
	}
	
	@Override
	protected void init() {
		super.init();
		this.exGui.init();
		this.leftPos = (this.width - this.imageWidth) / 2;
		this.topPos = (this.height - this.imageHeight) / 2;
		
		// Text Field
		this.name = new EditBox(this.font, this.leftPos + 22, this.topPos + 23, 154, 12, title);
		this.name.setCanLoseFocus(false);
		this.name.setTextColor(-1);
		this.name.setTextColorUneditable(-1);
		this.name.setBordered(false);
		this.name.setMaxLength(22);
		this.name.setValue(ExtendedInventory.getPageName());
		this.addRenderableWidget(this.name);
		this.setInitialFocus(this.name);
		
		// Buttons
		this.addRenderableWidget(new ExtendedImageButton(this.leftPos + 180, this.topPos + 20, 14, 14, SPRITES_BUTTON_CONFIRM,
				button -> {
					this.confirmPageName();
					ExtendedInventory.open(this.minecraft);
				},
				Component.translatable("container.builders_inventory.extended_inventory.rename.tooltip.button.confirm").withStyle(ChatFormatting.WHITE)));
		this.addRenderableWidget(new ExtendedImageButton(this.leftPos + 181, this.topPos + 5, 12, 12, SPRITES_BUTTON_CANCEL,
				button -> {
					ExtendedInventory.open(this.minecraft);
				},
				Component.translatable("container.builders_inventory.extended_inventory.rename.tooltip.button.cancel").withStyle(ChatFormatting.WHITE)));
		this.addRenderableWidget(new ExtendedImageButton(this.leftPos + 5, this.topPos + 21, 12, 12, SPRITES_BUTTON_CLEAR,
				button -> {
					this.name.setValue("");
				},
				Component.translatable("container.builders_inventory.extended_inventory.rename.tooltip.button.clear").withStyle(ChatFormatting.WHITE)));
	}
	
	private void confirmPageName() {
		String name = this.name.getValue();
		ExtendedInventory.setPageName(name.isBlank() ? "" : name);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			confirmPageName();
			ExtendedInventory.open(this.minecraft);
			return true;
		} else return super.keyPressed(keyCode, scanCode, modifiers);
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
		
		this.exGui.renderTooltip(this.font, gui, mouseX, mouseY);
	}
	
	@Override
	public void renderBackground(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		super.renderBackground(gui, mouseX, mouseY, partialTick);
		gui.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}
	
}
