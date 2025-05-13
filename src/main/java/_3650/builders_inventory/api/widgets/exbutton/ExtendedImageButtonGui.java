package _3650.builders_inventory.api.widgets.exbutton;

import java.util.ArrayList;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public class ExtendedImageButtonGui {
	
	private final ArrayList<AbstractExtendedImageButton> exButtons = new ArrayList<>();
	
	public void init() {
		this.clearWidgets();
	}
	
	public <T extends GuiEventListener & Renderable & NarratableEntry> void addRenderableWidget(T widget) {
		if (widget instanceof AbstractExtendedImageButton button) this.exButtons.add(button);
	}
	
	public void clearWidgets() {
		this.exButtons.clear();
	}
	
	public boolean renderTooltip(Font font, GuiGraphics gui, int mouseX, int mouseY) {
		for (var button : this.exButtons) {
			final var tooltip = button.tooltip();
			if (button.isActive() && button.isHoveredOrFocused() && !tooltip.isEmpty()) {
				gui.renderComponentTooltip(font,
						tooltip,
						button.isHovered() ? mouseX : button.getCenterX(),
						button.isHovered() ? mouseY : button.getCenterY());
				return true;
			} else {
				final var disabledTooltip = button.disabledTooltip.get();
				if (button.visible && !button.active && button.isHoveredOrFocused() && !disabledTooltip.isEmpty()) {
					gui.renderComponentTooltip(font,
							disabledTooltip,
							button.isHovered() ? mouseX : button.getCenterX(),
							button.isHovered() ? mouseY : button.getCenterY());
					return true;
				}
			}
		}
		return false;
	}
	
}
