package _3650.builders_inventory.mixin.feature.minimessage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Style;

@Mixin(GuiGraphics.class)
public interface GuiGraphicsInvoker {
	
	@Accessor("mouseX")
	public int getMouseX();
	
	@Accessor("mouseY")
	public int getMouseY();
	
	@Accessor("hoveredTextStyle")
	public void setHoveredTextStyle(Style style);
	
	@Accessor("clickableTextStyle")
	public void setClickableTextStyle(Style style);
	
	@Invoker("createDefaultTextParameters")
	public ActiveTextCollector.Parameters callCreateDefaultTextParameters(float opacity);
	
}
