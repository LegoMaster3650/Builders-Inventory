package _3650.builders_inventory.api.minimessage.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

public interface MiniMessageEventListener {
	
	public void miniMessageTick();
	
	public boolean miniMessageMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY);
	
	public boolean miniMessageMouseClicked(MouseButtonEvent event);
	
	public void miniMessageRender(GuiGraphicsExtractor gui, int mouseX, int mouseY);
	
}
