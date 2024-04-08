package _3650.builders_inventory.feature.extended_inventory;

import java.util.ArrayList;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public class ExtendedImageButtonGui {
	
	private final ArrayList<ExtendedImageButton> exButtons = new ArrayList<>();
	
	public void init() {
		this.clearWidgets();
	}
	
	public <T extends GuiEventListener & Renderable & NarratableEntry> void addRenderableWidget(T widget) {
		if (widget instanceof ExtendedImageButton button) this.exButtons.add(button);
	}
	
	public void clearWidgets() {
		this.exButtons.clear();
	}
	
	public boolean renderTooltip(Font font, GuiGraphics gui, int x, int y) {
		for (var button : this.exButtons) {
			if (button.isActive() && button.isHoveredOrFocused() && !button.tooltip.isEmpty()) {
				gui.renderComponentTooltip(font,
						button.tooltip,
						button.isHovered() ? x : button.getCenterX(),
						button.isHovered() ? y : button.getCenterY());
				return true;
			} else {
				var disabledTooltip = button.disabledTooltip.get();
				if (button.visible && !button.active && button.isHoveredOrFocused() && !disabledTooltip.isEmpty()) {
					gui.renderComponentTooltip(font,
							disabledTooltip,
							button.isHovered() ? x : button.getCenterX(),
							button.isHovered() ? y : button.getCenterY());
					return true;
				}
			}
		}
		return false;
	}
	
}
