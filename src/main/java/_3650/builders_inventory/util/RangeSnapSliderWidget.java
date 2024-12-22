package _3650.builders_inventory.util;

import _3650.builders_inventory.BuildersInventory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class RangeSnapSliderWidget extends AbstractWidget {
	
	private static final ResourceLocation SPRITE_BACKGROUND = ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "util/slider_background");
	private static final ResourceLocation SPRITE_NOTCH = ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "util/slider_notch");
	private static final ResourceLocation SPRITE_BAR = ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "util/slider_bar");
	private static final ResourceLocation SPRITE_BAR_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(BuildersInventory.MOD_ID, "util/slider_bar_highlighted");
	
	public final RangeSnapFloat value;
	private final float lower;
	private final float upper;
	private final int notchctr;
	private boolean dragging = false;
	
	public RangeSnapSliderWidget(int x, int y, RangeSnapFloat value) {
		super(x, y, 100, 30, Component.empty());
		this.value = value;
		this.lower = value.getRange()[0];
		this.upper = value.getRange()[value.getRange().length - 1] - lower;
		this.notchctr = value.getRange().length - 1;
	}
	
	@Override
	protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		gui.blitSprite(SPRITE_BACKGROUND, this.getX(), this.getY(), 100, 30);
		
		final float factor = 88f / (notchctr);
		
		for (int i = 1; i < this.notchctr; i++) {
			int snx = 5 + Math.round(i * factor);
			gui.blitSprite(SPRITE_NOTCH, this.getX() + snx, this.getY() + 10, 2, 10);
		}
		
		int sbx = 4 + Math.round(value.getIndex() * factor);
		final int x = mouseX - this.getX();
		final int y = mouseY - this.getY();
		final ResourceLocation barSprite = (dragging || x >= sbx && x < sbx + 4 && y >= 8 && y < 22) ? SPRITE_BAR_HIGHLIGHTED : SPRITE_BAR;
		gui.blitSprite(barSprite, this.getX() + sbx, this.getY() + 8, 4, 14);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.isValidClickButton(button)) {
			final double x = mouseX - this.getX();
			final double y = mouseY - this.getY();
			if (x >= 3 && x < 97 && y >= 8 && y < 22) {
				final float target = lower + (float)((x - 5) / 90.0) * upper;
				this.value.set(target);
				
				dragging = true;
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (this.isValidClickButton(button) && dragging) {
			final double x = mouseX - this.getX();
			final float target = lower + (float)((x - 5) / 90.0) * upper;
			this.value.set(target);
			
			return true;
		}
		return false;
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (this.isValidClickButton(button)) {
			dragging = false;
			return true;
		}
		return false;
	}
	
	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		
	}
	
}
