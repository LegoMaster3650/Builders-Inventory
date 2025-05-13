package _3650.builders_inventory.api.minimessage.widgets;

import net.minecraft.client.gui.GuiGraphics;

public interface MiniMessageEventListener {
	
	public void miniMessageTick();
	
	public boolean miniMessageMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY);
	
	public boolean miniMessageMouseClicked(double mouseX, double mouseY, int button);
	
	public void miniMessageRender(GuiGraphics gui, int mouseX, int mouseY);
	
}
