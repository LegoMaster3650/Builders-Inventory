package _3650.builders_inventory.api.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class GuiUtil {
	
	public static void blitScreenBackground(GuiGraphics gui, ResourceLocation atlasLocation, int x, int y, int width, int height) {
		gui.blit(RenderType::guiTextured, atlasLocation, x, y, 0, 0, width, height, 256, 256);
	}
	
}
