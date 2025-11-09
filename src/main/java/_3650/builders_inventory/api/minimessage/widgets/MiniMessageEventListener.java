package _3650.builders_inventory.api.minimessage.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;

public interface MiniMessageEventListener {
	
	public void miniMessageTick();
	
	public boolean miniMessageMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY);
	
	public boolean miniMessageMouseClicked(MouseButtonEvent event);
	
	public void miniMessageRender(GuiGraphics gui, int mouseX, int mouseY);
	
}
